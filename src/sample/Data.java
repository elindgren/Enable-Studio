package sample;

import javafx.scene.chart.XYChart;

/**
 * Created by Eric on 2017-04-16.
 */
public class Data {

    public static XYChart.Series getAcc(){
        XYChart.Series<Number,Number> series = new XYChart.Series<Number,Number>();
        //Rewrite to take data from Arduino
        for(int i = 0; i < 10; i++){
            series.getData().add(new XYChart.Data<Number,Number>(i,Math.random()*10));
        }
        return series;
    }
}
