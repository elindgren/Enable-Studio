package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import java.net.URL;
import javafx.util.Duration;
import java.util.ResourceBundle;
import org.gillius.jfxutils.JFXUtil.*;
import org.gillius.jfxutils.chart.StableTicksAxis;

public class Controller implements Initializable {
    //********************************************************************//
    private Timeline timeline;
    private Data data;

    //*********************MENU BAR************************//
    @FXML
    private MenuItem menuClose;

    //*********************STATIC VIEW*********************//
    private int statusResStatic =0; //TODO Change data so that it is the resulting data
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

    //LineChart - Static View
    @FXML
    private LineChart lineChartStatic;
    @FXML
    private NumberAxis xAxisStatic;
    @FXML
    private NumberAxis yAxisStatic;
    //Static series
    private XYChart.Series<Number,Number> seriesStatic;
    //******************************************************//

    //*********************ANIMATED VIEW*********************//
    private int statusResAnimated = 0;

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

    //LineChart - Animated View
    @FXML
    private LineChart lineChartAnimated;
    @FXML
    private NumberAxis xAxisAnimated;
    @FXML
    private NumberAxis yAxisAnimated;
    //Animated series
    private XYChart.Series<Number,Number> seriesAnimated;
    //******************************************************//


    //***************************EXPERIMENTAL****************//
    @FXML
    private Button readExperimental;
    @FXML
    private Button resetExperimental;
    @FXML
    private LineChart lineChartExperimental;
    //*******************************************************//
    //********************************************************************//

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources){
        //*****Checks if all the imported fx:id's are declared in FXML-file*****//
        assert menuClose != null : "fx:id=\"menuClose\" was not injected: check your FXML file";
        assert readStatic != null : "fx:id=\"readStatic\" was not injected: check your FXML file";
        assert resetStatic != null : "fx:id=\"resetStatic\" was not injected: check your FXML file";
        assert readAnimated != null : "fx:id=\"readAnimated\" was not injected: check your FXML file";
        assert stopAnimated != null : "fx:id=\"stopAnimated\" was not injected: check your FXML file";
        assert resetAnimated != null : "fx:id=\"resetAnimated\" was not injected: check your FXML file";
        //********************************************************************//

        ReadSerialPort rp = new ReadSerialPort();
        data = new Data(rp);

        //**********************************************BUTTON SETUP*******************************************//
        menuClose.setOnAction(e -> {
            System.out.println("MenuClose");
        });

        //*****************************BUTTONS - Animated VIEW*******************************//
        readAnimated.setOnAction(e -> {
            rp.setBuffer(6);
            ContinousToDoubleMatrix cont = new ContinousToDoubleMatrix(rp);
            Thread thread = new Thread(cont);
            thread.start();
            initAnimatedGraph();
            this.startAnimatedTimeline();
            System.out.println("readAnimated");
        });
        stopAnimated.setOnAction(e -> {
            this.stopAnimatedTimeline();
            System.out.println("stopAnimated");
        });
        resetAnimated.setOnAction(e -> {
            resetAnimatedGraph();
            System.out.println("resetAnimated");
        });
        //***********************************************************************************//

        //******************************BUTTONS - STATIC VIEW********************************//
        readStatic.setOnAction(e ->{
                if(statusResStatic == 0) {
                    initStaticGraph();
                    statusResStatic = 1;
                }
                else{
                    System.out.println("Graph reset");
                    resetStaticGraph();
                    initStaticGraph();

                }
            System.out.println("readStatic");
            });

        resetStatic.setOnAction(e -> {
            System.out.println("resetStatic");
            resetStaticGraph();
        });
        //***********************************************************************************//
        //*****************************************************************************************************//


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



        //****************SETUP OF EXPERIMENTAL VIEW*************//
        readExperimental.setOnAction(e -> {
            XYChart.Series<Number,Number> seriesExperimental = new XYChart.Series<Number,Number>();
            seriesExperimental = data.getStaticDataAcc("x");
            seriesExperimental.setName("Static Series - Experimental View");

            StableTicksAxis x = new StableTicksAxis();
            StableTicksAxis y = new StableTicksAxis();


            Graph2D staticGraph = new Graph2D(lineChartExperimental, seriesExperimental, x, y,"readExperimental");
            lineChartExperimental.getData().add(seriesExperimental);
        });
        //*******************************************************//
    }

    private void updateTableViewAnimated(){
        tableAnimated.setItems(data.getTableDataAnimated());
    }

    //**********************************************STATIC GRAPH*****************************************************//

    public void initStaticGraph(){
        //Fetching data for table
        tableStatic.setItems(data.getTableDataStatic());
        tableStatic.getColumns().setAll(xDataStaticCol,yDataStaticCol);

        //Setup of series
        seriesStatic = data.getStaticDataAcc("x");
        seriesStatic.setName("Static Series");

        Graph2D staticGraph = new Graph2D(lineChartStatic, seriesStatic, xAxisStatic, yAxisStatic,"readStatic");
        staticGraph.setup();
    }

    public void resetStaticGraph(){
        ObservableList<DataPoint2D> tableData = data.getTableDataStatic();
        tableData.remove(0,tableData.size());
        data.resetXYChartStatic();
    }
    //***************************************************************************************************************//

    //********************************************ANIMATED GRAPH*****************************************************//

    public void initAnimatedGraph(){

        //Fetching data for table
        ObservableList<DataPoint2D> series = data.getTableDataAnimated();
        tableAnimated.setItems(series);
        series.addListener(new ListChangeListener<DataPoint2D>() {
            @Override
            public void onChanged(Change<? extends DataPoint2D> c) {
                updateTableViewAnimated();
            }
        });
        tableAnimated.getColumns().setAll(xDataAnimatedCol,yDataAnimatedCol);

        seriesAnimated=data.getAnimatedAcc();
        seriesAnimated.setName("Animated Series");

        Graph2D animatedGraph = new Graph2D(lineChartAnimated, seriesAnimated, xAxisAnimated, yAxisAnimated,"readAnimated");
        animatedGraph.setup();
    }

    public void resetAnimatedGraph(){
        ObservableList<DataPoint2D> tableData = data.getTableDataAnimated();
        tableData.remove(0,tableData.size()); //Removes everything in the table
        data.resetXYChartAnimated();
    }
    //***************************************************************************************************************//

    //******************************************TIMELINE HANDLING****************************************************//

    public void timelineAnimated() {
        //Creating a timeline for updating the graph
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE); //Indefinite cycles of the timeline finishing
        timeline.setAutoReverse(true);
        //****************** EVENT HANDLER FOR KEYFRAME ***************************//
        EventHandler onFinished = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                //series.getData().add(new XYChart.Data<Number, Number>(secs, Math.random() * 10));
                XYChart.Series<Number,Number> series = data.getAnimatedAcc();
                for(int i = 0; i<series.getData().size(); i++) {
                    seriesAnimated.getData().add(series.getData().get(i)); //TODO Clear up, it's very convoluted. Decide if we want to retrieve a XYSeries or an XYData.

                }
            }
        };
        //*************************************************************************//

        Duration duration = Duration.millis(10);

        //I don't know how to do actionhandling for keyframe with lambda expression
        KeyFrame keyFrame = new KeyFrame(duration, onFinished);
        timeline.getKeyFrames().add(keyFrame);
    }

    public void startAnimatedTimeline(){
        timelineAnimated();
        timeline.play();
    }

    public void stopAnimatedTimeline(){
        timeline.stop();
    }

    //***************************************************************************************************************//
}
