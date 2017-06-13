package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * Created by Eric on 2017-04-11.
 */
public class AccGraph extends Application {
    Stage mainWindow;
    double[] data;

    public static void main(String[] args) {
        launch(args);
    }

    @Override public void start(Stage primaryStage){

        //**************************************INITIALIZATION OF STAGE & LAYOUT *******************************//

        mainWindow = primaryStage;


        mainWindow.setTitle("JANA-viewer");

        //Creating 2 gridpanes, one for each scene

        GridPane grid = new GridPane();

        //Setting alignements, gap between columns and padding before the borders for the grid
        grid.setAlignment(Pos.CENTER);

        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25,25,25,25)); //The padding in TOP, RIGHT, BOTTOM, LEFT

        //************************************INITIALIZATION OF MAINWINDOW COMPONENTS (GRAPHS ETC)****************//

        //defining the axes
        final NumberAxis statxAxis = new NumberAxis();
        final NumberAxis statyAxis = new NumberAxis();
        statxAxis.setLabel("time, <s>");
        statyAxis.setLabel("resulting acceleration, <m/s^2>");

        final NumberAxis dynxAxis = new NumberAxis();
        final NumberAxis dynyAxis = new NumberAxis();
        dynxAxis.setLabel("time, <s>");
        dynyAxis.setLabel("resulting acceleration, <m/s^2>");

        final NumberAxis statxyzxAxis = new NumberAxis();
        final NumberAxis statxyzyAxis = new NumberAxis();
        statxyzxAxis.setLabel("time, <s>");
        statxyzyAxis.setLabel("acceleration, <m/s^2>");

        //creating the charts
        final LineChart<Number,Number> statChart =
                new LineChart<Number,Number>(statxAxis,statyAxis);

        final LineChart<Number,Number> dynChart = new LineChart<Number,Number>(dynxAxis,dynyAxis);

        final LineChart<Number,Number> statxyzChart = new LineChart<Number,Number>(statxyzxAxis,statxyzyAxis);

        statChart.setTitle("Magnitude of resulting acceleration");
        dynChart.setTitle("Magnitude of resulting acceleration");
        statxyzChart.setTitle("Magnitude of xyz-acceleration");

        //defining a static series
        XYChart.Series statSeries = new XYChart.Series();
        statSeries.setName("Resulting acceleration, static");
        //defining a dynamic series
        XYChart.Series dynSeries = new XYChart.Series();
        dynSeries.setName("Resulting acceleration, dynamic");
        //defining statxyz-series
        XYChart.Series xstatxyzSeries = new XYChart.Series();
        xstatxyzSeries.setName("Acceleration, x");
        XYChart.Series ystatxyzSeries = new XYChart.Series();
        ystatxyzSeries.setName("Acceleration, y");
        XYChart.Series zstatxyzSeries = new XYChart.Series();
        zstatxyzSeries.setName("Acceleration, z");

        //*******************************POPULATING SERIES WITH DATA*******************************************//

        //populating the static series with data
        for(int i = 0; i <=10; i++) {
            statSeries.getData().add(new XYChart.Data(i,i));
        }
        statChart.getData().add(statSeries);

        //populating the dynamic series with data
        for(int i = 10; i<=30; i++){
            dynSeries.getData().add(new XYChart.Data(i,i));
        }
        dynChart.getData().add(dynSeries);

        //populating the static xyz-series with data
        for(int i = 10; i<20; i++ ){
            xstatxyzSeries.getData().add(new XYChart.Data(i-2,i-2));
            ystatxyzSeries.getData().add(new XYChart.Data(i-20,i-20));
            zstatxyzSeries.getData().add(new XYChart.Data(i+5,i+5));
        }
        statxyzChart.getData().addAll(xstatxyzSeries,ystatxyzSeries,zstatxyzSeries);

        //Setting dots to false
        statChart.setCreateSymbols(false);
        dynChart.setCreateSymbols(false);
        statxyzChart.setCreateSymbols(false);

        //Creating a button to switch between the 2 graphs

        Button graphSwitch = new Button("Dynamic");
        Button statxyzSwitch = new Button("Components");

        //Adding everything static to the static scene (scene1)
        grid.add(graphSwitch,1,1);
        grid.add(statxyzSwitch,2,1);
        grid.add(statChart,0, 1);
        //Creating the static chart-scene
        Scene scene1 = new Scene(grid,800,600);

        //Events for when pressing the buttons, scene1 -> scene2
        graphSwitch.setOnAction(e -> {
            if(graphSwitch.getText() == "Dynamic") {
                graphSwitch.setText("Static");

                grid.getChildren().remove(statChart);
                grid.add(dynChart, 0, 1);
            }
            else{
                graphSwitch.setText("Dynamic");

                grid.getChildren().remove(dynChart);
                grid.add(statChart,0,1);
                mainWindow.setScene(scene1);
            }
        });

        statxyzSwitch.setOnAction(e -> {
            if(statxyzSwitch.getText() == "Components"){
                statxyzSwitch.setText("Resultant");

                grid.getChildren().remove(statChart);
                grid.add(statxyzChart,0,1);
                mainWindow.setScene(scene1);
            }
            else{
                statxyzSwitch.setText("Components");

                grid.getChildren().remove(statxyzChart);
                grid.add(statChart,0,1);
                mainWindow.setScene(scene1);
            }
        });

        //Defualt static-graph view

        mainWindow.setScene(scene1);
        mainWindow.show();

        //**************************************** CLOSE PROGRAM *********************************************//
        //Adding a function for the user to make sure that they want to close the program (we will also be able
        //to save data if we want)
        mainWindow.setOnCloseRequest(e -> {
            e.consume();
            closeProgram();
        });

        //****************************************FUNCTIONALITY FOR DYNAMIC GRAPH*******************************//
        /*
        executor = Executors.newCachedThreadPool(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);
                return thread;
            }
        });

        AddToQueue addToQueue = new AddToQueue();
        executor.execute(addToQueue);
        //-- Prepare Timeline
        prepareTimeline();
        */

    }


    private void closeProgram(){
        Boolean answer = ConfirmBox.display("Close","Are you sure you want to close?");
        if(answer){ //If the user wants to exit the program exit, else do nothing
            mainWindow.close();
            System.out.println("File saved, output message when we have this functionality");
        }
    }

    //***************************************ADDING METHODS FOR ANIMATED CHART***********************************//

    /*
    private int refreshRate = 1000; //The delay between the adding of a new data point
    private static final int MAX_DATA_POINTS = 50; //The maximum amount of allowed datapoints.
    private int xSeriesData = 0;
    private XYChart.Series<Number,Number> series1 = new XYChart.Series<Number,Number>();
    private XYChart.Series<Number,Number> series2 = new XYChart.Series<Number,Number>();
    private XYChart.Series<Number,Number> series3 = new XYChart.Series<Number,Number>();
    private ExecutorService executor;
    private ConcurrentLinkedQueue<Number> dataQ1 = new ConcurrentLinkedQueue<Number>();
    private ConcurrentLinkedQueue<Number> dataQ2 = new ConcurrentLinkedQueue<Number>();
    private ConcurrentLinkedQueue<Number> dataQ3 = new ConcurrentLinkedQueue<Number>();

    private NumberAxis xAxis;

    private void init(Stage primaryStage, GridPane grid,Scene scene){
        xAxis = new NumberAxis(0, MAX_DATA_POINTS, MAX_DATA_POINTS/10);
        xAxis.setForceZeroInRange(false);
        xAxis.setAutoRanging(false);
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);

        NumberAxis yAxis = new NumberAxis();

        //Create a linechart

        final LineChart<Number, Number> lineChart = new LineChart<Number,Number>(xAxis, yAxis){
            //Override to remove symbols on each data point
            @Override
            protected void dataItemAdded(Series<Number, Number> series, int itemIndex, Data<Number,Number> item){
            }
        };

        lineChart.setAnimated(false);
        lineChart.setTitle("AnimatedLineChart");
        lineChart.setHorizontalGridLinesVisible(true);

        //Set name for series

        series1.setName("Series 1");
        series2.setName("Series 2");
        series3.setName("Series 3");

        //Add chart series
        lineChart.getData().addAll(series1, series2, series3);
        grid.add(lineChart,0,1);
        primaryStage.setScene(scene);

    }

    private class AddToQueue implements Runnable{
        public void run(){
            try {
                //add a item of random data to queue
                dataQ1.add(Math.random());
                dataQ2.add(Math.random());
                dataQ3.add(Math.random());

                Thread.sleep(refreshRate);
                executor.execute(this);
            } catch(InterruptedException ex){
                System.out.println("Error in AnimatedLineChart");
                ex.printStackTrace();
            }
        }
    }
    //Timeline gets called in the JavaFX Main thread
    private void prepareTimeline(){
        //Every frame to take any data from queue and add to chart
        new AnimationTimer(){
            @Override
            public void handle(long now){
                addDataToSeries();
            }
        }.start();
    }

    private void addDataToSeries() {
        for (int i = 0; i < 20; i++) {
            if (dataQ1.isEmpty()) break;
            series1.getData().add(new XYChart.Data<>(xSeriesData++, dataQ1.remove()));
            series2.getData().add(new XYChart.Data<>(xSeriesData++, dataQ2.remove()));
            series3.getData().add(new XYChart.Data<>(xSeriesData++, dataQ3.remove()));
        }
        //Remove points to keep us at no more than MAX_DATA_POINTS
        if (series1.getData().size() > MAX_DATA_POINTS) {
            series1.getData().remove(0, series1.getData().size() - MAX_DATA_POINTS); //Removes the oldest data
        }
        if (series2.getData().size() > MAX_DATA_POINTS){
            series2.getData().remove(0, series2.getData().size() - MAX_DATA_POINTS);
        }
        if (series3.getData().size() > MAX_DATA_POINTS){
            series3.getData().remove(0, series3.getData().size() - MAX_DATA_POINTS);
        }
        //update
        xAxis.setLowerBound(xSeriesData-MAX_DATA_POINTS);
        xAxis.setUpperBound(xSeriesData-1);
    }
    */
}
