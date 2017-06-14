package sample;
import com.sun.deploy.security.DeployURLClassPathCallback;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

/**
 * Created by Eric on 2017-04-16.
 */
public class Data {
    private static ObservableList<DataPoint2D> tableDataStatic;
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

        tableDataStatic = FXCollections.observableArrayList();

        //Listener to track changes to table

/*
        tableDataStatic.addListener(new ListChangeListener<DataPoint2D>() {
                                        @Override
                                        public void onChanged(Change<? extends DataPoint2D> c) {
                                            //Write something here TODO
                                        }
                                    }
                                    );
*/
        XYChart.Series<Number,Number> staticData = new XYChart.Series<Number,Number>();
        int seconds;
        for (int i=0; i<serialDataAccStatic.length; i++ ){
            //XY-data for chart
            XYChart.Data<Number,Number> staticDataPoint=new XYChart.Data<Number,Number>(serialDataAccStatic[i][0], serialDataAccStatic[i][1]);
            staticData.getData().add(staticDataPoint);
            //IntelliJ doesn't like the row below
            tableDataStatic.add(new DataPoint2D(serialDataAccStatic[i][0],serialDataAccStatic[i][1]));
        }
        return staticData;
    }
    public static ObservableList<DataPoint2D> getTableDataStatic(){
        return tableDataStatic;
    }
}

