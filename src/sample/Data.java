package sample;
import com.sun.deploy.security.DeployURLClassPathCallback;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

/**
 * Created by Eric on 2017-04-16.
 */
public class Data {
    private static ObservableList<DataPoint2D> tableDataStatic;
    private static ObservableList<DataPoint2D> tableDataAnimated = FXCollections.observableArrayList();
    private static XYChart.Series<Number,Number> staticDataSeries;
    private static XYChart.Series<Number,Number> animatedDataSeries;
    private static double[][] serialDataAccStatic;
    private static double[][] serialDataAccAnimated;
    //*********************************Animated Data********************************//
    public static XYChart.Data<Number,Number> getAnimatedAcc(int secs){

        int seconds = secs;
        ReadSerialPort rp = new ReadSerialPort();
        serialDataAccAnimated = rp.stringArrayToDoubleMatrix("animated",seconds);

        animatedDataSeries = new XYChart.Series<Number,Number>();
        XYChart.Data<Number,Number> animatedDataPoint = new XYChart.Data<Number,Number>(0,0);
        for (int i=0; i<serialDataAccAnimated.length; i++ ){
            //XY-data for chart
            animatedDataPoint=new XYChart.Data<Number,Number>(serialDataAccAnimated[i][0], serialDataAccAnimated[i][1]);
            animatedDataSeries.getData().add(animatedDataPoint);

            DataPoint2D datapoint = new DataPoint2D(serialDataAccAnimated[i][0],serialDataAccAnimated[i][1], i); //Number is the number of the object created, referring to its place in the array

            //Adding listeners to change the dataTable and the XYChart-data whenever a change occurs in the tableView.
            datapoint.xProperty().addListener( (v, oldValue, newValue) -> {
                //Fetching the new value and inserting it in the XYChart, and then inserting it in the array
                animatedDataSeries.getData().get(datapoint.getIndex()).setXValue(datapoint.getX().doubleValue());
                serialDataAccAnimated[datapoint.getIndex()][0] = newValue.doubleValue();
            });
            datapoint.yProperty().addListener( (v, oldValue, newValue) -> {
                animatedDataSeries.getData().get(datapoint.getIndex()).setYValue(datapoint.getY().doubleValue());
                serialDataAccAnimated[datapoint.getIndex()][1] = newValue.doubleValue();
            });
            tableDataAnimated.add(datapoint);
        }
        return animatedDataPoint;
    }
    //******************************************************************************//

    //***********************************Static Data********************************//
    //Returns a XYChart.Series, not XYChart.Data (it returns a whole set of data, not individual data points).

    public static XYChart.Series<Number,Number> getStaticDataGraph(){
        ReadSerialPort rp = new ReadSerialPort();
        serialDataAccStatic = rp.stringArrayToDoubleMatrix("static", 0);

        tableDataStatic = FXCollections.observableArrayList();
        //(datapoint) -> new Observable[]{datapoint.xProperty()}

        staticDataSeries = new XYChart.Series<Number,Number>();
        for (int i=0; i<serialDataAccStatic.length; i++ ){
            //XY-data for chart
            XYChart.Data<Number,Number> staticDataPoint=new XYChart.Data<Number,Number>(serialDataAccStatic[i][0], serialDataAccStatic[i][1]);
            staticDataSeries.getData().add(staticDataPoint);

            DataPoint2D datapoint = new DataPoint2D(serialDataAccStatic[i][0],serialDataAccStatic[i][1], i); //Number is the number of the object created, referring to its place in the array

            //Adding listeners to change the dataTable and the XYChart-data whenever a change occurs in the tableView.
            datapoint.xProperty().addListener( (v, oldValue, newValue) -> {
                //Fetching the new value and inserting it in the XYChart, and then inserting it in the array
                staticDataSeries.getData().get(datapoint.getIndex()).setXValue(datapoint.getX().doubleValue());
                serialDataAccStatic[datapoint.getIndex()][0] = newValue.doubleValue();
            });
            datapoint.yProperty().addListener( (v, oldValue, newValue) -> {
                staticDataSeries.getData().get(datapoint.getIndex()).setYValue(datapoint.getY().doubleValue());
                serialDataAccStatic[datapoint.getIndex()][1] = newValue.doubleValue();
            });

            tableDataStatic.add(datapoint);
        }
        return staticDataSeries;
    }
    public static ObservableList<DataPoint2D> getTableDataStatic(){return tableDataStatic;}

    public static ObservableList<DataPoint2D> getTableDataAnimated() {return tableDataAnimated;}

    public static void resetXYChartStatic(){
        //Reset Legend TODO
        staticDataSeries.getData().removeAll(staticDataSeries.getData());
        serialDataAccStatic = null; //Creating a new, empty matrix. Cannot make it null?
    }
    public static void resetXYChartAnimated(){
        //Reset Legend TODO
        animatedDataSeries.getData().removeAll(animatedDataSeries.getData());
        serialDataAccAnimated = null; //Creating a new, empty matrix. Cannot make it null?
    }


}

