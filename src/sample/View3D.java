package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.fxyz3d.shapes.composites.ScatterPlotMesh;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ericl on 2017-07-05.
 */
public class View3D {
    //**************************3D********************//
    //**** INTER-CLASS COMMUNICATION ****//
    private Data data;
    private ReadSerialPort rp;
    private ProgressBar progressBar;
    private Label progressLabel;
    private ObservableList<Integer> progressList;
    private ObservableList<Integer> statusList;
    private Group group3D;
    private Group scatterGroup;

    //**** CONTAINERS ****//
    private TabPane tabPane;
    private Tab tab;
    private AnchorPane onScreenPaneSlide;
    private AnchorPane onScreenPaneHover;

    //**** ON-SCREEN BUTTONS ****//
    private Button readButton;
    private Button resetButton;
    private Button saveButton;
    private ToggleButton chipModeToggle;
    private ToggleButton cinematicModeToggle;
    private Slider timelineSlider;

    //**** FLAGS ****//
    private boolean readFromChip = false;
    private boolean timelineIsStopped = false;
    private boolean cinematicMode = false;
    private boolean statusView = false;

    //**** DATA ****//
    private double[][] dataList = new double[0][0];

    //**** TASKS ****//
    private Task<Void> readTask;
    private Task<Void> saveTask;

    //**** TIMELINE ****//
    KeyFrame keyFrameAnimated;
    private Timeline timeline;
    private boolean timelineIsFinished;
    private int timelineIteration = 0;

    //**** MESH ****//
    private MeshView meshView;

    //**** CAMERA & MODEL VARIABLES ****//
    //** MODEL **//
    private Box xAxis;
    private Box yAxis;
    private Box zAxis;


    private Xform boxGroup = new Xform();

    //** CAMERA **//
    private PerspectiveCamera camera = new PerspectiveCamera(true);
    final Xform axisGroup = new Xform();
    final Xform world = new Xform();
    final Xform cameraXform = new Xform();
    final Xform cameraXform2 = new Xform();
    final Xform cameraXform3 = new Xform();
    private static final double CAMERA_INITIAL_DISTANCE = -200;
    private static final double CAMERA_INITIAL_X_ANGLE = 30;
    private static final double CAMERA_INITIAL_Y_ANGLE = 200;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private static double AXIS_LENGTH = 50;

    //**** MOUSE AND KEYBOARD HANDLING**** //
    private static final double CONTROL_MULTIPLIER = 0.1;
    private static final double SHIFT_MULTIPLIER = 10.0;
    private static final double MOUSE_SPEED = 0.1;
    private static final double ROTATION_SPEED = 2.0;
    private static final double TRACK_SPEED = 0.3;

    //** MOUSE POSITION **//
    double mousePosX;
    double mousePosY;
    double mouseOldX;
    double mouseOldY;
    double mouseDeltaX;
    double mouseDeltaY;


    //************************************************//

