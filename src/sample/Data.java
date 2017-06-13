package sample;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

/**
 * Created by Eric on 2017-04-16.
 */
public class Data {
    private static ObservableList<Number> tableDataXStatic;
    private static ObservableList<Number> tableDataYStatic;
    private static ObservableList<Number> tableDataAnimated;
    private static double[][] serialDataAccStatic;
    //*********************************Animated Data********************************//
    public static XYChart.Data<Number,Number> getAnimatedAcc(int secs){
        int seconds = secs;
        XYChart.Data<Number,Number> animatedData = new XYChart.Data<Number,Number>(seconds, Math.random()*10);
        return animatedData;
    }
    //******************************************************************************//

    //***********************************Static Data********************************//
    //Returns a XYChart.Series, not XYChart.Data (it returns a whole set of data, not individual data points).

    public static XYChart.Series<Number,Number> getStaticDataGraph(){
        ReadSerialPort rp = new ReadSerialPort();
        serialDataAccStatic = rp.stringArrayToDoubleMatrix();
        tableDataXStatic = FXCollections.observableArrayList(serialDataAccStatic.length);
        tableDataYStatic = FXCollections.observableArrayList(serialDataAccStatic.length);
        //Listener to track changes to table

        tableDataXStatic.addListener(new ListChangeListener<Number>() {
                                        @Override
                                        public void onChanged(Change<? extends Number> c) {
                                            //Write something here TODO
                                        }
                                    }
        );
        tableDataYStatic.addListener(new ListChangeListener<Number>() {
                                         @Override
                                         public void onChanged(Change<? extends Number> c) {
                                             //Write something here TODO
                                         }
                                     }
        );

        XYChart.Series<Number,Number> staticData = new XYChart.Series<Number,Number>();
        int seconds;
        for (int i=0; i<serialDataAccStatic.length; i++ ){
            tableDataXStatic.add(i, serialDataAccStatic[i][0]);
            XYChart.Data<Number,Number> staticDataPoint=new XYChart.Data<Number,Number>(serialDataAccStatic[i][0], serialDataAccStatic[i][1]);
            staticData.getData().add(staticDataPoint);
        }
        return staticData;
    }
    public static ObservableList<Number> getTableDataXStatic(){
        return tableDataXStatic;
    }
    public static ObservableList<Number> getTableDataYStatic(){
        return tableDataYStatic;
    }
}

