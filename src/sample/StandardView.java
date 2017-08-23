package sample;

import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXSlider;
import com.sun.javaws.progress.Progress;
import com.sun.org.apache.xpath.internal.SourceTree;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.gillius.jfxutils.chart.StableTicksAxis;
import java.io.File;

/**
 * Created by ericl on 2017-06-29.
 */
public class StandardView implements View{

    private int measurementIndex = 0;
    //**** Inter-class communication ****//
    private Data data;
    private ReadSerialPort rp;
    private JFXProgressBar progressBar;
    private Label progressLabel;
    private ObservableList<Integer> progressList;
    private ObservableList<Integer> statusList;

    //**** CONTAINERS ****//
    private AnchorPane onScreenPaneSlide;
    private AnchorPane onScreenPaneHover;
    private TabPane tabPane;
    private Tab tab;
    private TableView table;
    private TableColumn xCol;
    private TableColumn yCol;
    private LineChart<Number,Number> lineChart;
    private Graph2D graph;
    private StableTicksAxis xAxis;
    private StableTicksAxis yAxis;


    //**** Buttons ****//
    private MenuButton chartSetting;
    private Button readButton;
    private Button resetButton;
    private Button saveButton;
    private ToggleButton chipModeToggle;
    private ToggleButton cinematicModeToggle;

    private CheckMenuItem settingX;
    private CheckMenuItem settingY;
    private CheckMenuItem settingZ;
    private CheckMenuItem settingClickData;
    private CheckMenuItem settingGPSData;
    private Menu settingAcc;
    private MenuItem settingForceZeroInRange;

    //**** SLIDER ****//
    private JFXSlider timelineSlider;

    //**** Flags ****//
    private boolean readFromChip = false;
    private boolean cinematicMode = false;

    private boolean settingXHasBeenActivated = false;
    private boolean settingXIsActive = true;
    private boolean settingYHasBeenActivated = false;
    private boolean settingYIsActive = false;
    private boolean settingZHasBeenActivated = false;
    private boolean settingZIsActive = false;

    private boolean statusChart = false; //TODO Change data so that it is the resulting data
    private boolean clickStatusChart = false;
    private int statusPan = 1;

    //**MATH**//
    private boolean findMaxFlag = false;
    private boolean findMinFlag = false;

    //**** DATA ****//
    private ObservableList<DataPoint2D> dataListX = FXCollections.observableArrayList();
    private ObservableList<DataPoint2D> dataListY = FXCollections.observableArrayList();
    private ObservableList<DataPoint2D> dataListZ = FXCollections.observableArrayList();
    private XYChart.Series<Number, Number> seriesX = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> seriesY = new XYChart.Series<>();
    private XYChart.Series<Number, Number> seriesZ = new XYChart.Series<>();
    //**** TASKS ****//
    private Task<Void> readTask;
    private Task<Void> saveTask;

    //**** TIMELINE ****//
    KeyFrame keyFrameAnimated;
    private Timeline timeline;
    private boolean timelineIsFinished = false;
    private boolean timelineIsStopped = false;
    private int timelineIteration = 0;

