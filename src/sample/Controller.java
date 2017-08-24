package sample;

import com.jfoenix.controls.*;
import com.jfoenix.transitions.hamburger.HamburgerSlideCloseTransition;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point3D;
import javafx.scene.*;
import javafx.scene.control.*;
import java.net.URL;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Stack;


public class Controller implements Initializable {
    //********************************************************************//
    ReadSerialPort rp;
    private ArrayList viewList = new ArrayList(); //A list of all the active views

    //Setting for controlling size of icons:
    String iconSize="38px";

    //Below are used by various progressbars.
    private ObservableList<Integer> progressList;
    private ObservableList<Integer> statusList;
    private ObservableList<Integer> statusListView=FXCollections.observableArrayList(); //StatusList used by all the views, as to not have eventhandlers conflict

    @FXML
    private AnchorPane mainPane;

    @FXML
    private AnchorPane overlayPane;
    @FXML
    private HBox overlayMenuBox2D;
    @FXML
    private HBox overlayMenuBox3D;

    @FXML
    private Tab tab2D;
    @FXML
    private Tab tab3D;

    private ArrayList<Tab> tabs2D;
    private ArrayList<Tab> tabs3D;
    private int tabIndex2D=0;
    private int tabIndex3D=0;

    @FXML
    private TabPane tabPane;
    SingleSelectionModel<Tab> selectionModel;
    //*********************MENU BAR************************//

    @FXML
    private Button menuButton;
    @FXML
    private Button overlayButton;
    @FXML
    private MenuButton mathButton;
    @FXML
    private Button redoButton;
    @FXML
    private Button undoButton;
    @FXML
    private Button settingsButton;

    //ProgressBar & Status - Static View
    @FXML
    private Button refreshButton;
    @FXML
    private Label progressLabel;
    @FXML
    private JFXProgressBar progressBar;
    @FXML
    private Circle statusCircle;

    //**** BUTTONS ****//
    @FXML
    private JFXButton change2DDefualt;
    @FXML
    private JFXButton change3DDefualt;
    @FXML
    private JFXButton newScene2D;

    //***** SLIDE IN PANES ****//
    @FXML
    private AnchorPane navList;
    @FXML
    private AnchorPane onScreenList;

    //Navigation drawer
    @FXML
    private JFXHamburger menuHamburger;


    //******************************************************//

    //******3D******//
    @FXML
    private Group group3D;
    @FXML
    private Group scatterGroup;


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
        viewList.add(std);

        //**********************SETUP*******************//
        setupButtons();
        setupChip();
        setup3D();
        setupProgressBar();
        setupTabs();
        prepareSlideMenuAnimation(); //Setup of slide-in menu
        prepareOverlayMenuAnimation();
        prepareSlideMenuAnimationHamburger();

