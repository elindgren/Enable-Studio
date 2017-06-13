package sample;


import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import java.net.URL;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import javafx.scene.text.Text;

import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;

public class Controller implements Initializable {
    //TableView
    @FXML
    private TableView tableStatic;

    //MenuBar
    @FXML
    private MenuItem menuClose;

    //On-screen buttons. Naming-scheme: button bij referes to button j on tab i. Ex. b12 refers to button 2 on tab 1.
    @FXML
    private Button b11;
    @FXML
    private Button b12;
    @FXML
    private Button b13;
    @FXML
    private Button b21;
    @FXML
    private Button b22;

    //LineChart - Animated
    @FXML
    private LineChart lineChartAnimated;
    @FXML
    private NumberAxis xAxisAnimated;
    @FXML
    private NumberAxis yAxisAnimated;
    //Series for LineChart
    private XYChart.Series<Number,Number> seriesAnimated;

    //LineChart - Static
    @FXML
    private LineChart lineChartStatic;
    @FXML
    private NumberAxis xAxisStatic;
    @FXML
    private NumberAxis yAxisStatic;
    //Series for LineChart
    private XYChart.Series<Number,Number> seriesStatic;

    @Override
    public void initialize(URL fxmlFileLocation, ResourceBundle resources){
        //Checks if all the imported fx:id's are declared in FXML-file
        assert menuClose != null : "fx:id=\"menuClose\" was not injected: check your FXML file";
        assert b11 != null : "fx:id=\"b11\" was not injected: check your FXML file";
        assert b12 != null : "fx:id=\"b12\" was not injected: check your FXML file";
        assert b13 != null : "fx:id=\"b13\" was not injected: check your FXML file";
        assert b21 != null : "fx:id=\"b21\" was not injected: check your FXML file";
        assert b22 != null : "fx:id=\"b22\" was not injected: check your FXML file";

        menuClose.setOnAction(e -> {
            System.out.println("MenuClose");
        });

        b11.setOnAction(e -> {
            initAnimatedGraph();
            MainApp.startAnimatedTimeline();
            System.out.println("b11");
        });
        b12.setOnAction(e -> {
            MainApp.stopAnimatedTimeline();
            System.out.println("b12");
        });
        b13.setOnAction(e -> {
            MainApp.setAnimatedSeries(new XYChart.Series());
            System.out.println("b13");
        });

        b21.setOnAction(e -> {
            MainApp.staticChart();
            initStaticGraph();
            System.out.println("b21");
        });

        b22.setOnAction(e -> {
            MainApp.setStaticSeries(new XYChart.Series());
            System.out.println("b22");
        });
    }

    public void initStaticGraph(){
        //Setup of static TableView.
        /*
        //Fetching data for table
        tableDataStatic=Data.getStaticAcc();
        tableStatic.setItems(tableDataStatic);
        TableColumn<Number,String> xDataStaticCol = new TableColumn<Number,String>("x");
        xDataStaticCol.setCellValueFactory(new PropertyValueFactory<Number, String>());
        */


        //Setup of static graph
        xAxisStatic.setAutoRanging(true); //As to not have the axis scale weirdly
        xAxisStatic.setTickLabelsVisible(true);
        xAxisStatic.setTickMarkVisible(false);
        xAxisStatic.setMinorTickVisible(false);

        yAxisStatic.setAutoRanging(true);

        //Graph
        lineChartStatic.setAnimated(false);
        lineChartStatic.setId("staticLineChart");
        lineChartStatic.setTitle("Static Line Chart");

        seriesStatic=MainApp.getStaticSeries();
        seriesStatic.setName("Static Series1");

        lineChartStatic.getData().add(seriesStatic);
        lineChartStatic.setCreateSymbols(false);


    }

    public void initAnimatedGraph(){

        //Setup of graph
        xAxisAnimated.setAutoRanging(true); //As to not have the axis scale weirdly
        xAxisAnimated.setTickLabelsVisible(true);
        xAxisAnimated.setTickMarkVisible(false);
        xAxisAnimated.setMinorTickVisible(false);

        yAxisAnimated.setAutoRanging(true);

        //Graph
        lineChartAnimated.setAnimated(false);
        lineChartAnimated.setId("liveLineChart");
        lineChartAnimated.setTitle("Animated Line Chart");

        seriesAnimated=MainApp.getAnimatedSeries();
        seriesAnimated.setName("Animated Series1");

        lineChartAnimated.getData().add(seriesAnimated);
        lineChartAnimated.setCreateSymbols(false);
    }

    public void test(){
        System.out.println("Test");
    }


}
