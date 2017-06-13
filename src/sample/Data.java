package sample;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

/**
 * Created by Eric on 2017-04-16.
 */
public class Data {
    ObservableList<Number> tableDataStatic;
    ObservableList<Number> tableDataAnimated;
    //*********************************Animated Data********************************//
    public static XYChart.Data<Number,Number> getAnimatedAcc(int secs){
        int seconds = secs;
        XYChart.Data<Number,Number> animatedData = new XYChart.Data<Number,Number>(seconds, Math.random()*10);
        return animatedData;
    }
    //******************************************************************************//

    //***********************************Static Data********************************//
    //Returns a XYChart.Series, not XYChart.Data (it returns a whole set of data, not individual data points).
    public static XYChart.Series<Number,Number> getStaticAcc(){
        ReadSerialPort hej = new ReadSerialPort();
        Double[][] acc = hej.stringArrayToDoubleMatrix();

        XYChart.Series<Number,Number> staticData = new XYChart.Series<Number,Number>();
        int seconds;
        for (int i=0; i<10; i++ ){
            seconds = i;
            XYChart.Data<Number,Number> staticDataPoint=new XYChart.Data<Number,Number>(acc[i][1], acc[i][5]);
            staticData.getData().add(staticDataPoint);
        }
        //tableDataStatic.setAll()
        return staticData;
    }
    //*****************************************************************************//
}

