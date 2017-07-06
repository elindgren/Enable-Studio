package sample;

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
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Stack;


public class Controller implements Initializable {
    //********************************************************************//
    ReadSerialPort rp;
    private ArrayList viewList = new ArrayList(); //A list of all the active views

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
    private Button menuButton;
    @FXML
    private MenuButton mathButton;
    @FXML
    private AnchorPane navList;
    @FXML
    private AnchorPane onScreenList;

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

    //******3D******//
    @FXML
    private Group group3D;


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

        //*******************************************************//
    }


    //*********************************************SETUP METHODS******************************************************//
    private void setup3D(){
        //***********************3D-view setup*******************//

        View3D view3d = new View3D(true, tabPane, tab3D, rp , progressBar, progressList, progressLabel, statusList, group3D);
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
        tabs.add(tabIndex+1,tab3D);
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
            viewList.add(std);
            addNewShortcutButton(newTab);
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

        mathButton.getItems().addAll(findMax, findMin);

    }

    //******************MENU BAR SLIDE ANIMATION****************//
    //Code taken from StackOverflow question, https://stackoverflow.com/questions/31601900/javafx-how-to-create-slide-in-animation-effect-for-a-pane-inside-a-transparent
    private void prepareSlideMenuAnimation(){
        TranslateTransition openNav = new TranslateTransition(new Duration(350), navList);
        openNav.setToX(0);
        TranslateTransition closeNav = new TranslateTransition(new Duration(350), navList);
        ArrayList<WritableImage> imageList = new ArrayList<>();
        menuButton.setOnAction(e -> {
            if(navList.getTranslateX()!=0){
                //Setup snapshot views
                for(int i=0; i < tabs.size(); i++){
                    //imageList.add(i, new WritableImage((int)tabPane.getHeight(),(int)tabPane.getWidth()));
                    imageList.add(i, new WritableImage(900,500));
                    tabs.get(i).getContent().snapshot(new SnapshotParameters(), imageList.get(i));
                    Button menuButton = (Button)slideMenuBox.getChildren().get(i);
                    ImageView iw = new ImageView(imageList.get(i));
                    iw.setSmooth(true);
                    iw.setPreserveRatio(true);
                    iw.setFitHeight(menuButton.getHeight());
                    iw.setFitWidth(menuButton.getWidth());
                    //menuButton.setBackground(new Background(new BackgroundImage(iw.getImage(), null, null, null, null)));
                    menuButton.setGraphic(iw);
                }
                openNav.play();
            }
            else{
                closeNav.setToX(-(navList.getWidth()));
                closeNav.play();
            }
        });
    }

    public void prepareOverlayMenuAnimation(){

    }



    private void addNewShortcutButton(Tab tab){
        Button btn = new Button("2D view-" + Integer.toString(tabIndex));
        btn.getStyleClass().add("menu-slide-button");
        slideMenuBox.getChildren().add(tabIndex-1,btn);
        btn.setOnAction(e->{
            selectionModel.select(tab);
        });
    }



}
