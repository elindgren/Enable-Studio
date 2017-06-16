package sample;


import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import java.net.URL;

import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.Duration;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import javafx.scene.text.Text;
import javafx.util.converter.NumberStringConverter;

import static com.sun.xml.internal.fastinfoset.alphabet.BuiltInRestrictedAlphabets.table;

public class Controller implements Initializable {

    //MenuBar
    @FXML
    private MenuItem menuClose;

    //On-screen buttons. Naming-scheme: button bij refers to button j on tab i. Ex. b12 refers to button 2 on tab 1.
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

    //TableView - Static
    @FXML
    private TableView tableStatic;
    private TableColumn xDataStaticCol;
    private TableColumn yDataStaticCol;
    //TableView - Animated
    @FXML
    private TableView tableAnimated;
    private TableColumn xDataAnimatedCol;
    private TableColumn yDataAnimatedCol;

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
    //Static series
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
            System.out.println("b13");
            ObservableList<DataPoint2D> tableData = Data.getTableDataAnimated();
            tableData.remove(0,tableData.size());
            Data.resetXYChartAnimated();

        });

        b21.setOnAction(e ->{
                MainApp.staticChart();
                initStaticGraph();
                System.out.println("b21");
            });

        b22.setOnAction(e -> {
            System.out.println("b22");
            ObservableList<DataPoint2D> tableData = Data.getTableDataStatic();
            tableData.remove(0,tableData.size());
            Data.resetXYChartStatic();
        });


        //Setup of static TableView.
        tableStatic.setEditable(true);
        //X-column
        xDataStaticCol = new TableColumn("Time, <s>");
        TableCol xColStatic = new TableCol(xDataStaticCol,"x");

        //Y-column
        yDataStaticCol = new TableColumn("Acc, <m/s>");
        TableCol yColStatic = new TableCol(yDataStaticCol,"y");

        //****************************//
        //Setup of Animated TableView.
        tableAnimated.setEditable(true);
        //X-column
        xDataAnimatedCol = new TableColumn("Time, <s>");
        TableCol xColAnimated = new TableCol(xDataAnimatedCol,"x");

        //Y-column
        yDataAnimatedCol = new TableColumn("Acc, <m/s>");
        TableCol yColAnimated = new TableCol(yDataAnimatedCol,"y");
    }

    public void initStaticGraph(){
        //Fetching data for table
        tableStatic.setItems(Data.getTableDataStatic());
        tableStatic.getColumns().setAll(xDataStaticCol,yDataStaticCol);

        //Setup of series
        seriesStatic=MainApp.getStaticSeries();
        seriesStatic.setName("Static Series");

        Graph2D staticGraph = new Graph2D(lineChartStatic, seriesStatic, xAxisStatic, yAxisStatic,"b21");
        staticGraph.setup();
    }
    public void initAnimatedGraph(){

        //Fetching data for table
        ObservableList<DataPoint2D> series = Data.getTableDataAnimated();
        tableAnimated.setItems(series);
        series.addListener(new ListChangeListener<DataPoint2D>() {
            @Override
            public void onChanged(Change<? extends DataPoint2D> c) {
                updateTableViewAnimated();
            }
        });
        tableAnimated.getColumns().setAll(xDataAnimatedCol,yDataAnimatedCol);

        seriesAnimated=MainApp.getAnimatedSeries();
        seriesAnimated.setName("Animated Series");

        Graph2D staticGraph = new Graph2D(lineChartAnimated, seriesAnimated, xAxisAnimated, yAxisAnimated,"b11");
        staticGraph.setup();
    }
    private void updateTableViewAnimated(){
        tableAnimated.setItems(Data.getTableDataAnimated());
    }
}
