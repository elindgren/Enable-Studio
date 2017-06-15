package sample;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

/**
 * Created by ericl on 2017-06-14.
 */
public class Graph2D {
    private NumberAxis xAxis;
    private NumberAxis yAxis;
    private LineChart<Number,Number> lineChart;
    private XYChart.Series<Number,Number> series;
    public Graph2D(LineChart<Number,Number> lineChart, XYChart.Series<Number,Number> series, NumberAxis x, NumberAxis y, String str){
        this.lineChart=lineChart;
        this.series = series;

        if(str=="b21") {
            xAxis=x;
            yAxis=y;
            //Graph
            this.lineChart.setAnimated(false);
            this.lineChart.setId("staticLineChart");
            this.lineChart.setTitle("Static Line Chart");

            this.lineChart.setCreateSymbols(true);
            System.out.println("Graph created successfully");
        }
        else if(str == "b11"){
            xAxis=x;
            yAxis=y;
            //Graph
            this.lineChart.setAnimated(false);
            this.lineChart.setId("animatedLineChart");
            this.lineChart.setTitle("Animated Line Chart");

            this.lineChart.setCreateSymbols(true);
            System.out.println("Graph created successfully");
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

        lineChart.setCreateSymbols(true);
        lineChart.getData().add(series);

    }

}
