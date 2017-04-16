package sample;

import com.sun.org.apache.xpath.internal.SourceTree;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;

import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;

public class Controller implements Initializable {
    //Table
    @FXML
    private TableView table;

    //Adding closing functionality to close option in menu
    @FXML
    private MenuItem menuClose;

    //On-screen buttons
    @FXML
    private Button b1;
    @FXML
    private Button b2;

    //LineChart
    @FXML
    private LineChart lineChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    private XYChart.Series<Number,Number> series;
    private ExecutorService executor;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources){
        //Checks if all the imported fx:id's are declared in FXML-file
        assert menuClose != null : "fx:id=\"menuClose\" was not injected: check your FXML file";
        assert b1 != null : "fx:id=\"b1\" was not injected: check your FXML file";
        assert b2 != null : "fx:id=\"b2\" was not injected: check your FXML file";

        menuClose.setOnAction(e -> {
            System.out.println("Skall lägga till stängning här");
        });

        b1.setOnAction(e -> {
            System.out.println("b1");
        });

        this.initGraph();


    }

    public void initGraph(){
        xAxis.setAutoRanging(false); //As to not have the axis scale wierdly

        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(true);

        yAxis.setAutoRanging(true);

        //Graph
        lineChart.setAnimated(false);
        lineChart.setId("liveLineChart");
        lineChart.setTitle("Animated Line Chart");

        series=Data.getAcc();

        series.setName("Random Data");
        lineChart.getData().add(series);
    }


}