        //*******************************************************//

    }


    //*********************************************SETUP METHODS******************************************************//
    private void setup3D(){
        //***********************3D-view setup*******************//
        View3D view3d = new View3D(true, tabPane, tab3D, rp , progressBar, progressList, progressLabel, statusList, group3D, scatterGroup);
        viewList.add(view3d);
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
            //rp.setupPorts();
            System.out.println("Chip available. Defualt to reading from chip.");
        } else {
            statusCircle.setFill(Color.FIREBRICK);
            System.out.println("Chip unavailable. Defualt to reading from harddrive.");
        }
        //*****************************************************************************************************//
    }

    private void setupTabs(){
        //**********************************************TAB SETUP**********************************************//
        tabs2D= new ArrayList<>();
        tabs3D= new ArrayList<>();
        tabs2D.add(tabIndex2D,tab2D);
        tabs3D.add(tabIndex3D,tab3D);
        tabIndex2D++;
        tabIndex3D++;
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
        //*************************** REFRESH STATUS BUTTON *****************************//

        refreshButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                rp.isConnected();
                /*
                if(refreshButton.getRotate()!=0){
                    refreshButton.setRotate(0);
                }
                //RotateTransition rot = new RotateTransition(new Duration(350), refreshButton);
                //rot.setByAngle(360);

                //rot.play();
                */
            }
        });

        Node iconRefresh = GlyphsDude.createIcon(MaterialDesignIcon.REFRESH,iconSize);
        iconRefresh.getStyleClass().add("material-icon");
        refreshButton.setGraphic(iconRefresh);
        refreshButton.getStyleClass().setAll("material-icon-container");


        redoButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                /*
                if(redoButton.getRotate()!=0){
                    redoButton.setRotate(0);
                }
                RotateTransition rot = new RotateTransition(new Duration(350), redoButton);
                rot.setByAngle(360);
                rot.play();
                */
            }
        });

        //Styling for button, with icon.
        Node iconRedo = GlyphsDude.createIcon(MaterialDesignIcon.REDO,iconSize);
        iconRedo.getStyleClass().add("material-icon");
        redoButton.setGraphic(iconRedo);
        redoButton.getStyleClass().setAll("material-icon-container");
        /*
        undoButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(undoButton.getRotate()!=0){
                    undoButton.setRotate(0);
                }
                RotateTransition rot = new RotateTransition(new Duration(350), undoButton);
                rot.setByAngle(-360);
                rot.play();
            }
        });
        */
        Node iconUndo = GlyphsDude.createIcon(MaterialDesignIcon.UNDO,iconSize);
        iconUndo.getStyleClass().add("material-icon");
        undoButton.setGraphic(iconUndo);
        undoButton.getStyleClass().setAll("material-icon-container");
        /*
        settingsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(settingsButton.getRotate()!=0){
                    settingsButton.setRotate(0);
                }
                RotateTransition rot = new RotateTransition(new Duration(350), settingsButton);
                rot.setByAngle(360);
                rot.play();
            }
        });
        */
        Node iconSettings = GlyphsDude.createIcon(MaterialDesignIcon.SETTINGS,iconSize);
        iconSettings.getStyleClass().add("material-icon");
        settingsButton.setGraphic(iconSettings);
        settingsButton.getStyleClass().setAll("material-icon-container");

        //*************************** OVERLAY MENU BUTTONS ******************************//
        newScene2D.setOnAction(e ->{
            Tab newTab = new Tab("2D view-" + Integer.toString(tabIndex2D +1));
            tabs2D.add(tabIndex2D,newTab);
            tabIndex2D++;
            tabPane.getTabs().add(newTab);
            StandardView std = new StandardView(false, newTab,tabPane, rp, progressBar, progressList,progressLabel, statusListView);
            viewList.add(std);
            addNewShortcutButton2D(newTab);
        });
        //*****************************************************************************//

        change2DDefualt.setOnAction(e -> {
            selectionModel.select(tab2D);
        });

        change3DDefualt.setOnAction(e -> {
            selectionModel.select(tab3D);
        });


        //**********************************************************//

        //******************MATH MENU BUTTON****************//
        MenuItem findMax = new MenuItem("Find max");
        findMax.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/chartLineSmallBlack.png")));
        findMax.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                StandardView std = (StandardView)viewList.get(0);
                std.findMax();
                System.out.println("Max found");
            }
        });

        MenuItem findMin = new MenuItem("Find min");
        findMin.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/chartLineSmallBlack.png")));
        findMin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                StandardView std = (StandardView)viewList.get(0);
                std.findMin();
                System.out.println("Min found");
            }
        });
        //Setting up material design rippler for mathButton //TODO
        Node iconMath = GlyphsDude.createIcon(MaterialDesignIcon.MATH_COMPASS,iconSize);
        iconMath.getStyleClass().add("material-icon");
        mathButton.setGraphic(iconMath);
        mathButton.getStyleClass().setAll("material-icon-container");

        mathButton.getItems().addAll(findMax, findMin);

        Node iconOverlay = GlyphsDude.createIcon(MaterialDesignIcon.APPS,iconSize);
        iconOverlay.getStyleClass().add("material-icon");
        overlayButton.setGraphic(iconOverlay);
        overlayButton.getStyleClass().setAll("material-icon-container");
    }

    //******************MENU BAR & OVERLAY ANIMATION****************//
    private void prepareSlideMenuAnimationHamburger(){
        HamburgerSlideCloseTransition burgerTask = new HamburgerSlideCloseTransition(menuHamburger);
        burgerTask.setRate(-1);

        TranslateTransition openNav = new TranslateTransition(new Duration(350), navList);
        openNav.setToX(0);
        TranslateTransition closeNav = new TranslateTransition(new Duration(350), navList);

        menuHamburger.addEventHandler(MouseEvent.MOUSE_PRESSED, (e) -> {
            if(navList.getTranslateX()!=0){
                openNav.play();
            }
            else{
                closeNav.setToX(-(navList.getWidth()));
                closeNav.play();
            }
            burgerTask.setRate(burgerTask.getRate()*-1);
            burgerTask.play();
        });
    }
    //Code taken from StackOverflow question, https://stackoverflow.com/questions/31601900/javafx-how-to-create-slide-in-animation-effect-for-a-pane-inside-a-transparent
    private void prepareSlideMenuAnimation(){


    }

    public void prepareOverlayMenuAnimation(){
        /*
        RotateTransition openOverlayRot = new RotateTransition(new Duration(350), overlayButton);
        openOverlayRot.setByAngle(90);
        RotateTransition closeOverlayRot = new RotateTransition(new Duration(350), overlayButton);
        */

        TranslateTransition openOverlay = new TranslateTransition(new Duration(350), overlayPane);
        openOverlay.setToY(0);
        TranslateTransition closeOverlay = new TranslateTransition(new Duration(350), overlayPane);
        ArrayList<WritableImage> imageList = new ArrayList<>();
        overlayButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(overlayPane.getTranslateY()!=0){
                    for(int i=0; i < tabs2D.size(); i++){
                        //Setup snapshot views
                        imageList.add(i, new WritableImage(900,500));
                        tabs2D.get(i).getContent().snapshot(new SnapshotParameters(), imageList.get(i));
                        Button menuButton = (Button)overlayMenuBox2D.getChildren().get(i);
                        ImageView iw = new ImageView(imageList.get(i));
                        iw.setSmooth(true);
                        iw.setPreserveRatio(true);
                        iw.setFitHeight(menuButton.getHeight());
                        iw.setFitWidth(menuButton.getWidth());
                        //menuButton.setBackground(new Background(new BackgroundImage(iw.getImage(), null, null, null, null)));
                        menuButton.setGraphic(iw);
                    }
                    for(int i=0; i < tabs3D.size(); i++){
                        //imageList.add(i, new WritableImage((int)tabPane.getHeight(),(int)tabPane.getWidth()));
                        imageList.add(i, new WritableImage(900,500));
                        tabs3D.get(i).getContent().snapshot(new SnapshotParameters(), imageList.get(i));
                        Button menuButton = (Button)overlayMenuBox3D.getChildren().get(i);
                        ImageView iw = new ImageView(imageList.get(i));
                        iw.setSmooth(true);
                        iw.setPreserveRatio(true);
                        iw.setFitHeight(menuButton.getHeight());
                        iw.setFitWidth(menuButton.getWidth());
                        //menuButton.setBackground(new Background(new BackgroundImage(iw.getImage(), null, null, null, null)));
                        menuButton.setGraphic(iw);
                    }
                    openOverlay.play();
                    //openOverlayRot.play();
                    selectionModel.getSelectedItem().setDisable(true);

                }else{
                    closeOverlay.setToY(-(mainPane.getHeight()+overlayPane.getHeight()+20));
                    closeOverlay.play();
                    //closeOverlayRot.setByAngle(-90);
                    //closeOverlayRot.play();
                    selectionModel.getSelectedItem().setDisable(false);
                }
            }
        });
        overlayPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                closeOverlay.setToY(-(mainPane.getHeight()+overlayPane.getHeight()+20));
                closeOverlay.play();
                //closeOverlayRot.setByAngle(-90);
                //closeOverlayRot.play();
                selectionModel.getSelectedItem().setDisable(false);
            }
        });
    }



    private void addNewShortcutButton2D(Tab tab){
        Button btn = new Button("2D view-" + Integer.toString(tabIndex2D));
        btn.getStyleClass().add("menu-slide-button");
        overlayMenuBox2D.getChildren().add(tabIndex2D-1,btn);
        btn.setOnAction(e->{
            selectionModel.select(tab);
        });
    }



}
