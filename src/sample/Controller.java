package sample;


import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableView;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import java.net.URL;
import javafx.util.Duration;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import javafx.scene.text.Text;

public class Controller implements Initializable {
    //TableView
    @FXML
    private TableView table;

    //MenuBar
    @FXML
    private MenuItem menuClose;

    //On-screen buttons
    @FXML
    private Button b1;
    @FXML
    private Button b2;
    @FXML
    private Button b3;

    //LineChart
    @FXML
    private LineChart lineChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    //Series for LineChart
    private XYChart.Series<Number,Number> series;




    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources){
        //Checks if all the imported fx:id's are declared in FXML-file
        assert menuClose != null : "fx:id=\"menuClose\" was not injected: check your FXML file";
        assert b1 != null : "fx:id=\"b1\" was not injected: check your FXML file";
        assert b2 != null : "fx:id=\"b2\" was not injected: check your FXML file";

        menuClose.setOnAction(e -> {
            System.out.println("MenuClose");
        });

        b1.setOnAction(e -> {
            MainApp.startTimeline();
            System.out.println("b1");
        });
        b2.setOnAction(e -> {
            MainApp.stopTimeline();
            System.out.println("b2");
        });
        b3.setOnAction(e -> {
            MainApp.setSeries(new XYChart.Series());
            System.out.println("b3");
        });

        this.initGraph();
    }

    public void initGraph(){

        //Setup of graph
        xAxis.setAutoRanging(true); //As to not have the axis scale wierdly
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);

        yAxis.setAutoRanging(true);

        //Graph
        lineChart.setAnimated(false);
        lineChart.setId("liveLineChart");
        lineChart.setTitle("Animated Line Chart");

        //series=Data.getAcc();

        series=MainApp.getSeries();
        series.setName("Random Data 1");



        lineChart.getData().add(series);
        lineChart.setCreateSymbols(false);
    }

    public void test(){
        System.out.println("Test");
    }


}
