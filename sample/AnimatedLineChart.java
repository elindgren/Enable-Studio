package sample;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;


/**
 * Created by ericl on 2017-04-12.
 */
public class AnimatedLineChart extends Application {
    private Stage stage;

    public Stage getStage(){
        return stage;
    }

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

    private void init(Stage primaryStage){
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
        Scene view = new Scene(lineChart);
        primaryStage.setScene(view);
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Animated Line Chart Sample");
        this.stage = stage;
        init(stage);
        stage.show();


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

    public static void main(String[] args){
        launch(args);
    }
}