    public StandardView(boolean isStandard, Tab tab, TabPane tabPane, ReadSerialPort rp, JFXProgressBar progressBar, ObservableList<Integer> progressList, Label progressLabel, ObservableList<Integer> statusList){
        this.statusList=statusList;
        this.progressList=progressList;
        this.tabPane = tabPane;
        this.rp=rp;
        this.progressBar = progressBar;
        this.progressLabel = progressLabel;
        this.tab=tab;
        data= new Data(rp);

        if(isStandard){
            table = (TableView)((StackPane)((Parent)tab.getContent()).getChildrenUnmodifiable().get(0)).getChildren().get(0);
            lineChart = (LineChart<Number, Number>)((AnchorPane)((StackPane)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(0)).getChildren().get(0);
            chartSetting = (MenuButton)((AnchorPane)((StackPane)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(0)).getChildren().get(1);
            xAxis=(StableTicksAxis)lineChart.getXAxis();
            yAxis=(StableTicksAxis)lineChart.getYAxis();


            onScreenPaneHover = ((AnchorPane)((StackPane)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1));
            onScreenPaneSlide = (AnchorPane)((AnchorPane)((StackPane)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1)).getChildren().get(0);
            cinematicModeToggle = (ToggleButton) ((HBox)((VBox)((Parent)onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(0)).getChildren().get(0);
            chipModeToggle = (ToggleButton) ((HBox)((VBox)((Parent)onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(0)).getChildren().get(1);
            readButton = (Button)((HBox)((VBox)((Parent)onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(0)).getChildren().get(2);
            resetButton = (Button)((HBox)((VBox)((Parent)onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(0)).getChildren().get(3);
            saveButton = (Button)((HBox)((VBox)((Parent)onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(0)).getChildren().get(4);
            timelineSlider = (JFXSlider) ((VBox)((Parent)onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(1);
            prepareSlideButtonAnimation();
            /*
            cinematicModeToggle = (ToggleButton) ((HBox)((VBox)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1)).getChildren().get(0);
            chipModeToggle = (ToggleButton) ((HBox)((VBox)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1)).getChildren().get(1);
            readButton = (Button)((HBox)((VBox)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1)).getChildren().get(2);
            resetButton = (Button)((HBox)((VBox)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1)).getChildren().get(3);
            saveButton = (Button)((HBox)((VBox)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1)).getChildren().get(4);
            timelineSlider=(Slider)((VBox)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(2);
            */
        }
        else{
            BorderPane bp = new BorderPane();
            table= new TableView();
            xAxis = new StableTicksAxis();
            yAxis = new StableTicksAxis();
            lineChart = new LineChart<Number, Number>(xAxis,yAxis);

            chartSetting = new MenuButton();


            cinematicModeToggle = new ToggleButton("Cinematic Mode");
            cinematicModeToggle.applyCss();

            chipModeToggle = new ToggleButton("Chip");
            chipModeToggle.applyCss();

            readButton = new Button("Read");
            readButton.applyCss();

            resetButton = new Button("Reset");
            resetButton.applyCss();

            saveButton = new Button("Save");
            saveButton.applyCss();

            timelineSlider = new JFXSlider();
            timelineSlider.applyCss();

            HBox buttonBox = new HBox(cinematicModeToggle, chipModeToggle, readButton, resetButton, saveButton);

            HBox lineChartBox = new HBox(lineChart, chartSetting);

            VBox centerBox = new VBox(lineChartBox, buttonBox, timelineSlider);

            bp.setLeft(table);
            bp.setCenter(centerBox);

            tab.setContent(bp);

        }
        setupButtons();
        setupTable();
        setupSlider();
        setupStatusListener();
        if(statusList.get(0)==0){
            //chipModeToggle.setDisable(true); TODO
        }
        saveButton.setDisable(true);
    }

    public void findMax(){
        int index = data.findMaxIndex();
        if(!findMaxFlag) {
            System.out.println("Trying to find max");
            System.out.println("Max: " + index);
            Circle circ = new Circle(4, Color.TRANSPARENT);
            circ.setStroke(Color.FORESTGREEN);
            seriesX.getData().get(index).setNode(circ);
            lineChart.getData().setAll(seriesX);
            table.scrollTo(index);
            table.getSelectionModel().select(index);
            findMaxFlag = true;
        }else{
            seriesX.getData().get(index ).getNode().setVisible(false);
            findMaxFlag=false;
        }

    }

    public void findMin(){
        int index = data.findMinIndex();
        if(!findMinFlag) {
            System.out.println("Trying to find Min");
            System.out.println("Min: " + index);
            Circle circ = new Circle(4, Color.TRANSPARENT);
            circ.setStroke(Color.FIREBRICK);
            seriesX.getData().get(index).setNode(circ);
            lineChart.getData().setAll(seriesX);
            table.scrollTo(index);
            table.getSelectionModel().select(index);
            findMinFlag=true;
        }else{
            seriesX.getData().get(index).getNode().setVisible(false);
            findMinFlag = false;
        }
    }

    public void prepareSlideButtonAnimation(){
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


    public void setupButtons(){
        //**********************************************BUTTON SETUP*******************************************//
        //******Toggle: Cinematic mode***********//
        cinematicModeToggle.setOnAction(e -> {
            if (cinematicMode) {
                cinematicMode = false;
                readButton.getStyleClass().set(1,"read-button");
            } else {
                cinematicMode = true;
                readButton.getStyleClass().set(1,"play-button");
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
        //**SETUP OF SETTING-box**//

        settingX = new CheckMenuItem("X");
        settingX.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/chartLineSmallBlack.png")));
        settingX.setSelected(true);
        //settingX.setDisable(true);
        settingY = new CheckMenuItem("Y");
        settingY.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/chartLineSmallBlack.png")));
        //settingY.setDisable(true);
        settingZ = new CheckMenuItem("Z");
        settingZ.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/chartLineSmallBlack.png")));
        //settingZ.setDisable(true);

        settingGPSData = new CheckMenuItem("Position");
        settingGPSData.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/gpsOff.png")));

        settingClickData = new CheckMenuItem("Clickable data");
        settingClickData.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/chartClickSmallBlack.png")));
        settingClickData.setDisable(true);

        settingForceZeroInRange = new MenuItem("Force zero in range");

        settingAcc = new Menu("Acc");

        Accordion settingsAccordion = new Accordion();
        TitledPane tp = new TitledPane("Hej", new Button("Button"));
        settingsAccordion.getPanes().add(tp);

        settingX.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!settingX.isSelected() && !statusChart) {
                    settingXIsActive = true;
                } else if (settingX.isSelected() && !statusChart) {
                    settingX.setSelected(false);
                }else if(!settingXHasBeenActivated && !settingX.isSelected() && statusChart && !settingYIsActive && !settingZIsActive){
                    settingXIsActive=true;
                }else if (!settingXHasBeenActivated && !settingXIsActive && statusChart) {
                    data.setupX();
                    seriesX = data.getDataSeriesY();
                    seriesX.setName("x");
                    lineChart.getData().add(seriesX);
                    settingXHasBeenActivated = true;
                    settingXIsActive = true;
                } else if (!settingXIsActive && settingXHasBeenActivated && statusChart) {
                    lineChart.getData().add(seriesX);
                    settingXHasBeenActivated = true;
                    settingXIsActive = true;
                } else if (settingXIsActive && statusChart) {
                    lineChart.getData().removeAll(seriesX);
                    settingXIsActive = false;
                } else if (settingXIsActive && statusChart) {
                    lineChart.getData().removeAll(seriesX);
                    settingXIsActive = false;
                }
            }
        });


        settingY.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!settingYIsActive && !statusChart) {
                    settingYIsActive = true;
                } else if (settingYIsActive && !statusChart) {
                    settingYIsActive = false;
                } else if (!settingYHasBeenActivated && !settingYIsActive && statusChart) {
                    data.setupY();
                    seriesY = data.getDataSeriesY();
                    seriesY.setName("y");
                    lineChart.getData().add(seriesY);
                    settingYHasBeenActivated = true;
                    settingYIsActive = true;
                } else if (!settingYIsActive && settingYHasBeenActivated && statusChart) {
                    lineChart.getData().add(seriesY);
                    settingYHasBeenActivated = true;
                    settingYIsActive = true;
                } else if (settingYIsActive && statusChart) {
                    lineChart.getData().removeAll(seriesY);
                    settingYIsActive = false;
                } else if (settingYIsActive && statusChart) {
                    lineChart.getData().removeAll(seriesY);
                    settingYIsActive = false;
                }
            }
        });

        settingZ.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                if (!settingZIsActive && !statusChart) {
                    settingZIsActive = true;
                } else if (settingZIsActive && !statusChart) {
                    settingZIsActive = false;
                } else if (!settingZHasBeenActivated && !settingZIsActive && statusChart) {
                    data.setupZ();
                    seriesZ = data.getDataSeriesZ();
                    seriesZ.setName("z");
                    lineChart.getData().add(seriesZ);
                    settingZHasBeenActivated = true;
                    settingZIsActive = true;
                } else if (!settingZIsActive && settingZHasBeenActivated && statusChart) {
                    lineChart.getData().add(seriesZ);
                    settingZHasBeenActivated = true;
                    settingZIsActive = true;
                } else if (settingZIsActive && statusChart) {
                    lineChart.getData().removeAll(seriesZ);
                    settingZIsActive = false;
                } else if (settingZIsActive && statusChart) {
                    lineChart.getData().removeAll(seriesZ);
                    settingZIsActive = false;
                }
            }
        });

        settingGPSData.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(!data.getDataMode()) {
                    data.setDataMode("GPS");
                    settingGPSData.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/gpsOn.png")));
                    xCol.setText("Long.");
                    yCol.setText("Lat.");
                }
                else{
                    /*
                    data.setDataMode("Acceleration");
                    chartSetting.getItems().add(0,settingX);
                    chartSetting.getItems().add(1,settingY);
                    chartSetting.getItems().add(2,settingZ);
                    settingAcc.setDisable(true);
                    */
                    settingGPSData.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/gpsOff.png")));
                    xCol.setText("Time <s>");
                    yCol.setText("Acc <m/s^2>");

                }
            }
        });

        settingForceZeroInRange.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(settingForceZeroInRange.getText() == "Force zero in range"){
                    xAxis.setForceZeroInRange(false);
                }
            }
        });
        Menu acc = new Menu("Acceleration");
        //acc.getItems().setAll(settingX,settingY,settingZ);
        Menu gps = new Menu("GPS");
        Menu rotation = new Menu("Rotation");
        Menu magnetic = new Menu("Magnetic");

        acc.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Acc");
                if(settingX.getParentMenu()!= null) {
                    settingX.getParentMenu().getItems().remove(0, 3); //Removing the menuitem from the other menus, if it has already been used
                    System.out.println("Removed settings from previous contextmenu");
                }
                acc.getItems().setAll(settingX,settingY,settingZ);
                data.setDataMode("Acceleration");
                settingX.setText("X");
                settingY.setText("Y");
                settingZ.setText("Z");
                acc.show();
            }
        });
        gps.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("GPS");
                if(settingX.getParentMenu() != null) {
                    settingX.getParentMenu().getItems().remove(0, 3); //Removing the menuitem from the other menus, if it has already been used
                    System.out.println("Removed settings from previous contextmenu");
                }
                gps.getItems().setAll(settingX,settingY,settingZ,settingGPSData);
                data.setDataMode("GPS");
                settingX.setText("Longtitude");
                settingY.setText("Latitude");
                settingZ.setText("Altitude");
                gps.show();
            }
        });
        rotation.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Rotation");
                if(settingX.getParentMenu() != null) {
                    settingX.getParentMenu().getItems().remove(0, 3); //Removing the menuitem from the other menus, if it has already been used
                    System.out.println("Removed settings from previous contextmenu");
                }
                rotation.getItems().setAll(settingX,settingY,settingZ);
                data.setDataMode("Rotation");
                settingX.setText("Pitch");
                settingY.setText("Jaw");
                settingZ.setText("Roll");
                rotation.show();
            }
        });
        magnetic.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Magnetic");
                if(settingX.getParentMenu() != null) {
                    settingX.getParentMenu().getItems().remove(0, 3); //Removing the menuitem from the other menus, if it has already been used
                    System.out.println("Removed settings from previous contextmenu");
                }
                data.setDataMode("Magnetic");
                magnetic.getItems().setAll(settingX,settingY,settingZ);
                settingX.setText("X");
                settingY.setText("Y");
                settingZ.setText("Z");
                magnetic.show();
            }
        });

        settingClickData.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (clickStatusChart) {
                    graph.setDataClickable(clickStatusChart);
                    clickStatusChart = false;
                } else {
                    graph.setDataClickable(clickStatusChart);
                    clickStatusChart = true;
                }
            }
        });

        //chartSetting.getItems().setAll(Acc, settingX, settingY, settingZ,settingClickData, settingGPSData, settingAcc, settingForceZeroInRange);
        chartSetting.getItems().setAll(acc,rotation,gps,magnetic, settingClickData, settingForceZeroInRange);

        //******************************BUTTONS - STATIC VIEW********************************//
        readButton.setOnAction(e -> {
            if(cinematicMode) {
                System.out.println("Trying to read from chip");
                if (readFromChip) {
                    if (statusChart && !timelineIsStopped) {
                        timeline.stop();
                        timelineIsStopped=true;
                    }
                    else if(statusChart && timelineIsStopped) {
                        timeline.play();
                        timelineIsStopped=false;
                        //System.out.println("Graph reset");
                        //resetGraph();
                    }else {
                        readChipCinematic();
                    }
                    //statusChart=true;
                /*
                initAnimatedGraph();
                this.startAnimatedTimeline("chip");
                */
                } else {
                    if(statusChart && !timelineIsFinished && !timelineIsStopped){
                        System.out.println("Stopping timeline");
                        stopAnimatedTimeline();
                        timelineIsStopped = true;
                    }
                    else if (statusChart && timelineIsFinished && timelineIteration + 1 == dataListX.size()) {
                        System.out.println("Resetting chart");
                        resetGraph();
                        readFileCinematic();
                    } else if (!timelineIsFinished && statusChart || (timelineIteration + 1 != dataListX.size() && timelineIsFinished)) {
                        //timeline.play();
                        System.out.println("Starting timeline");
                        timelineIsStopped=false;
                        startAnimatedTimeline("file");
                    } else {
                        readFileCinematic();
                    }
                }
                statusChart = true;

                settingX.setDisable(false);
                settingY.setDisable(false);
                settingZ.setDisable(false);
                settingClickData.setDisable(false);
                saveButton.setDisable(false);

                System.out.println("readStatic");
            }
            else{
                //Logic for determining whether to read from chip or from disk
                if (readFromChip) {
                    if (statusChart) {
                        System.out.println("Graph Reset");
                        resetGraph();
                    }
                    Task<Void> readTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            //Fetching data for table
                            System.out.println("Reading data");

                            if(settingXIsActive) {
                                System.out.println("reading x...");
                                data.setData(1,10);
                                data.readFile(null, true, progressList);
                                settingXHasBeenActivated = true;
                            }
                            if(settingYIsActive){
                                data.setData(1,11);
                                data.readFile(null, true, progressList);
                                settingYHasBeenActivated = true;
                            }
                            if(settingZIsActive){
                                data.setData(1,12);
                                data.readFile(null, true, progressList);
                                settingZHasBeenActivated = true;
                            }
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    initStaticGraph();
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

                    statusChart = true;

                    settingX.setDisable(false);
                    settingY.setDisable(false);
                    settingZ.setDisable(false);
                    settingClickData.setDisable(false);
                    saveButton.setDisable(false);

                    System.out.println("readStatic");
                } else if (!readFromChip) {
                    if (statusChart) {
                        System.out.println("Graph reset");
                        resetGraph();
                    }
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
                                if(settingXIsActive) {
                                    System.out.println("Reading x");
                                    data.setData(1,10);
                                    data.readFile(file, false, progressList);
                                    settingXHasBeenActivated = true;
                                }
                                if(settingYIsActive){
                                    System.out.println("Reading y");
                                    data.setData(1,11);
                                    data.readFile(file, false, progressList);
                                    settingYHasBeenActivated = true;
                                }
                                if(settingZIsActive){
                                    System.out.println("Reading z");
                                    data.setData(1,12);
                                    data.readFile(file, false, progressList);
                                    settingZHasBeenActivated = true;
                                }
                                System.out.println("Read successful");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        initStaticGraph();
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

                        statusChart = true;

                        settingX.setDisable(false);
                        settingY.setDisable(false);
                        settingZ.setDisable(false);
                        settingClickData.setDisable(false);
                        saveButton.setDisable(false);

                        System.out.println("readStatic");
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

        });


        resetButton.setOnAction(e -> {
            System.out.println("resetStatic");
            resetGraph();
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

    //**********************************************STATIC GRAPH*****************************************************//
    public void initStaticGraph() {


        dataListX = data.getDataX();
        table.setItems(dataListX);
        table.getColumns().setAll(xCol, yCol);
        //Setup of series

        if(settingXHasBeenActivated) {
            seriesX = data.getDataSeriesX();
            graph = new Graph2D(lineChart, seriesX, xAxis, yAxis, "readStatic");
            graph.setup("x");
            if(!settingYHasBeenActivated && !settingZHasBeenActivated) {
                System.out.println("Only x will be printed");
            }else if(settingYHasBeenActivated && !settingZHasBeenActivated){
                data.setupY();
                seriesY = data.getDataSeriesY();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;
            }else if(!settingYHasBeenActivated && settingZHasBeenActivated){
                data.setupZ();
                seriesZ = data.getDataSeriesZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            }else if(settingYHasBeenActivated && settingZHasBeenActivated){
                data.setupY();
                seriesY = data.getDataSeriesY();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;

                data.setupZ();
                seriesZ = data.getDataSeriesZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            }
        }
        else if(settingYHasBeenActivated) {
            seriesY = data.getDataSeriesY();
            graph = new Graph2D(lineChart, seriesY, xAxis, yAxis, "readStatic");
            graph.setup("y");
            if(!settingXHasBeenActivated && !settingZHasBeenActivated) {
                System.out.println("Only y will be printed");
            }else if(settingXHasBeenActivated && !settingZHasBeenActivated){
                data.setupX();
                seriesX = data.getDataSeriesX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;
            }else if(!settingXHasBeenActivated && settingZHasBeenActivated){
                data.setupZ();
                seriesZ = data.getDataSeriesZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            }else if(settingXHasBeenActivated && settingZHasBeenActivated){
                data.setupX();
                seriesX = data.getDataSeriesX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;

                data.setupZ();
                seriesZ = data.getDataSeriesZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            }
        }
        else if(settingYHasBeenActivated) {
            seriesY = data.getDataSeriesY();
            graph = new Graph2D(lineChart, seriesY, xAxis, yAxis, "readStatic");
            graph.setup("z");
            if(!settingXHasBeenActivated && !settingYHasBeenActivated) {
                System.out.println("Only y will be printed");
            }else if(settingXHasBeenActivated && !settingYHasBeenActivated){
                data.setupX();
                seriesX = data.getDataSeriesX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;
            }else if(!settingXHasBeenActivated && settingYHasBeenActivated){
                data.setupY();
                seriesY = data.getDataSeriesY();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;
            }else if(settingXHasBeenActivated && settingYHasBeenActivated){
                data.setupX();
                seriesX = data.getDataSeriesX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;

                data.setupY();
                seriesY = data.getDataSeriesY();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;
            }
        }
        else{

            seriesX = data.getDataSeriesX();
            graph = new Graph2D(lineChart, seriesX, xAxis, yAxis, "readStatic");
            graph.setup("x");

        }
    }

    public void resetGraph() {
        if(timeline != null && statusChart && !timelineIsFinished && !timelineIsStopped) {
            System.out.println("Stopping timeline");
            timeline.stop();
        }

        if(settingClickData.selectedProperty().get()){ //resetting ClickableData
            graph.setDataClickable(clickStatusChart);
            clickStatusChart=false;
            settingClickData.setSelected(false);
        }

        if(settingX.selectedProperty().getValue()){
            lineChart.getData().removeAll(seriesX);
            seriesX= new XYChart.Series<>();
            //settingXIsActive=false;
            //settingX.setText("Show x");
        }else{
            settingXIsActive=true; //Due to it being default
            settingX.setSelected(true);
        }
        if(settingY.selectedProperty().getValue()){
            lineChart.getData().removeAll(seriesY);
            seriesY= new XYChart.Series<>();
            settingY.setSelected(false);
            settingYIsActive=false;
        }
        if(settingZ.selectedProperty().getValue()){
            lineChart.getData().removeAll(seriesZ);
            seriesZ= new XYChart.Series<>();
            settingZ.setSelected(false);
            settingZIsActive=false;
        }


        settingClickData.setDisable(true);
        settingGPSData.setSelected(false);
        settingGPSData.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/gpsOff.png")));

        settingXHasBeenActivated=false;
        settingYHasBeenActivated=false;
        settingZHasBeenActivated=false;

        /*
        settingX.setDisable(true);
        settingY.setDisable(true);
        settingZ.setDisable(true);
        */
        timelineIsFinished=false;
        timelineIsStopped=false;
        timelineIteration=0;
        timelineSlider.setVisible(false);
        progressBar.setVisible(false);
        progressLabel.setVisible(false);
        saveButton.setDisable(true);

        data.resetData();
        statusChart = false;
    }

    //***************************************************************************************************************//

    public void setupTable(){
        //*************SETUP OF STATIC TABLEVIEW***************//
        //Setup of static TableView.
        table.setEditable(true);
        //X-column
        xCol = new TableColumn("Time, <s>");
        TableCol xColStatic = new TableCol(xCol, "x");
        //Y-column
        yCol=  new TableColumn("Acc, <m/s^2>");
        TableCol yColStatic = new TableCol(yCol, "y");
        //****************************************************//
    }

    //********************************************ANIMATED GRAPH*****************************************************//

    public void initAnimatedGraph() {
        //Fetching data for table
        dataListX = data.getDataX();

        table.setItems(dataListX);
        table.getColumns().setAll(xCol, yCol);


        if(settingXHasBeenActivated) {
            graph = new Graph2D(lineChart, seriesX, xAxis, yAxis, "readStatic");
            graph.setup("x");
            if(!settingYHasBeenActivated && !settingZHasBeenActivated) {
                System.out.println("Only x will be printed");
            }else if(settingYHasBeenActivated && !settingZHasBeenActivated){
                data.setupY();
                dataListY = data.getDataY();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;
            }else if(!settingYHasBeenActivated && settingZHasBeenActivated){
                data.setupZ();
                dataListZ = data.getDataZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            }else if(settingYHasBeenActivated && settingZHasBeenActivated){
                data.setupY();
                dataListY = data.getDataY();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;

                data.setupZ();
                dataListZ = data.getDataZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            }
        }
        else if(settingYHasBeenActivated) {
            graph = new Graph2D(lineChart, seriesY, xAxis, yAxis, "readStatic");
            graph.setup("y");
            if(!settingXHasBeenActivated && !settingZHasBeenActivated) {
                System.out.println("Only y will be printed");
            }else if(settingXHasBeenActivated && !settingZHasBeenActivated){
                data.setupX();
                dataListX = data.getDataX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;
            }else if(!settingXHasBeenActivated && settingZHasBeenActivated){
                data.setupZ();
                dataListZ = data.getDataZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            }else if(settingXHasBeenActivated && settingZHasBeenActivated){
                data.setupX();
                dataListX = data.getDataX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;

                data.setupZ();
                dataListZ = data.getDataZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            }
        }
        else if(settingYHasBeenActivated) {
            graph = new Graph2D(lineChart, seriesY, xAxis, yAxis, "readStatic");
            graph.setup("z");
            if(!settingXHasBeenActivated && !settingYHasBeenActivated) {
                System.out.println("Only y will be printed");
            }else if(settingXHasBeenActivated && !settingYHasBeenActivated){
                data.setupX();
                dataListX = data.getDataX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;
            }else if(!settingXHasBeenActivated && settingYHasBeenActivated){
                data.setupY();
                dataListZ = data.getDataZ();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;
            }else if(settingXHasBeenActivated && settingYHasBeenActivated){
                data.setupX();
                dataListX = data.getDataX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;

                data.setupY();
                dataListZ = data.getDataZ();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;
            }
        }
        else{
            seriesX = data.getDataSeriesX();
            graph = new Graph2D(lineChart, seriesX, xAxis, yAxis, "readAnimated");
            graph.setup("x");

        }
    }
    //***************************************************************************************************************//

    //******************************************TIMELINE HANDLING****************************************************//

    public void timelineAnimated(String str) {
        //****************** EVENT HANDLER FOR KEYFRAME ***************************//
        EventHandler onFinishedFile = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                if(settingXIsActive) {
                    XYChart.Data<Number, Number> datapoint = new XYChart.Data<>(dataListX.get(timelineIteration).getX(), dataListX.get(timelineIteration).getY());
                    seriesX.getData().add(datapoint);
                }
                if(settingYIsActive){
                    XYChart.Data<Number, Number> datapoint = new XYChart.Data<>(dataListY.get(timelineIteration).getX(), dataListY.get(timelineIteration).getY());
                    seriesY.getData().add(datapoint);
                }
                if(settingZIsActive){
                    XYChart.Data<Number, Number> datapoint = new XYChart.Data<>(dataListZ.get(timelineIteration).getX(), dataListZ.get(timelineIteration).getY());
                    seriesZ.getData().add(datapoint);
                }

                double progress = Math.round(((double) timelineIteration / dataListX  .size()) * 100);
                System.out.println("Progress: " + progress);
                System.out.println("timeline Iteration: " + timelineIteration);
                timelineSlider.setValue(progress);
                timelineIteration++;
            }
        };

        EventHandler onFinishedChip = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                data.readContinously();
                XYChart.Series<Number, Number> series = data.getDataSeriesX();

                for (int i = 0; i < series.getData().size(); i++) {
                    //seriesX.getData().setAll(series.getData().get(i)); //TODO, CLEAR UP
                    if(series.getData().size()!=0) {
                        //seriesX = series;
                    }
                    if(settingXIsActive) {
                        seriesX=data.getDataSeriesX();
                    }

                    if(settingYIsActive){
                        seriesY=data.getDataSeriesY();
                    }
                    if(settingZIsActive){
                        seriesZ=data.getDataSeriesZ();
                    }
                }

                /*
                try {

                    //seriesX.getData().addAll(datapoint);
                }catch(Exception e){
                    System.out.println("Error");
                }
                */

            }
        };
        //*************************************************************************//


        //Creating a timeline for updating the graph
        timeline = new Timeline();
        if (str == "file") {
            System.out.println("entered file");
            timeline.setCycleCount(dataListX.size()- timelineIteration); //Cycles of the timeline finishing according to the size of the series //
            //timeline.setAutoReverse(true);
            timelineIsFinished = false;
            timeline.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    timelineIsFinished = true;
                    readButton.setStyle(".play-button");
                    System.out.println("Done!");
                }

            });
            Duration duration = Duration.millis(10);
            //I don't know how to do actionhandling for keyframe with lambda expression
            keyFrameAnimated = new KeyFrame(duration, onFinishedFile);
            timeline.getKeyFrames().add(keyFrameAnimated);
        } else if (str == "chip") {
            System.out.println("Reading chip");
            timeline.setCycleCount(Timeline.INDEFINITE);
            Duration duration = Duration.millis(10);
            keyFrameAnimated = new KeyFrame(duration, onFinishedChip);
            timeline.getKeyFrames().add(keyFrameAnimated);
        }
    }

    public void startAnimatedTimeline(String str) {
        readButton.getStyleClass().set(1,"pause-button");
        timelineAnimated(str);
        timelineSlider.setVisible(true);
        timeline.play();
    }

    public void stopAnimatedTimeline() {
        readButton.getStyleClass().set(1,"play-button");
        System.out.println("Timeline stopped");
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
                    if(settingXIsActive) {
                        System.out.println("Reading x");
                        data.setData(1,10);
                        data.readContinouslyFromFile(file, false, progressList);
                        settingXHasBeenActivated = true;
                    }
                    if(settingYIsActive){
                        System.out.println("Reading y");
                        data.setData(1,11);
                        data.readContinouslyFromFile(file, false, progressList);
                        settingYHasBeenActivated = true;
                    }
                    if(settingZIsActive){
                        System.out.println("Reading z");
                        data.setData(1,12);
                        data.readContinouslyFromFile(file,  false, progressList);
                        settingZHasBeenActivated = true;
                    }
                    System.out.println("Read successful");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            initAnimatedGraph();
                            startAnimatedTimeline("file");
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
        progressBar.setVisible(false);
        progressLabel.setVisible(false);
        Thread th = new Thread(readTask);
        System.out.println("Starting new readThread: from chip");
        th.start();
        initAnimatedGraph();
        startAnimatedTimeline("chip");
    }
    //***************************************************************************************************************//

    public void setupSlider(){
        //**********************SLIDER SETUP*********************//
        timelineSlider.setVisible(false);
        timelineSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //progressBarStaticView.setProgress(newValue.doubleValue()/timelineSlider.getMax()); //sets the progressbar's progress
                double progress = newValue.doubleValue() / 100;
                int max = timeline.getCycleCount() - 1;
                int newTimelineIteration;
                if (timelineIsFinished) {
                    newTimelineIteration = (int) Math.round(progress * max);
                    if (newTimelineIteration > timelineIteration) {
                        lineChart.getData().removeAll(seriesX);
                        for (int i = timelineIteration; i < timelineIteration; i++) {
                            XYChart.Data<Number, Number> datapoint = new XYChart.Data<Number, Number>(dataListX.get(i).getX(), dataListX.get(i).getY());
                            seriesX.getData().add(datapoint);
                        }
                        lineChart.getData().add(seriesX);
                    } else if (newTimelineIteration < timelineIteration) {
                        lineChart.getData().removeAll(seriesX);
                        seriesX.getData().remove(newTimelineIteration, timelineIteration);
                        lineChart.getData().add(seriesX);

                    }
                    timelineIteration = newTimelineIteration;
                    System.out.println("timelineIteration new: " + timelineIteration);
                }

            }
        });
        //*******************************************************//
    }
}
