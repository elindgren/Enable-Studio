package sample;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;

/**
 * Created by Eric on 2017-04-11.
 */
public class AccGraph extends Application {

    @Override public void start(Stage stage) throws Exception{
        stage.setTitle("JANA-viewer");

        //defining the axes
        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setLabel("time, <s>");
        yAxis.setLabel("resulting acceleration, <m/s^2>");
        //creating the chart
        final LineChart<Number,Number> lineChart =
                new LineChart<Number,Number>(xAxis,yAxis);

        lineChart.setTitle("Magnitude of resulting acceleration");
        //defining a series
        XYChart.Series series1 = new XYChart.Series();
        series1.setName("My portfolio");
        //populating the series with data

        for(int i = 0; i <=10; i++) {
            series1.getData().add(new XYChart.Data(i,i));
        }

        Scene scene  = new Scene(lineChart,800,600);
        lineChart.getData().add(series1);

        stage.setScene(scene);
        stage.show();

    }

    public static void main(String[] args){
        launch(args);
    }
}
