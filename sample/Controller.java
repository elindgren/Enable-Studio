package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.ResourceBundle;
import org.gillius.jfxutils.JFXUtil.*;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;
import org.gillius.jfxutils.chart.StableTicksAxis;
import org.gillius.jfxutils.chart.XYChartInfo;
import org.gillius.jfxutils.tab.TabUtil;

public class Controller implements Initializable {
    //********************************************************************//
    ReadSerialPort rp;
    private Data data;

    //Below are used by various progressbars.
    private ObservableList<Integer> progressList;
    private ObservableList<Integer> statusList;
    private int[] rowsToRead;


    @FXML
    private TabPane tabPane;
    //*********************MENU BAR************************//
    @FXML
    private MenuItem menuClose;

    //*********************STATIC VIEW*********************//
    private boolean statusResStatic = false; //TODO Change data so that it is the resulting data
    private int statusPan = 1;
    private boolean clickStatusStaticView=false;
    private int readModeStaticView = 1; //1 corresponds to reading a file from harddrive, 0 corresponds to reading from the chip
    private ChartPanManager panManagerStaticView;
    private Task<Void> readTask;

    @FXML
    private Tab tabStaticView;

    //TableView - Static View
    @FXML
    private TableView tableStatic;
    private TableColumn xDataStaticCol;
    private TableColumn yDataStaticCol;

    //Buttons - Static View
    @FXML
    private Button readStatic;
    @FXML
    private Button resetStatic;
    @FXML
    private Button toggleClickableStaticView;

    //ProgressBar & Status - Static View
    @FXML
    private Label progressLabelStaticView;
    @FXML
    private ProgressBar progressBarStaticView;
    @FXML
    private Circle statusStaticView;


    //LineChart - Static View
    @FXML
    private LineChart lineChartStatic;
    @FXML
    private StableTicksAxis xAxisStatic;
    @FXML
    private StableTicksAxis yAxisStatic;
    Graph2D staticGraph;
    //Static series
    private XYChart.Series<Number,Number> seriesDataStatic = new XYChart.Series<Number,Number>();
    private ObservableList<DataPoint2D> dataStatic = FXCollections.observableArrayList();
    //******************************************************//

    //*********************ANIMATED VIEW*********************//
    private boolean statusResAnimated = false;
    private boolean readFromChip=false;
    KeyFrame keyFrameAnimated;
    ChartPanManager panManagerAnimatedView;
    private boolean timelineIsFinished;
    private Timeline timeline;
    private int timelineIteration=0;

    @FXML
    private Slider timelineSlider;


    @FXML
    private Tab tabAnimatedView;

    //TableView - Animated View
    @FXML
    private TableView tableAnimated;
    private TableColumn xDataAnimatedCol;
    private TableColumn yDataAnimatedCol;

    //Buttons - Animated View
    @FXML
    private Button stopAnimated;
    @FXML
    private Button readAnimated;
    @FXML
    private Button resetAnimated;
    @FXML
    private ToggleButton toggleReadChip;
    @FXML
    private Button saveButton;

