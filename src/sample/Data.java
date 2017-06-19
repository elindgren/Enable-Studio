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
    private  ObservableList<DataPoint2D> tableDataStatic = FXCollections.observableArrayList();
    private  ObservableList<DataPoint2D> tableDataAnimated = FXCollections.observableArrayList();
    private  XYChart.Series<Number,Number> staticDataSeries;
    private  XYChart.Series<Number,Number> animatedDataSeries;
    private  double[][] serialDataAccStatic;
    private  double[][] serialDataAccAnimated;
    private ReadSerialPort rp;
    private  int lastCount = 0;
    private double offset;
    private int offsetRow;

    public Data(ReadSerialPort rp){
        this.rp=rp;
    }



    //*********************************ANIMATED SERIES FOR GRAPH AND TABLE********************************//
    public XYChart.Series<Number,Number> getAnimatedAcc(){
        //***********************************************SETUP OF DATA***********************************//
        if (lastCount == 0){
            //rp.continuousToDoubleMatrix();
            serialDataAccAnimated = rp.getContinuousMatrix();
            offsetRow = rp.getCount();
            offset = serialDataAccAnimated[offsetRow][1]; //Finding offset in time from 0
            while (offset == 0){
                offsetRow = rp.getCount();
                offset = serialDataAccAnimated[offsetRow][1];
                try
                {
                    Thread.sleep(0); //TODO TOO FAST TOO DRAW GRPAH WITHOUT THIS
                }
                catch(InterruptedException ex)
                {
                    Thread.currentThread().interrupt();
                }
            }
            lastCount = rp.getCount();
        }

        int newCount = rp.getCount(); //Used to set proper offset, as to make the graph begin at 0.
        //***************************************************************************************************//

        animatedDataSeries = new XYChart.Series<Number,Number>();
        XYChart.Data<Number,Number> animatedDataPoint;
        for (int i=lastCount; i<newCount; i++ ){
            //********************************** XYCHART.Series, animated*********************************************//
            animatedDataPoint=new XYChart.Data<Number,Number>(serialDataAccAnimated[i][1]-offset, serialDataAccAnimated[i][5]);
            animatedDataSeries.getData().add(animatedDataPoint);
            //********************************************************************************************************//

            //****************List of DataPoints2D, coupled to the double[][] used in the series above****************//
            DataPoint2D datapoint = new DataPoint2D(serialDataAccAnimated[i][1]-offset,serialDataAccAnimated[i][5], i); //Number is the number of the object created, referring to its place in the array
            //Adding listeners to change the dataTable and the XYChart-data whenever a change occurs in the tableView.
            datapoint.xProperty().addListener( (v, oldValue, newValue) -> {
                //Fetching the new value and inserting it in the XYChart, and then inserting it in the array
                animatedDataSeries.getData().get(datapoint.getIndex()).setXValue(datapoint.getX().doubleValue());
                serialDataAccAnimated[datapoint.getIndex()+offsetRow][1] = newValue.doubleValue();
            });
            datapoint.yProperty().addListener( (v, oldValue, newValue) -> {
                animatedDataSeries.getData().get(datapoint.getIndex()).setYValue(datapoint.getY().doubleValue());
                serialDataAccAnimated[datapoint.getIndex()+offsetRow][5] = newValue.doubleValue();
            });
            tableDataAnimated.add(datapoint);
            //System.out.println("tableDataAnimated " + tableDataAnimated.get(i));
            //********************************************************************************************************//
        }
        lastCount = newCount;
        return animatedDataSeries;
    }
    //****************************************************************************************************************//



    //******************************STATIC SERIES FOR GRAPH AND DATA FOR STATIC TABLE*********************************//

    public  XYChart.Series<Number,Number> getStaticDataAcc(String series){
        //********************************SETUP OF DATA - from readserialport********************************//
        int axis;
        if(series=="x"){
            axis =5;
        }
        else if(series =="y"){
            axis = 6;
        }
        else if(series =="z"){
            axis = 7;
        }
        else{
            axis=5;
            System.out.println("Non-valid axis supplied. Using default axis (x)");
        }

        rp.setBuffer(4);
        rp.fileToDoubleMatrix(100);
        serialDataAccStatic = rp.fileToDoubleMatrix(100);
        double offset = serialDataAccStatic[0][1]; //Used to set proper offset, as to make the graph begin at t=0
        //***************************************************************************************************//

        staticDataSeries = new XYChart.Series<Number,Number>();
        for (int i=0; i<serialDataAccStatic.length; i++ ){
            //********************************** XYCHART.Series, Static*********************************************//
            XYChart.Data<Number,Number> staticDataPoint=new XYChart.Data<Number,Number>(serialDataAccStatic[i][1]-offset, serialDataAccStatic[i][axis]);
            staticDataSeries.getData().add(staticDataPoint);
            //******************************************************************************************************//

            //****************List of DataPoints2D, coupled to the double[][] used in the series above****************//
            DataPoint2D datapoint = new DataPoint2D(serialDataAccStatic[i][1]-offset,serialDataAccStatic[i][axis], i); //Number is the number of the object created, referring to its place in the array

            //Adding listeners to change the dataTable and the XYChart-data whenever a change occurs in the tableView.
            datapoint.xProperty().addListener( (v, oldValue, newValue) -> {
                //Fetching the new value and inserting it in the XYChart, and then inserting it in the array
                staticDataSeries.getData().get(datapoint.getIndex()).setXValue(datapoint.getX().doubleValue());
                serialDataAccStatic[datapoint.getIndex()][1] = newValue.doubleValue();
            });
            datapoint.yProperty().addListener( (v, oldValue, newValue) -> {
                staticDataSeries.getData().get(datapoint.getIndex()).setYValue(datapoint.getY().doubleValue());
                serialDataAccStatic[datapoint.getIndex()][axis] = newValue.doubleValue();
            });
            tableDataStatic.add(datapoint);
            //********************************************************************************************************//
        }
        return staticDataSeries;
    }
    //***************************************************************************************************************//


    //***************************************************GET**********************************************//
    public  ObservableList<DataPoint2D> getTableDataStatic(){return tableDataStatic;}

    public  ObservableList<DataPoint2D> getTableDataAnimated() {return tableDataAnimated;}
    //****************************************************************************************************//

    //***************************************************RESET********************************************//
    public  void resetXYChartStatic(){
        //Reset Legend TODO
        staticDataSeries.getData().removeAll(staticDataSeries.getData());
        serialDataAccStatic = null; //Creating a new, empty matrix. Cannot make it null?
    }
    public  void resetXYChartAnimated(){
        //Reset Legend TODO
        animatedDataSeries.getData().removeAll(animatedDataSeries.getData());
        serialDataAccAnimated = null; //Creating a new, empty matrix. Cannot make it null?
    }
    //****************************************************************************************************//
}

