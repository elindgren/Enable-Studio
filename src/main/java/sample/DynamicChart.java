package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class DynamicChart{

    private LineChart<Number, Number> chart;
    private XYChart.Series<Number, Number> dataSeries;
    private NumberAxis xAxis;
    private Timeline animation;
    private double sequence = 0;
    private double y = 10;
    private final int MAX_DATA_POINTS = 25, MAX = 10, MIN = 5;



    public DynamicChart() {

        // create timeline to add new data every 60th of second
        animation = new Timeline();
        animation.getKeyFrames()
                .add(new KeyFrame(Duration.millis(100),
                        (ActionEvent actionEvent) -> plotTime()));
        animation.setCycleCount(Animation.INDEFINITE);
    }

    public Parent createContent() {

        xAxis = new NumberAxis(0, MAX_DATA_POINTS + 1, 2);
        final NumberAxis yAxis = new NumberAxis(MIN - 1, MAX + 1, 1);
        chart = new LineChart<>(xAxis, yAxis);

        // setup chart
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setTitle("Animated Line Chart");
        xAxis.setLabel("X Axis");
        xAxis.setForceZeroInRange(false);

        yAxis.setLabel("Y Axis");
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis, "$", null));

        // add starting data
        dataSeries = new XYChart.Series<>();
        dataSeries.setName("Data");

        // create some starting data
        dataSeries.getData()
                .add(new XYChart.Data<Number, Number>(++sequence, y));

        chart.getData().add(dataSeries);

        return chart;
    }

    private void plotTime() {
        dataSeries.getData().add(new XYChart.Data<Number, Number>(++sequence, getNextValue()));

        // after 25hours delete old data
        if (sequence > MAX_DATA_POINTS) {
            dataSeries.getData().remove(0);
        }

        // every hour after 24 move range 1 hour
        if (sequence > MAX_DATA_POINTS - 1) {
            xAxis.setLowerBound(xAxis.getLowerBound() + 1);
            xAxis.setUpperBound(xAxis.getUpperBound() + 1);
        }
    }

    private int getNextValue(){
        Random rand = new Random();
        return rand.nextInt((MAX - MIN) + 1) + MIN;
    }

    public void play() {
        animation.play();
    }

    public void stop() {
        animation.pause();
    }
}