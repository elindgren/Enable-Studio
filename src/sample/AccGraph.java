package sample;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * Created by Eric on 2017-04-11.
 */
public class AccGraph extends Application {

    @Override public void start(Stage stage){
        double[] data;


        stage.setTitle("JANA-viewer");

        //Creating 2 gridpanes, one for each scene

        GridPane grid = new GridPane();

        //Setting alignements, gap between columns and padding before the borders for the grid
        grid.setAlignment(Pos.CENTER);

        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25,25,25,25)); //The padding in TOP, RIGHT, BOTTOM, LEFT


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
        statxyzyAxis.setLabel("resulting acceleration, <m/s^2>");

        //creating the charts
        final LineChart<Number,Number> statChart =
                new LineChart<Number,Number>(statxAxis,statyAxis);

        final LineChart<Number,Number> dynChart = new LineChart<Number,Number>(dynxAxis,dynyAxis);

        final LineChart<Number,Number> statxyzChart = new LineChart<Number,Number>(statxyzxAxis,statxyzyAxis);

        statChart.setTitle("Magnitude of resulting acceleration");
        dynChart.setTitle("Magnitude of resulting acceleration");
        statxyzChart.setTitle("Magnitude of resulting acceleration");

        //defining a static series
        XYChart.Series statSeries = new XYChart.Series();
        statSeries.setName("Resulting acceleration, static, resultant");
        //defining a dynamic series
        XYChart.Series dynSeries = new XYChart.Series();
        dynSeries.setName("Resulting acceleration, dynamic, resultant");
        //defining statxyz-series
        XYChart.Series xstatxyzSeries = new XYChart.Series();
        xstatxyzSeries.setName("Acceleration, x");
        XYChart.Series ystatxyzSeries = new XYChart.Series();
        ystatxyzSeries.setName("Acceleration, y");
        XYChart.Series zstatxyzSeries = new XYChart.Series();
        zstatxyzSeries.setName("Acceleration, z");

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
                stage.setScene(scene1);
            }
            else{
                graphSwitch.setText("Dynamic");

                grid.getChildren().remove(dynChart);
                grid.add(statChart,0,1);
                stage.setScene(scene1);
            }
        });

        statxyzSwitch.setOnAction(e -> {
            if(statxyzSwitch.getText() == "Components"){
                statxyzSwitch.setText("Resultant");

                grid.getChildren().remove(statChart);
                grid.add(statxyzChart,0,1);
                stage.setScene(scene1);
            }
            else{
                statxyzSwitch.setText("Components");

                grid.getChildren().remove(statxyzChart);
                grid.add(statChart,0,1);
                stage.setScene(scene1);
            }
        });

        //Defualt static view

        stage.setScene(scene1);
        stage.show();

    }

    public static void main(String[] args){
        launch(args);
    }
}
