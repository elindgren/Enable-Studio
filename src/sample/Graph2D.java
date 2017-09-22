package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.JFXChartUtil;

import java.util.ArrayList;

import static java.awt.event.MouseEvent.MOUSE_ENTERED;

/**
 * Created by ericl on 2017-06-14.
 */
public class Graph2D {
    //private NumberAxis xAxis;
    //private NumberAxis yAxis;
    private ValueAxis<Number> xAxis;
    private ValueAxis<Number> yAxis;
    private LineChart<Number,Number> lineChart;
    private XYChart.Series<Number,Number> series;
    private ChartPanManager panManager;
    private Data data;

    //public Graph2D(LineChart<Number,Number> lineChart, XYChart.Series<Number,Number> series, NumberAxis x, NumberAxis y, String str, ChartPanManager panManager){
    public Graph2D(LineChart<Number,Number> lineChart, XYChart.Series<Number,Number> series, ValueAxis x, ValueAxis y, String str){
        this.lineChart=lineChart;
        this.series=series;
        this.panManager=panManager;

        if(str=="readStatic") {
            xAxis=x;
            yAxis=y;
            //Graph
            this.lineChart.setAnimated(false);
            this.lineChart.setId("staticLineChart");
            //Series
            series.setName("x");
            this.lineChart.setCreateSymbols(false);
            System.out.println("Graph created successfully");
        }
        else if(str == "readAnimated"){
            xAxis=x;
            yAxis=y;
            //Graph
            this.lineChart.setAnimated(false);
            this.lineChart.setId("animatedLineChart");
            //Series
            series.setName("x");
            this.lineChart.setCreateSymbols(false);
            System.out.println("Graph created successfully");
        }
        else{
            System.out.println("Error in Constructor 2, Graph2D: If-criteria not met.");
        }
    }

    public void setup(String str) {
        //Setup of static graph
        xAxis.setAutoRanging(true); //As to not have the axis scale weirdly
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);
        yAxis.setAutoRanging(true);

        //lineChart.setCreateSymbols(true);

        //********JFXUtil - Panning & Zooming ******//

        JFXChartUtil jfxStatic = new JFXChartUtil();
        javafx.scene.layout.Region regStatic = jfxStatic.setupZooming(lineChart);
        jfxStatic.addDoublePrimaryClickAutoRangeHandler(lineChart);

        //Panning
        panManager = new ChartPanManager(lineChart);
        panManager.setMouseFilter(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.isSecondaryButtonDown()){
                    //pan.start();
                }
                else{
                    event.consume();
                }
            }
        });
        panManager.start();
        //lineChart.setCreateSymbols(true);
        lineChart.getData().setAll(series);




        //*****************************************//
    }

    public void setDataClickable(boolean status){
        if(!status) {
            ArrayList lists = new ArrayList<ObservableList<XYChart.Data<Number, Number>>>();
            //ObservableList<XYChart.Data<Number, Number>> list = lineChart.getData().get(0).getData();//series.getData();
            lists.add(0,lineChart.getData().get(0).getData());
            for(int i=1; i<lineChart.getData().size(); i++){
                lists.add(i,lineChart.getData().get(i).getData());
            }
            for(int i=0; i<lists.size(); i++) { //Acessing all of the current series of the lineChart and setting them up individually
                for (int j = 0; j < ((ObservableList)lists.get(i)).size(); j++) {
                    //list.get(i).setNode(new ClickedThresholdNode(list.get(j).getXValue(),list.get(j).getYValue()));
                    ((ObservableList<XYChart.Data<Number, Number>>)lists.get(i)).get(j).setNode(new ClickedThresholdNode(i,((ObservableList<XYChart.Data<Number, Number>>)lists.get(i)).get(j).getXValue(),((ObservableList<XYChart.Data<Number, Number>>)lists.get(i)).get(j).getYValue()));
                }
            }
            for(int i=0; i<lists.size(); i++) {
                lineChart.getData().set(i,lineChart.getData().get(i));
            }
        }
        else{
            ArrayList lists = new ArrayList<ObservableList<XYChart.Data<Number, Number>>>();
            //ObservableList<XYChart.Data<Number, Number>> list = lineChart.getData().get(0).getData();//series.getData();
            lists.add(0,lineChart.getData().get(0).getData());
            for(int i=1; i<lineChart.getData().size(); i++){
                lists.add(i,lineChart.getData().get(i).getData());
            }
            for(int i=0; i<lists.size(); i++) { //Acessing all of the current series of the lineChart and setting them up individually
                for (int j = 0; j < ((ObservableList)lists.get(i)).size(); j++) {
                    //list.get(i).setNode(new ClickedThresholdNode(list.get(j).getXValue(),list.get(j).getYValue()));
                    ((ObservableList<XYChart.Data<Number, Number>>)lists.get(i)).get(j).getNode().setVisible(false); //TODO Have to replace with blank nodes, not null
                }
            }

            //list.get(i).getNode().setVisible(false); //TODO Have to replace with blank nodes, not null

            //lineChart.getData().setAll(lineChart.getData().get(0)); //lineChart.getData().setAll(series);
            for(int i=0; i<lists.size(); i++) {
                lineChart.getData().set(i,lineChart.getData().get(i));
            }
        }
    }

    class ClickedThresholdNode extends StackPane {
        ClickedThresholdNode(int series, Number xValue, Number yValue) {
            setPrefSize(5,5);

            final Label label = createDataThresholdLabel(series, xValue, yValue);

            setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    getChildren().setAll(label);
                    setCursor(Cursor.NONE);
                    toFront();
                }
            });
            setOnMouseExited(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    getChildren().clear();
                    setCursor(Cursor.DEFAULT);
                }
            });
        }

        private Label createDataThresholdLabel(int series, Number xValue, Number yValue) {
            final Label label = new Label("x=" + xValue + ", " + "y=" + yValue);
            if(series==0){
                label.getStyleClass().addAll("default-color0","chart-line-symbol","chart-series-line");
            }
            else if(series==1){
                label.getStyleClass().addAll("default-color1","chart-line-symbol","chart-series-line");
            }
            else if(series==2){
                label.getStyleClass().addAll("default-color2","chart-line-symbol","chart-series-line");
            }
            //label.getStyleClass().addAll("default-color0","chart-line-symbol","chart-series-line");
            label.setStyle("-fx-font-size: 12; -fx-font-weight: bold");

            if(yValue.doubleValue()==0){
                label.setTextFill(Color.DARKGRAY);
            }
            else if(yValue.doubleValue() > 0){
                label.setTextFill(Color.FORESTGREEN);
            }
            else{
                label.setTextFill(Color.FIREBRICK);
            }

            label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
            return label;
        }
    }
}
