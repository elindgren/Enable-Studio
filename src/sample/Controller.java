package sample;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.*;
import javafx.scene.Cursor;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

import java.awt.*;
import java.io.File;
import java.net.URL;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ResourceBundle;
import org.gillius.jfxutils.JFXUtil.*;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.gillius.jfxutils.chart.StableTicksAxis;
import org.gillius.jfxutils.chart.XYChartInfo;
import org.gillius.jfxutils.tab.TabUtil;
import org.fxyz3d.utils.*;

import javax.xml.bind.annotation.XmlAnyAttribute;

public class Controller implements Initializable {
    //********************************************************************//
    ReadSerialPort rp;
    private Data data;

    //Below are used by various progressbars.
    private ObservableList<Integer> progressList;
    private ObservableList<Integer> statusList;
    private ObservableList<Integer> statusListView=FXCollections.observableArrayList(); //StatusList used by all the views, as to not have eventhandlers conflict

    @FXML
    private VBox slideMenuBox;

    @FXML
    private Tab tab2D;
    @FXML
    private Tab tab3D;
    private ArrayList<Tab> tabs;
    private int tabIndex=0;

    @FXML
    private TabPane tabPane;
    SingleSelectionModel<Tab> selectionModel;
    //*********************MENU BAR************************//
    @FXML
    private Button menu;
    @FXML
    private AnchorPane navList;

    //ProgressBar & Status - Static View
    @FXML
    private Label progressLabel;
    @FXML
    private ProgressBar progressBar;
    @FXML
    private Circle statusCircle;

    //**** BUTTONS ****//
    @FXML
    private Button change2DDefualt;
    @FXML
    private Button change3DDefualt;
    @FXML
    private Button newScene2D;


    //******************************************************//

    //**************************3D********************//
    /*
    @FXML
    private SubScene scene3D;
    @FXML
    private BorderPane pane3D;
    */
    @FXML
    private Group group3D;
    private MeshView meshView;

    private static final int VIEWPORT_SIZE = 500;
    private static final double MODEL_SCALE_FACTOR = 40;
    private static final double MODEL_X_OFFSET = 0;
    private static final double MODEL_Y_OFFSET = 0;
    private static final double MODEL_Z_OFFSET = VIEWPORT_SIZE * 21;

    private PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform axisGroup = new Xform();
    final Xform world = new Xform();
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    private static final double CAMERA_INITIAL_DISTANCE = -450;
    private static final double CAMERA_INITIAL_X_ANGLE = 70.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 320.0;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private static final double AXIS_LENGTH = 250.0;

    private Xform boxGroup = new Xform();

    // MOUSE AND KEYBOARD HANDLING //
    private static final double CONTROL_MULTIPLIER = 0.1;
    private static final double SHIFT_MULTIPLIER = 10.0;
    private static final double MOUSE_SPEED = 0.1;
    private static final double ROTATION_SPEED = 2.0;
    private static final double TRACK_SPEED = 0.3;

    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;


    //************************************************//

    //********************************************************************//

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        //*****Checks if all the imported fx:id's are declared in FXML-file*****//

        //********************************************************************//

        rp = new ReadSerialPort();
        progressList = rp.getProgressList();
        //Setup of progresslist, as to not have set() crash.
        progressList.add(0, 0);
        progressList.add(1, 1);
        statusListView.add(0,0);
        StandardView std = new StandardView(true,tab2D,tabPane, rp, progressBar, progressList,progressLabel, statusListView);

        //**********************SETUP*******************//
        setupButtons();
        setupChip();
        setup3D();
        setupProgressBar();
        setupTabs();
        prepareSlideMenuAnimation(); //Setup of slide-in menu

