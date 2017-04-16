package sample;

import javafx.scene.chart.XYChart;

/**
 * Created by Eric on 2017-04-16.
 */
public class Data {

    public static XYChart.Data getAcc(XYChart.Series series){

        double[][] acc = new double[10][2];
        for(int i = 0; i < 10; i++){
            acc[i][0] = (double)i;
            acc[i][1] = 10*Math.random();
        }
        return acc;
    }
}
