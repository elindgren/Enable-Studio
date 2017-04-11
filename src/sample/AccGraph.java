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
        stage.setTitle("JANA-viewer");

        //Creating 2 gridpanes, one for each scene

        GridPane grid1 = new GridPane();
        GridPane grid2 = new GridPane();

        //Setting alignements, gap between columns and padding before the borders for the grid
        grid1.setAlignment(Pos.CENTER);
        grid2.setAlignment(Pos.CENTER);

        grid1.setHgap(10);
        grid1.setVgap(10);
        grid1.setPadding(new Insets(25,25,25,25)); //The padding in TOP, RIGHT, BOTTOM, LEFT

        grid2.setHgap(10);
        grid2.setVgap(10);
        grid2.setPadding(new Insets(25,25,25,25)); //The padding in TOP, RIGHT, BOTTOM, LEFT


        //defining the axes
        final NumberAxis statxAxis = new NumberAxis();
        final NumberAxis statyAxis = new NumberAxis();
        statxAxis.setLabel("time, <s>");
        statyAxis.setLabel("resulting acceleration, <m/s^2>");

        final NumberAxis dynxAxis = new NumberAxis();
        final NumberAxis dynyAxis = new NumberAxis();
        dynxAxis.setLabel("time, <s>");
        dynyAxis.setLabel("resulting acceleration, <m/s^2>");
        //creating the chart
        final LineChart<Number,Number> statChart =
                new LineChart<Number,Number>(statxAxis,statyAxis);

        final LineChart<Number,Number> dynChart = new LineChart<Number,Number>(dynxAxis,dynyAxis);

        statChart.setTitle("Magnitude of resulting acceleration");
        dynChart.setTitle("Magnitude of resulting acceleration");

        //defining a static series
        XYChart.Series statSeries = new XYChart.Series();
        statSeries.setName("Resulting acceleration, static");
        //defining a dynamic series
        XYChart.Series dynSeries = new XYChart.Series();
        dynSeries.setName("Resulting acceleration, dynamic");

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


        //Setting dots to false
        statChart.setCreateSymbols(false);
        dynChart.setCreateSymbols(false);

        //Creating a 2 buttons button to switch between Scenes

        Button scene1Switch = new Button("Dynamic");
        Button scene2Switch = new Button("Static");

        //Adding everything static to the static scene (scene1)
        grid1.add(scene1Switch,1,1); //Adding the button
        grid1.add(statChart,0, 1);
        //Creating the static chart-scene
        Scene scene1 = new Scene(grid1,800,600);

        //Adding everything static to the dynamic scene (scene2)
        grid2.add(scene2Switch,1,1);
        grid2.add(dynChart,0,1);

        //Creating the dynamic chart-scene
        Scene scene2 = new Scene(grid2,800,600);

        //Events for when pressing the buttons, scene1 -> scene2
        scene1Switch.setOnAction(e -> {
            //scene1Switch.setText("Static");
            //dynSeries.setName("Resulting acceleration, dynamic");
            stage.setScene(scene2);
        });

        //scene2 -> scene1
        scene2Switch.setOnAction(e -> {
            stage.setScene(scene1);
        });



        //Defualt static view

        stage.setScene(scene1);
        stage.show();

    }

    public static void main(String[] args){
        launch(args);
    }
}