        //*******************************************************//
    }


    //*********************************************SETUP METHODS******************************************************//
    private void setup3D(){
        //***********************3D-view setup*******************//

        buildCamera();
        buildAxes();
        buildBox();

        //initCamera();
        //meshView = buildMesh();
        //Group meshInGroup = buildScene();
        SubScene subscene = createScene3D(world);
        handleKeyboard(subscene,world);
        handleMouse(subscene,world);
        this.group3D.getChildren().add(subscene);
        /*
        buildCamera();
        buildAxes();
        scene3D.setCamera(camera);
        */


        //*******************************************************//
    }


    private void setupChip(){
        //***********************************************CHIP STATUS SETUP************************************//
        statusList = rp.getStatusList();
        statusList.addListener(new ListChangeListener<Integer>() {
            @Override
            public void onChanged(Change<? extends Integer> c) {
                if (statusList.get(0).intValue() == 0) {
                    statusCircle.setFill(Color.FIREBRICK);
                    statusListView.set(0,0);
                } else if (statusList.get(0).intValue() == 1) {
                    statusCircle.setFill(Color.FORESTGREEN);
                    statusListView.set(0,1);
                }
            }
        });
        //Setup of default mode. If chip is connected, the program will load data from the chip. If not, it will default to load from a file on
        //the computer

        if (rp.isConnected()) {
            statusCircle.setFill(Color.FORESTGREEN);
            rp.setupPorts();
            System.out.println("Chip available. Defualt to reading from chip.");
        } else {
            statusCircle.setFill(Color.FIREBRICK);
            System.out.println("Chip unavailable. Defualt to reading from harddrive.");
        }
        //*****************************************************************************************************//
    }

    private void setupTabs(){
        //**********************************************TAB SETUP**********************************************//
        tabs= new ArrayList<>();
        tabs.add(tabIndex,tab2D);
        tabIndex++;
        selectionModel=tabPane.getSelectionModel();
        //tabs.makeDroppable(tabPane);
        //tabs.makeDraggable(tabExperimentalView); <- TODO NullpointerException? Why?
        //*****************************************************************************************************//
    }

    private void setupProgressBar(){
        //***********************************************PROGRESS BAR SETUP************************************//
        //Setup of progressBar

        //Add functionality for it to say "Done" when done TODO

        progressList.addListener(new ListChangeListener<Integer>() {
                                     @Override
                                     public void onChanged(Change<? extends Integer> c) {
                                         double progress = progressList.get(0).doubleValue() / progressList.get(1).doubleValue();
                                         progressBar.setProgress(progress);
                                     }
                                 }
        );
        //*****************************************************************************************************//
    }



    private void setupButtons(){
        //*************************** SLIDE-MENU BUTTONS ******************************//
        newScene2D.setOnAction(e ->{
            Tab newTab = new Tab("2D view-" + Integer.toString(tabIndex +1));
            tabs.add(tabIndex,newTab);
            tabIndex++;
            tabPane.getTabs().add(newTab);
            StandardView std = new StandardView(false, newTab,tabPane, rp, progressBar, progressList,progressLabel, statusListView);
            addNewShortcutButton(newTab);
        });
        //*****************************************************************************//

        change2DDefualt.setOnAction(e -> {
            selectionModel.select(tab2D);
        });

        change3DDefualt.setOnAction(e -> {
            selectionModel.select(tab3D);
        });

    }

    //******************MENU BAR SLIDE ANIMATION****************//
    //Code taken from StackOverflow question, https://stackoverflow.com/questions/31601900/javafx-how-to-create-slide-in-animation-effect-for-a-pane-inside-a-transparent
    private void prepareSlideMenuAnimation(){
        TranslateTransition openNav = new TranslateTransition(new Duration(350), navList);
        openNav.setToX(0);
        TranslateTransition closeNav = new TranslateTransition(new Duration(350), navList);
        menu.setOnAction(e -> {
            if(navList.getTranslateX()!=0){
                openNav.play();
            }
            else{
                closeNav.setToX(-(navList.getWidth()));
                closeNav.play();
            }
        });
    }

    //**********************************************************//

    private void addNewShortcutButton(Tab tab){
        Button btn = new Button("2D view-" + Integer.toString(tabIndex));
        slideMenuBox.getChildren().add(tabIndex-1,btn);
        btn.setOnAction(e->{
            selectionModel.select(tab);
        });
    }


    //CODE BELOW TAKEN FROM JAVAFX 3D example, Molecule

    private void buildCamera() {
        group3D.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
    }


    private void buildAxes() {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        final Box xAxis = new Box(AXIS_LENGTH, 1, 1);
        final Box yAxis = new Box(1, AXIS_LENGTH, 1);
        final Box zAxis = new Box(1, 1, AXIS_LENGTH);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        axisGroup.setVisible(true);
        world.getChildren().addAll(axisGroup);
    }

    private SubScene createScene3D(Group group) {
        SubScene scene3d = new SubScene(group, 500, 500, true, SceneAntialiasing.BALANCED);
        scene3d.widthProperty().bind(((AnchorPane) group3D.getParent()).widthProperty());
        scene3d.heightProperty().bind(((AnchorPane) group3D.getParent()).heightProperty());

        scene3d.setFill(Color.WHITE);
        scene3d.setCamera(camera);
        scene3d.setPickOnBounds(true);
        return scene3d;
    }

    /*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

//
// The handleMouse() method is used in the MoleculeSampleApp application to
// handle the different 3D camera views.
// This method is used in the Getting Started with JavaFX 3D Graphics tutorial.
//

    private void handleMouse(SubScene scene, final Node root) {

        scene.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseOldX = me.getSceneX();
                mouseOldY = me.getSceneY();
            }
        });
        scene.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent me) {
                mouseOldX = mousePosX;
                mouseOldY = mousePosY;
                mousePosX = me.getSceneX();
                mousePosY = me.getSceneY();
                mouseDeltaX = (mousePosX - mouseOldX);
                mouseDeltaY = (mousePosY - mouseOldY);

                double modifier = 1.0;

                if (me.isControlDown()) {
                    modifier = CONTROL_MULTIPLIER;
                }
                if (me.isShiftDown()) {
                    modifier = SHIFT_MULTIPLIER;
                }
                if (me.isPrimaryButtonDown()) {
                    cameraXform.ry.setAngle(cameraXform.ry.getAngle() -
                            mouseDeltaX*modifier*ROTATION_SPEED);  //
                    cameraXform.rx.setAngle(cameraXform.rx.getAngle() +
                            mouseDeltaY*modifier*ROTATION_SPEED);  // -
                }
                else if (me.isSecondaryButtonDown()) {
                    double z = camera.getTranslateZ();
                    double newZ = z + mouseDeltaX*MOUSE_SPEED*modifier;
                    camera.setTranslateZ(newZ);
                }
                else if (me.isMiddleButtonDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() +
                            mouseDeltaX*MOUSE_SPEED*modifier*TRACK_SPEED);  // -
                    cameraXform2.t.setY(cameraXform2.t.getY() +
                            mouseDeltaY*MOUSE_SPEED*modifier*TRACK_SPEED);  // -
                }
            }
        }); // setOnMouseDragged
    } //handleMouse


    /*
 * Copyright (c) 2013, 2014 Oracle and/or its affiliates.
 * All rights reserved. Use is subject to license terms.
 *
 * This file is available and licensed under the following license:
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  - Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  - Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the distribution.
 *  - Neither the name of Oracle nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

//
// The handleKeyboard() method is used in the MoleculeSampleApp application to
// handle the different 3D camera views.
// This method is used in the Getting Started with JavaFX 3D Graphics tutorial.
//

    private void handleKeyboard(SubScene scene, final Node root) {

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                switch (event.getCode()) {
                    case Z:
                        cameraXform2.t.setX(0.0);
                        cameraXform2.t.setY(0.0);
                        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
                        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
                        break;
                    case X:
                        axisGroup.setVisible(!axisGroup.isVisible());
                        break;
                    case V:
                        boxGroup.setVisible(!boxGroup.isVisible());
                        break;
                } // switch
            } // handle()
        });  // setOnKeyPressed
    }  //  handleKeyboard()


    private void buildBox(){
        Box box3D = new Box(20, 12, 40);
        final PhongMaterial blackMaterial = new PhongMaterial();
        blackMaterial.setSpecularColor(Color.LIGHTGREY);
        blackMaterial.setDiffuseColor(Color.GREY);
        box3D.setMaterial(blackMaterial);
        /*
        Sphere sphere3D = new Sphere(40.0);
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setSpecularColor(Color.BURLYWOOD);
        blueMaterial.setDiffuseColor(Color.BLUE);
        sphere3D.setMaterial(blueMaterial);
        */

        Xform group3d = new Xform();
        group3d.getChildren().add(box3D);
        //group3d.getChildren().add(sphere3D);

        boxGroup.getChildren().add(group3d);
        world.getChildren().addAll(boxGroup);
    }
}