    public View3D(boolean isStandard, TabPane tabPane, Tab tab, ReadSerialPort rp, ProgressBar progressBar, ObservableList<Integer> progressList, Label progressLabel, ObservableList<Integer> statusList, Group group3D, Group scatterGroup){
        this.statusList=statusList;
        this.progressList=progressList;
        this.tabPane = tabPane;
        this.rp=rp;
        this.progressBar = progressBar;
        this.progressLabel = progressLabel;
        this.tab=tab;
        data= new Data(rp);
        this.group3D = group3D;
        this.scatterGroup = scatterGroup;

        if(isStandard) {
            onScreenPaneHover = ((AnchorPane)((StackPane)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1));
            onScreenPaneSlide = (AnchorPane)((AnchorPane)((StackPane)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1)).getChildren().get(0);
            cinematicModeToggle = (ToggleButton) ((HBox)((VBox)((Parent)onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(0)).getChildren().get(0);
            chipModeToggle = (ToggleButton) ((HBox)((VBox)((Parent)onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(0)).getChildren().get(1);
            readButton = (Button)((HBox)((VBox)((Parent)onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(0)).getChildren().get(2);
            resetButton = (Button)((HBox)((VBox)((Parent)onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(0)).getChildren().get(3);
            saveButton = (Button)((HBox)((VBox)((Parent)onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(0)).getChildren().get(4);
            timelineSlider = (Slider) ((VBox)((Parent)onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(1);

            prepareSlideButtonAnimation();
            setupButtons();
            //setupSlider();
            setupStatusListener();

            buildCamera();
            buildAxes();
            buildBox();
            //initCamera();
            //meshView = buildMesh();
            //Group meshInGroup = buildScene();
            SubScene subscene = createScene3D(world);
            handleKeyboard(subscene, world);
            handleMouse(subscene, world);
            this.group3D.getChildren().add(subscene);



            //*** SCATTER CHART ***//
            ScatterPlot sPlot = new ScatterPlot(100, 12, true);
            ScatterPlotMesh aPlot = new ScatterPlotMesh(100, 6, true);
            ArrayList<Double> xData = new ArrayList<>();
            ArrayList<Double> yData = new ArrayList<>();
            ArrayList<Double> zData = new ArrayList<>();

            final Xform world2 = new Xform();
            final Xform cameraX2form = new Xform();
            final Xform cameraX2form2 = new Xform();
            final Xform cameraX2form3 = new Xform();

            for(int i=0; i<100; i++){
                xData.add(new Double(i)/2);
                yData.add(new Double(Math.random()*10));
                zData.add(new Double(Math.random()*100));
            }

            sPlot.setXYZData(xData, yData, zData);
            aPlot.setXYZData(xData, yData, zData);
            world2.getChildren().add(aPlot);
            SubScene subSceneScatter = new SubScene(world2,500, 500, true, SceneAntialiasing.BALANCED);
            subSceneScatter.setFill(Color.LIGHTGREY);
            PerspectiveCamera camera2 = new PerspectiveCamera();
            scatterGroup.getChildren().add(cameraX2form);
            cameraX2form.getChildren().add(cameraX2form2);
            cameraX2form2.getChildren().add(cameraX2form3);
            cameraX2form3.getChildren().add(camera2);
            cameraX2form3.setRotateZ(180.0);

            camera2.setNearClip(CAMERA_NEAR_CLIP);
            camera2.setFarClip(CAMERA_FAR_CLIP);
            camera2.setTranslateZ(CAMERA_INITIAL_DISTANCE);
            cameraX2form.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
            cameraX2form.rx.setAngle(CAMERA_INITIAL_X_ANGLE);

            subSceneScatter.setCamera(camera2);
            subSceneScatter.setPickOnBounds(true);
            subSceneScatter.widthProperty().bind(((AnchorPane) this.scatterGroup.getParent()).widthProperty());
            subSceneScatter.heightProperty().bind(((AnchorPane) this.scatterGroup.getParent()).heightProperty());

            handleKeyboard(subSceneScatter, world2);
            handleMouse(subSceneScatter, world2);
            this.scatterGroup.getChildren().add(subSceneScatter);

        }


        //*******************************************************//
    }

    private void prepareSlideButtonAnimation(){
        TranslateTransition openSlide = new TranslateTransition(new Duration(350), onScreenPaneSlide);
        TranslateTransition openHover = new TranslateTransition(new Duration(350), onScreenPaneHover);
        openSlide.setToY(onScreenPaneSlide.getHeight()+2);
        openHover.setToY(onScreenPaneHover.getHeight()+2);
        TranslateTransition closeSlide = new TranslateTransition(new Duration(350), onScreenPaneSlide);
        TranslateTransition closeHover = new TranslateTransition(new Duration(350), onScreenPaneHover);

        onScreenPaneHover.setOnMouseEntered(e -> {
            openHover.play();
            openSlide.play();

        });

        onScreenPaneHover.setOnMouseExited(e -> {
            closeSlide.setToY(onScreenPaneHover.getHeight());
            closeHover.setToY(onScreenPaneSlide.getHeight() - 38);
            closeHover.play();
            closeSlide.play();
        });
    }


    private void setupButtons(){
        //**********************************************BUTTON SETUP*******************************************//
        //******Toggle: Cinematic mode***********//
        cinematicModeToggle.setOnAction(e -> {
            if (cinematicMode) {
                cinematicMode = false;
            } else {
                cinematicMode = true;
            }
        });
        //*****************************************//
        //******Toggle: Chip or Computer***********//
        chipModeToggle.setOnAction(e -> {
            if (readFromChip) {
                readFromChip = false;
            } else {
                readFromChip = true;
            }
        });
        //*****************************************//


        //******************************BUTTONS - STATIC VIEW********************************//
        readButton.setOnAction(e -> {
            if(cinematicMode) {
                if (readFromChip) {
                    if (!timelineIsFinished) {
                        timeline.stop();
                    }
                    else if(timelineIsFinished){
                        System.out.println("Graph reset");
                    }
                    readChipCinematic();
                /*
                initAnimatedGraph();
                this.startAnimatedTimeline("chip");
                */
                } else {
                    if(statusView) {
                        System.out.println("if");
                    }
                    if(statusView && !timelineIsFinished && !timelineIsStopped){
                        stopAnimatedTimeline();
                        timelineIsStopped = true;
                    }
                    else if (statusView && timelineIsFinished && timelineIteration + 1 == dataList.length) {
                        System.out.println("Graph reset");
                        readFileCinematic();
                    } else if (!timelineIsFinished && statusView || (timelineIteration + 1 != dataList.length && timelineIsFinished)) {
                        //timeline.play();
                        startAnimatedTimeline("file");
                    } else {
                        readFileCinematic();
                    }
                }
            }
            else{
                //Logic for determining whether to read from chip or from disk
                if (readFromChip) {
                    Task<Void> readTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            //Fetching data for table
                            System.out.println("Reading data");
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {

                                    progressLabel.setText("Done");
                                }
                            });
                            return null;
                        }
                    };
                    Thread th = new Thread(readTask);
                    System.out.println("Starting new readThread");
                    progressBar.setVisible(true);
                    progressLabel.setVisible(true);
                    th.start();
                } else if (!readFromChip) {

                    FileChooser fileChoose = new FileChooser();
                    FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("txt File(*.txt)", "*.txt");
                    fileChoose.getExtensionFilters().add(extensionFilter);

                    File file = fileChoose.showOpenDialog(tabPane.getScene().getWindow());

                    if (file != null) {
                        System.out.println("File found");
                        //Open up a new task to read the file.
                        readTask = new Task<Void>() {
                            @Override
                            protected Void call() {
                                System.out.println("Reading data");

                                System.out.println("Read successful");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        progressBar.setVisible(false);
                                        progressLabel.setVisible(false);
                                        progressLabel.setText("Done");
                                    }
                                });
                                return null;
                            }
                        };
                        Thread th = new Thread(readTask);
                        //progressBarStaticView.setProgress(0);
                        System.out.println("Starting new readThread");
                        progressBar.setVisible(true);
                        progressLabel.setVisible(true);
                        th.start();
                    }
                    /*
                        try{
                            while(true) {
                                System.out.println(readTask.getProgress());
                                Thread.sleep(10);
                            }
                        }catch(Exception a){
                            ;
                        }
                        */
                        /* //Not necessary at the moment TODO
                        String fileName = file.getName();
                        String fileExtension = fileName.substring(fileName.indexOf(".") + 1, file.getName().length());
                        System.out.println(fileExtension);
                        while (fileExtension != "txt") {
                            Alert noSuchFileAlert = new Alert(Alert.AlertType.INFORMATION);
                            noSuchFileAlert.setTitle("Error in loading file");
                            noSuchFileAlert.setHeaderText(null);
                            noSuchFileAlert.setContentText("Unallowed file. Please try again.");
                            noSuchFileAlert.showAndWait();
                            file = fileChoose.showOpenDialog(tabPane.getScene().getWindow());
                            fileChoose.showOpenDialog(tabPane.getScene().getWindow());
                            fileName = file.getName();
                            fileExtension = fileName.substring(fileName.indexOf(".") + 1, file.getName().length());
                            System.out.println(fileExtension);
                        }
                        */

                } else {
                    Alert processExitAlert = new Alert(Alert.AlertType.INFORMATION);
                    processExitAlert.setTitle("Read Exit");
                    processExitAlert.setHeaderText(null);
                    processExitAlert.setContentText("Reading of file will now terminate");
                    processExitAlert.showAndWait();
                    //Styling in css
                    DialogPane dialogPane = processExitAlert.getDialogPane();
                    dialogPane.getStylesheets().add(getClass().getResource("stylesheet.css").toExternalForm());
                    dialogPane.getStyleClass().add("stylesheet");
                }
            }

            saveButton.setDisable(false);

            System.out.println("readStatic");
        });


        resetButton.setOnAction(e -> {
            System.out.println("resetStatic");
            data.resetData();
        });

        saveButton.setOnAction(e -> {
            FileChooser fileChoose = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("txt File(*.txt)", "*.txt");
            fileChoose.getExtensionFilters().add(extensionFilter);

            File file = fileChoose.showOpenDialog(tabPane.getScene().getWindow());
            if (file != null) {
                saveTask = new Task<Void>(){
                    protected Void call(){
                        data.writeFile(file, progressList, readFromChip);

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progressLabel.setVisible(false);
                                progressBar.setVisible(false);
                            }
                        });
                        return null;
                    }
                };
                Thread thread = new Thread(saveTask);
                thread.start();
                progressLabel.setVisible(true);
                progressBar.setVisible(true);
            }else{
                System.out.println("Error in saveButton: valid file must be supplied");
            }

        });
    }
    public void setupStatusListener(){
        statusList.addListener(new ListChangeListener<Integer>() {
            @Override
            public void onChanged(Change<? extends Integer> c) {
                if (statusList.get(0).intValue() == 0) {
                    chipModeToggle.setDisable(true);
                } else if (statusList.get(0).intValue() == 1) {
                    chipModeToggle.setDisable(false);
                }
            }
        });
    }



    //****************************************** SETUP TIMELINE *****************************************************//
    public void timelineAnimated(String str) {
        //Creating a timeline for updating the graph
        timeline = new Timeline();
        //****************** EVENT HANDLER FOR KEYFRAME ***************************//
        EventHandler onFinishedFile = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                //XYChart.Data<Number, Number> datapoint = new XYChart.Data<>(dataList.get(timelineIteration).getX(), dataList.get(timelineIteration).getY());
                //seriesX.getData().add(datapoint);
                xAxis.setWidth(dataList[timelineIteration][5]*1000);
                yAxis.setHeight(dataList[timelineIteration][6]*1000);
                zAxis.setDepth(dataList[timelineIteration][7]*1000);
                double progress = Math.round(((double) timelineIteration / dataList.length) * 100);
                System.out.println("Progress: " + progress);
                System.out.println("timeline Iteration: " + timelineIteration);
                timelineSlider.setValue(progress);
                timelineIteration++;
            }
        };
        Duration duration = Duration.millis(10);
        //I don't know how to do actionhandling for keyframe with lambda expression
        keyFrameAnimated = new KeyFrame(duration, onFinishedFile);
        timeline.getKeyFrames().add(keyFrameAnimated);
        timeline.setCycleCount(dataList.length - timelineIteration);

        //*************************************************************************//
    }

