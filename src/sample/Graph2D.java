package sample;

import javafx.event.EventHandler;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.input.MouseEvent;
import org.gillius.jfxutils.chart.JFXChartUtil;

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

    //public Graph2D(LineChart<Number,Number> lineChart, XYChart.Series<Number,Number> series, NumberAxis x, NumberAxis y, String str){
    public Graph2D(LineChart<Number,Number> lineChart, XYChart.Series<Number,Number> series, ValueAxis<Number> x, ValueAxis<Number> y, String str){
        this.lineChart=lineChart;
        this.series = series;

        if(str=="readStatic") {
            xAxis=x;
            yAxis=y;
            //Graph
            this.lineChart.setAnimated(false);
            this.lineChart.setId("staticLineChart");
            this.lineChart.setTitle("Static Line Chart");

            this.lineChart.setCreateSymbols(false);
            System.out.println("Graph created successfully");
        }
        else if(str == "readAnimated"){
            xAxis=x;
            yAxis=y;
            //Graph
            this.lineChart.setAnimated(false);
            this.lineChart.setId("animatedLineChart");
            this.lineChart.setTitle("Animated Line Chart");

            this.lineChart.setCreateSymbols(false);
            System.out.println("Graph created successfully");
        }
        else if(str =="readExperimental"){
            xAxis=x;
            yAxis=y;

            this.lineChart.setTitle("Static Line Chart - Experimental");
            this.lineChart.setCreateSymbols(false);

            //JFXutil
            JFXChartUtil jfx = new JFXChartUtil();
            jfx.setupZooming(this.lineChart, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    if(event.getEventType()==MouseEvent.MOUSE_ENTERED || event.getEventType() == MouseEvent.MOUSE_MOVED || event.getEventType() == MouseEvent.MOUSE_EXITED){
                        event.consume();
                    }
                }
            });

            System.out.println("Experimental - Graph created successfully");
        }
        else{
            System.out.println("Error in Constructor 2, Graph2D: If-criteria not met.");
        }
    }
    public void setup(){
        //Setup of static graph
        xAxis.setAutoRanging(true); //As to not have the axis scale weirdly
        xAxis.setTickLabelsVisible(true);
        xAxis.setTickMarkVisible(false);
        xAxis.setMinorTickVisible(false);
        yAxis.setAutoRanging(true);

        //lineChart.setCreateSymbols(true);
        lineChart.getData().add(series);

    }

}