    //LineChart - Animated View
    @FXML
    private LineChart lineChartAnimated;
    @FXML
    private StableTicksAxis xAxisAnimated;
    @FXML
    private StableTicksAxis yAxisAnimated;
    Graph2D animatedGraph;
    //Animated series
    private XYChart.Series<Number,Number> seriesDataAnimated = new XYChart.Series<Number,Number>();
    private ObservableList<DataPoint2D>  dataAnimated = FXCollections.observableArrayList();
    //******************************************************//
    //********************************************************************//

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources) {
        //*****Checks if all the imported fx:id's are declared in FXML-file*****//
        assert menuClose != null : "fx:id=\"menuClose\" was not injected: check your FXML file";
        assert readStatic != null : "fx:id=\"readStatic\" was not injected: check your FXML file";
        assert resetStatic != null : "fx:id=\"resetStatic\" was not injected: check your FXML file";
        assert readAnimated != null : "fx:id=\"readAnimated\" was not injected: check your FXML file";
        assert stopAnimated != null : "fx:id=\"stopAnimated\" was not injected: check your FXML file";
        assert resetAnimated != null : "fx:id=\"resetAnimated\" was not injected: check your FXML file";
        //********************************************************************//

        rp = new ReadSerialPort();
        data = new Data(rp);
        //InitializeStaticGraph initStatic = new InitializeStaticGraph( data,  tableStatic,  yDataStaticCol,  xDataStaticCol,  lineChartStatic,  xAxisStatic,  yAxisStatic,  staticGraph, panManagerStaticView, seriesStatic );

        //***********************************************CHIP STATUS SETUP************************************//
        statusList = rp.getStatusList();
        statusList.addListener(new ListChangeListener<Integer>() {
            @Override
            public void onChanged(Change<? extends Integer> c) {
                if(statusList.get(0).intValue() == 0 ){
                    statusStaticView.setFill(Color.FIREBRICK);
                }
                else if(statusList.get(0).intValue() ==1){
                    statusStaticView.setFill(Color.FORESTGREEN);
                }
            }
        });
        //Setup of default mode. If chip is connected, the program will load data from the chip. If not, it will default to load from a file on
        //the computer

        if(rp.isConnected()){
            statusStaticView.setFill(Color.FORESTGREEN);
            rp.setupPorts();
            System.out.println("Chip available. Defualt to reading from chip.");
            readModeStaticView = 0;
        }
        else{
            statusStaticView.setFill(Color.FIREBRICK);
            System.out.println("Chip unavailable. Defualt to reading from harddrive.");
            readModeStaticView = 1;
        }
        //*****************************************************************************************************//

        //**********************************************TAB SETUP**********************************************//

        //tabs.makeDroppable(tabPane);
        //tabs.makeDraggable(tabExperimentalView); <- TODO NullpointerException? Why?
        //*****************************************************************************************************//

        //***********************************************PROGRESS BAR SETUP************************************//
        //Setup of progressBar
        progressList = rp.getProgressList();
        //Setup of progresslist, as to not have set() crash.
        progressList.add(0,0);
        progressList.add(1,1);
        //Add functionality for it to say "Done" when done TODO

        progressList.addListener(new ListChangeListener<Integer>() {
                                     @Override
                                     public void onChanged(Change<? extends Integer> c) {
                                         double progress=progressList.get(0).doubleValue()/progressList.get(1).doubleValue();
                                         progressBarStaticView.setProgress(progress);
                                     }
                                 }
        );

        //*****************************************************************************************************//


        //**********************************************BUTTON SETUP*******************************************//
        menuClose.setOnAction(e -> {
            System.out.println("MenuClose");
        });

        //*****************************BUTTONS - Animated VIEW*******************************//
        //******Toggle: Chip or Computer***********//
        toggleReadChip.setOnAction(e -> {
            if(readFromChip){
                readFromChip=false;
            }
            else{
                readFromChip=true;
            }
        });
        //*****************************************//
        readAnimated.setOnAction(e -> {
            if(readFromChip) {
                if (statusResAnimated) {
                    System.out.println("Graph reset");
                    resetStaticGraph();
                }
                readChipCinematic();
                /*
                initAnimatedGraph();
                this.startAnimatedTimeline("chip");
                */
            }
            else{
                if (statusResAnimated && timelineIsFinished && timelineIteration+1 == dataAnimated.size()) {
                    System.out.println("Graph reset");
                    resetStaticGraph();
                    readFileCinematic();
                }
                else if (!timelineIsFinished && statusResAnimated || (timelineIteration+1 != dataAnimated.size() && timelineIsFinished)){
                    //timeline.play();
                    startAnimatedTimeline("file");
                }
                else{
                    readFileCinematic();
                }
            }
            statusResAnimated=true;
            System.out.println("readAnimated");
        });
        stopAnimated.setOnAction(e -> {
            this.stopAnimatedTimeline();
            System.out.println("stopAnimated");
        });
        resetAnimated.setOnAction(e -> {
            timeline.getKeyFrames().remove(keyFrameAnimated);
            statusResAnimated =false;
            resetAnimatedGraph();
            System.out.println("resetAnimated");
        });

        saveButton.setOnAction(e -> {
            FileChooser fileChoose = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("txt File(*.txt)", "*.txt");
            fileChoose.getExtensionFilters().add(extensionFilter);

            File file = fileChoose.showOpenDialog(tabPane.getScene().getWindow());
            if (file != null) {
                data.writeFile(file,progressList, readFromChip);
            }

        });
        //***********************************************************************************//

        //******************************BUTTONS - STATIC VIEW********************************//
        readStatic.setOnAction(e ->{
                //Logic for determining whether to read from chip or from disk
                if(readModeStaticView == 0) {
                    if(statusResStatic){
                        System.out.println("Graph Reset");
                        resetStaticGraph();
                    }
                    Task<Void> readTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            //Fetching data for table
                            System.out.println("Reading data");
                            data.readFile(null,"x",true, progressList);
                            Platform.runLater(new Runnable() {
                                @Override public void run(){
                                    initStaticGraph();
                                    progressLabelStaticView.setText("Done");
                                }
                            });
                            return null;
                        }
                    };
                    Thread th = new Thread(readTask);
                    System.out.println("Starting new readThread");
                    progressBarStaticView.setVisible(true);
                    progressLabelStaticView.setVisible(true);
                    th.start();
                }
                else if(readModeStaticView ==1) {
                        if (statusResStatic) {
                            System.out.println("Graph reset");
                            resetStaticGraph();
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
                                    data.readFile(file, "x", false, progressList);
                                    System.out.println("Read successful");
                                    Platform.runLater(new Runnable() {
                                        @Override public void run(){
                                            initStaticGraph();
                                            progressLabelStaticView.setText("Done");
                                        }
                                    });
                                    return null;
                                }
                            };
                            Thread th = new Thread(readTask);
                            //progressBarStaticView.setProgress(0);
                            System.out.println("Starting new readThread");
                            progressBarStaticView.setVisible(true);
                            progressLabelStaticView.setVisible(true);
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

                }
                else{
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
            statusResStatic=true;
            System.out.println("readStatic");
        });


        resetStatic.setOnAction(e -> {
            System.out.println("resetStatic");
            statusResStatic=false;
            resetStaticGraph();
        });
        toggleClickableStaticView.setOnAction(e ->{
            if(clickStatusStaticView){
                staticGraph.setDataClickable(clickStatusStaticView);
                clickStatusStaticView=false;
            }
            else{
                staticGraph.setDataClickable(clickStatusStaticView);
                clickStatusStaticView=true;
            }
        });
        /*
        togglePanStaticView.setOnAction(e ->{ //Doesn't work properly TODO
           if(statusPan==1){
               panManagerStaticView.stop();
               statusPan=0;
           }
           else{
               panManagerStaticView.start();
           }
        });
        */

        //***********************************************************************************//
        //*****************************************************************************************************//

        //**********************SLIDER SETUP*********************//
        timelineSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //progressBarStaticView.setProgress(newValue.doubleValue()/timelineSlider.getMax()); //sets the progressbar's progress
                double progress =newValue.doubleValue()/100;
                int max = timeline.getCycleCount()-1;
                int newTimelineIteration;
                if(timelineIsFinished) {
                    newTimelineIteration = (int) Math.round(progress * max);
                    if(newTimelineIteration > timelineIteration){
                        lineChartAnimated.getData().removeAll(seriesDataAnimated);
                        for(int i=timelineIteration; i<timelineIteration; i++){
                            XYChart.Data<Number,Number> datapoint = new XYChart.Data<Number,Number>(dataAnimated.get(i).getX(), dataAnimated.get(i).getY());
                            seriesDataAnimated.getData().add(datapoint);
                        }
                        lineChartAnimated.getData().add(seriesDataAnimated);
                    }
                    else if(newTimelineIteration < timelineIteration){
                        lineChartAnimated.getData().removeAll(seriesDataAnimated);
                        seriesDataAnimated.getData().remove(newTimelineIteration,timelineIteration);
                        lineChartAnimated.getData().add(seriesDataAnimated);

                    }
                    timelineIteration=newTimelineIteration;
                    System.out.println("timelineIteration new: " + timelineIteration);
                }

            }
        });


        //*******************************************************//
        //*************SETUP OF STATIC TABLEVIEW***************//
        //Setup of static TableView.
        tableStatic.setEditable(true);
        //X-column
        xDataStaticCol = new TableColumn("Time, <s>");
        TableCol xColStatic = new TableCol(xDataStaticCol,"x");
        //Y-column
        yDataStaticCol = new TableColumn("Acc, <m/s^2>");
        TableCol yColStatic = new TableCol(yDataStaticCol,"y");
        //****************************************************//

        //***********SETUP OF ANIMATED TALBEVIEW**************//
        //Setup of Animated TableView.
        tableAnimated.setEditable(true);
        //X-column
        xDataAnimatedCol = new TableColumn("Time, <s>");
        TableCol xColAnimated = new TableCol(xDataAnimatedCol,"x");
        //Y-column
        yDataAnimatedCol = new TableColumn("Acc, <m/s^2>");
        TableCol yColAnimated = new TableCol(yDataAnimatedCol,"y");
        //****************************************************//

    }

    private void updateTableViewAnimated(){
        tableAnimated.setItems(data.getDataAnimated());
    }

    //**********************************************STATIC GRAPH*****************************************************//
    public void initStaticGraph(){
        dataStatic = data.getDataStatic();
        tableStatic.setItems(dataStatic);
        tableStatic.getColumns().setAll(xDataStaticCol,yDataStaticCol);
        //Setup of series
        seriesDataStatic=data.getStaticDataSeries();
        staticGraph = new Graph2D(lineChartStatic, seriesDataStatic, xAxisStatic, yAxisStatic,"readStatic", panManagerStaticView);
        //lineChartStatic.getData().add(seriesStatic);
        staticGraph.setup();
    }

    public void resetStaticGraph(){
        data.resetDataStatic();
        lineChartStatic.getData().removeAll(seriesDataStatic);
        statusResStatic = false;
        progressBarStaticView.setVisible(false);
        progressLabelStaticView.setVisible(false);
    }

    //***************************************************************************************************************//


    //********************************************ANIMATED GRAPH*****************************************************//

    public void initAnimatedGraph(){
        //Fetching data for table
        dataAnimated=data.getDataAnimated();
        tableAnimated.setItems(dataAnimated);
        /*
        dataAnimated.addListener(new ListChangeListener<DataPoint2D>() {
            @Override
            public void onChanged(Change<? extends DataPoint2D> c) {
                updateTableViewAnimated();
            }
        });
        */
        //seriesDataAnimated=data.getStaticDataSeries();
        tableAnimated.getColumns().setAll(xDataAnimatedCol,yDataAnimatedCol);

        animatedGraph = new Graph2D(lineChartAnimated, seriesDataAnimated, xAxisAnimated, yAxisAnimated,"readAnimated", panManagerAnimatedView);
        animatedGraph.setup();
    }

    public void resetAnimatedGraph(){
        data.resetDataAnimated();
        lineChartAnimated.getData().removeAll(seriesDataAnimated);
        statusResAnimated = false;
    }
    //***************************************************************************************************************//

    //******************************************TIMELINE HANDLING****************************************************//

    public void timelineAnimated(String str) {
        //****************** EVENT HANDLER FOR KEYFRAME ***************************//
        EventHandler onFinishedFile = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                XYChart.Data<Number,Number> datapoint = new XYChart.Data<>(dataAnimated.get(timelineIteration).getX(),dataAnimated.get(timelineIteration).getY());
                seriesDataAnimated.getData().add(datapoint);
                double progress = Math.round(((double)timelineIteration/dataAnimated.size())*100);
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
                XYChart.Series<Number,Number> series= data.getAnimatedDataSeries();
                for(int i=0; i<series.getData().size(); i++){
                    seriesDataAnimated.getData().add(series.getData().get(i)); //TODO, CLEAR UP
                }

            }
        };
        //*************************************************************************//



        //Creating a timeline for updating the graph
        timeline = new Timeline();
        if(str =="file") {
            System.out.println("entered file");
            timeline.setCycleCount(dataAnimated.size() - timelineIteration); //Cycles of the timeline finishing according to the size of the series
            //timeline.setAutoReverse(true);
            timelineIsFinished = false;
            timeline.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    timelineIsFinished = true;
                    System.out.println("Done!");
                }

            });
            Duration duration = Duration.millis(10);
            //I don't know how to do actionhandling for keyframe with lambda expression
            keyFrameAnimated = new KeyFrame(duration, onFinishedFile);
            timeline.getKeyFrames().add(keyFrameAnimated);
        }
        else if(str=="chip"){
            System.out.println("entered chip");
            timeline.setCycleCount(Timeline.INDEFINITE);
            Duration duration = Duration.millis(10);
            keyFrameAnimated = new KeyFrame(duration, onFinishedChip);
            timeline.getKeyFrames().add(keyFrameAnimated);
        }


    }

    public void startAnimatedTimeline(String str){
        timelineAnimated(str);
        timeline.play();
    }

    public void stopAnimatedTimeline(){
        timeline.stop();
    }

    public void readFileCinematic(){
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

    public void readChipCinematic(){
        readTask =  new Task<Void>(){
            @Override protected Void call(){
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
        initAnimatedGraph();
        startAnimatedTimeline("chip");
    }

    //*****VERSION 2, if VERSION ABOVE FAILS******//

    public void readChipCinematic2(){
        System.out.println("Trying to use ContinousToDoubleMatrix");
        rp.setBuffer(6);
        ContinousToDoubleMatrix cont = new ContinousToDoubleMatrix(rp);
        Thread th = new Thread(cont);
        System.out.println("Starting new ContinousToDoubleMatrix thread");
        th.start();
        initAnimatedGraph();
        startAnimatedTimeline("chip");
    }
    //***************************************************************************************************************//
}