    public void startAnimatedTimeline(String str) {
        dataList = data.getFileData();
        timelineAnimated(str);
        timelineSlider.setVisible(true);
        timeline.play();
        System.out.println("Timeline started");
    }

    public void stopAnimatedTimeline() {
        timeline.stop();
    }

    public void readFileCinematic() {
        FileChooser fileChoose = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("txt File(*.txt)", "*.txt");
        fileChoose.getExtensionFilters().add(extensionFilter);

        File file = fileChoose.showOpenDialog(tabPane.getScene().getWindow());

        if (file != null) {
            System.out.println("File found");
            //Open up a new task to read the file. But do so continously.
            readTask = new Task<Void>() {
                @Override
                protected Void call() {
                    System.out.println("Reading data from file...");
                    data.readContinouslyFromFile(file, "x", false, progressList);
                    System.out.println("Read successful");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            startAnimatedTimeline("file3D");
                            //progressLabelStaticView.setText("Done");
                        }
                    });
                    return null;
                }
            };
            Thread th = new Thread(readTask);
            //progressBarStaticView.setProgress(0);
            System.out.println("Starting new readThread: from file");
            //progressBarStaticView.setVisible(true);
            //progressLabelStaticView.setVisible(true);
            th.start();
        }
    }

    public void readChipCinematic() {
        readTask = new Task<Void>() {
            @Override
            protected Void call() {
                System.out.println("Reading data from chip...");
                rp.setBuffer(6);
                rp.continousToDoubleMatrix();
                System.out.println("Read successful");
                /*
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        initAnimatedGraph();
                        startAnimatedTimeline("chip");
                    }
                });
                */
                return null;
            }
        };
        Thread th = new Thread(readTask);
        System.out.println("Starting new readThread: from chip");
        th.start();
        startAnimatedTimeline("chip");
    }
    //***************************************************************************************************************//

    private void setupSlider(){
        //**********************SLIDER SETUP*********************//
        timelineSlider.setVisible(false);
        timelineSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //progressBarStaticView.setProgress(newValue.doubleValue()/timelineSlider.getMax()); //sets the progressbar's progress
                double progress = newValue.doubleValue() / 100;
                int max = timeline.getCycleCount() - 1;
                int newTimelineIteration = 0;
                if (timelineIsFinished) {
                    newTimelineIteration = (int) Math.round(progress * max);
                    if (newTimelineIteration > timelineIteration) {

                        }
                    } else if (newTimelineIteration < timelineIteration) {

                    }
                    timelineIteration = newTimelineIteration;
                    System.out.println("timelineIteration new: " + timelineIteration);
                }

            });
    }
        //*******************************************************//



    //**************************************************************************************************************//

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

        xAxis = new Box(AXIS_LENGTH, 1, 1);
        yAxis = new Box(1, AXIS_LENGTH, 1);
        zAxis = new Box(1, 1, AXIS_LENGTH);

        /*
        xAxis.setTranslateX(AXIS_LENGTH/2);
        yAxis.setTranslateY(AXIS_LENGTH/2);
        zAxis.setTranslateZ(AXIS_LENGTH/2);
        */

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        axisGroup.setVisible(true);
        world.getChildren().addAll(axisGroup);
    }

    private SubScene createScene3D(Group group) {
        SubScene scene3d = new SubScene(group, 10, 10, true, SceneAntialiasing.BALANCED);
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
                double secondaryModifier = 20.0;

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
                    double newZ = z + mouseDeltaX*MOUSE_SPEED*secondaryModifier;
                    camera.setTranslateZ(newZ);
                }
                else if (me.isMiddleButtonDown()) {
                    cameraXform2.t.setX(cameraXform2.t.getX() +
                            mouseDeltaX*MOUSE_SPEED*secondaryModifier*TRACK_SPEED);  // -
                    cameraXform2.t.setY(cameraXform2.t.getY() +
                            mouseDeltaY*MOUSE_SPEED*secondaryModifier*TRACK_SPEED);  // -
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
        Box box3D = new Box(8*4, 2.4*4, 4*4);
        final PhongMaterial blackMaterial = new PhongMaterial();
        blackMaterial.setSpecularColor(Color.LIGHTGREY);
        blackMaterial.setDiffuseColor(Color.GREY);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setSpecularColor(Color.DARKGREEN);
        greenMaterial.setDiffuseColor(Color.FORESTGREEN);
        box3D.setMaterial(greenMaterial);
        /*
        Sphere sphere3D = new Sphere(40.0);
        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setSpecularColor(Color.BURLYWOOD);
        blueMaterial.setDiffuseColor(Color.BLUE);
        sphere3D.setMaterial(blueMaterial);
        */

        Xform group3d = new Xform();
        group3d.getChildren().add(box3D);
        Point3D point = new Point3D(10,10,10);
        //group3d.getChildren().add(point);
        //group3d.getChildren().add(sphere3D);

        boxGroup.getChildren().add(group3d);
        world.getChildren().addAll(boxGroup);
    }

}
