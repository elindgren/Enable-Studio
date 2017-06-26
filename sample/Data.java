package sample;
import com.sun.deploy.security.DeployURLClassPathCallback;
import com.sun.org.apache.xpath.internal.SourceTree;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.io.*;
import java.util.ConcurrentModificationException;

/**
 * Created by Eric on 2017-04-16.
 */
public class Data {
    private  ObservableList<DataPoint2D> dataStatic = FXCollections.observableArrayList();
    private  ObservableList<DataPoint2D> dataAnimated = FXCollections.observableArrayList();
    private  XYChart.Series<Number,Number> staticDataSeries= new XYChart.Series<>();
    private  XYChart.Series<Number,Number> animatedDataSeries= new XYChart.Series<>();
    private  double[][] fileData;
    private  double[][] continousData;
    private ReadSerialPort rp;
    private  int lastCount = 0;
    private double offset;
    private int offsetRow;

    public Data(ReadSerialPort rp){
        this.rp=rp;

    }

    public void readFile(File file, String series, boolean readFromChip, ObservableList<Integer> progressList) {
        //********************************SETUP OF DATA - from readserialport********************************//
        int axis;
        if (series == "x") {
            axis = 5;
        } else if (series == "y") {
            axis = 6;
        } else if (series == "z") {
            axis = 7;
        } else {
            axis = 5;
            System.out.println("Non-valid axis supplied. Using default axis (x)");
        }
        if (readFromChip) {
            rp.setBuffer(4);
            fileData = rp.fileToDoubleMatrix(19);
        }
        else{
            //Reads the whole file, row by row, and puts it in a string array.
            System.out.println("Accessing readFile");
            fileData = readFile(file, progressList);
            System.out.println("double[][] retrieved from readFile");
        }

        double offset = fileData[0][1]; //Used to set proper offset, as to make the graph begin at t=0
        //***************************************************************************************************//
        System.out.println("Beginning to setup datapoints");
        for (int i=0; i<fileData.length; i++ ) {
            progressList.set(0,i);
            //System.out.println("Starting line: " + i+1);
            //********************************** XYCHART.Series, Static*********************************************//
            XYChart.Data<Number, Number> staticDataPoint = new XYChart.Data<Number, Number>(fileData[i][1] - offset, fileData[i][axis]);
            staticDataSeries.getData().add(staticDataPoint);
            //******************************************************************************************************//

            //****************List of DataPoints2D, coupled to the double[][] used in the series above****************//
            DataPoint2D datapoint = new DataPoint2D(fileData[i][1] - offset, fileData[i][axis], i); //Number is the number of the object created, referring to its place in the array

            //Adding listeners to change the dataTable and the XYChart-data whenever a change occurs in the tableView.
            datapoint.xProperty().addListener((v, oldValue, newValue) -> {
                //Fetching the new value and inserting it in the XYChart, and then inserting it in the array
                staticDataSeries.getData().get(datapoint.getIndex()).setXValue(datapoint.getX().doubleValue());
                fileData[datapoint.getIndex()][1] = newValue.doubleValue();
            });
            datapoint.yProperty().addListener((v, oldValue, newValue) -> {
                staticDataSeries.getData().get(datapoint.getIndex()).setYValue(datapoint.getY().doubleValue());
                fileData[datapoint.getIndex()][axis] = newValue.doubleValue();
            });
            dataStatic.add(datapoint);
            //System.out.println("Line: " + i+1 + " completed.");
            //********************************************************************************************************//
        }
    }

    public void readContinouslyFromFile(File file, String series, boolean readFromChip, ObservableList<Integer> progressList){
        //********************************SETUP OF DATA - from readserialport********************************//
        int axis;
        if (series == "x") {
            axis = 5;
        } else if (series == "y") {
            axis = 6;
        } else if (series == "z") {
            axis = 7;
        } else {
            axis = 5;
            System.out.println("Non-valid axis supplied. Using default axis (x)");
        }
        if (readFromChip) {
            rp.setBuffer(4);
            fileData = rp.fileToDoubleMatrix(19);
        }
        else{
            //Reads the whole file, row by row, and puts it in a string array.
            System.out.println("Accessing readFile");
            fileData = readFile(file, progressList);
            System.out.println("double[][] retrieved from readFile");
        }

        double offset = fileData[0][1]; //Used to set proper offset, as to make the graph begin at t=0
        //***************************************************************************************************//
        System.out.println("Beginning to setup datapoints");
        for (int i=0; i<fileData.length; i++ ) {
            progressList.set(0,i);
            //System.out.println("Starting line: " + i+1);
            //********************************** XYCHART.Series, Static*********************************************//
            XYChart.Data<Number, Number> animatedDataPoint = new XYChart.Data<Number, Number>(fileData[i][1] - offset, fileData[i][axis]);
            staticDataSeries.getData().add(animatedDataPoint);
            //******************************************************************************************************//

            //****************List of DataPoints2D, coupled to the double[][] used in the series above****************//
            DataPoint2D datapoint = new DataPoint2D(fileData[i][1] - offset, fileData[i][axis], i); //Number is the number of the object created, referring to its place in the array

            //Adding listeners to change the dataTable and the XYChart-data whenever a change occurs in the tableView.
            datapoint.xProperty().addListener((v, oldValue, newValue) -> {
                //Fetching the new value and inserting it in the XYChart, and then inserting it in the array
                animatedDataSeries.getData().get(datapoint.getIndex()).setXValue(datapoint.getX().doubleValue());
                fileData[datapoint.getIndex()][1] = newValue.doubleValue();
            });
            datapoint.yProperty().addListener((v, oldValue, newValue) -> {
                animatedDataSeries.getData().get(datapoint.getIndex()).setYValue(datapoint.getY().doubleValue());
                fileData[datapoint.getIndex()][axis] = newValue.doubleValue();
            });
            dataAnimated.add(datapoint);
            //System.out.println("Line: " + i+1 + " completed.");
            //********************************************************************************************************//
        }
    }
    //****************************************************************************************************//

    public void readContinously(){
        //***********************************************SETUP OF DATA***********************************//
        if (lastCount == 0){
            //rp.setBuffer(6);
            //rp.continousToDoubleMatrix();
            continousData = rp.getContinuousMatrix();
            offsetRow = rp.getCount();
            offset = continousData[offsetRow][1]; //Finding offset in time from 0
            while (offset == 0){
                offsetRow = rp.getCount();
                offset = continousData[offsetRow][1];
                try
                {
                    Thread.sleep(0); //TODO TOO FAST TOO DRAW GRAPH WITHOUT THIS
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
            animatedDataPoint=new XYChart.Data<Number,Number>(continousData[i][1]-offset, continousData[i][5]);
            animatedDataSeries.getData().add(animatedDataPoint);
            //********************************************************************************************************//

            //****************List of DataPoints2D, coupled to the double[][] used in the series above****************//
            DataPoint2D datapoint = new DataPoint2D(continousData[i][1]-offset,continousData[i][5], i); //Number is the number of the object created, referring to its place in the array
            //Adding listeners to change the dataTable and the XYChart-data whenever a change occurs in the tableView.
            datapoint.xProperty().addListener( (v, oldValue, newValue) -> {
                //Fetching the new value and inserting it in the XYChart, and then inserting it in the array
                animatedDataSeries.getData().get(datapoint.getIndex()).setXValue(datapoint.getX().doubleValue());
                continousData[datapoint.getIndex()+offsetRow][1] = newValue.doubleValue();
            });
            datapoint.yProperty().addListener( (v, oldValue, newValue) -> {
                animatedDataSeries.getData().get(datapoint.getIndex()).setYValue(datapoint.getY().doubleValue());
                continousData[datapoint.getIndex()+offsetRow][5] = newValue.doubleValue();
            });
            dataStatic.add(datapoint);
            //System.out.println("tableDataAnimated " + tableDataAnimated.get(i));
            //********************************************************************************************************//
        }
        lastCount = newCount;
    }
    //****************************************************************************************************//

    //***************************************************GET**********************************************//

    //***************GET DATA*****************//
    public  ObservableList<DataPoint2D> getDataStatic(){return dataStatic;}

    public  ObservableList<DataPoint2D> getDataAnimated() {return dataAnimated;}
    //*****************************************//

    //*************LINE CHART DATA*************//
    public XYChart.Series<Number,Number> getStaticDataSeries(){
        return staticDataSeries;
    }
    //*****************************************//

    //*************LINE CHART DATA*************//
    public XYChart.Series<Number,Number> getAnimatedDataSeries(){
        return animatedDataSeries;
    }
    //*****************************************//
    //****************************************************************************************************//

    //***************************************************RESET********************************************//
    public  void resetDataStatic(){
        //Reset Legend TODO
        staticDataSeries.getData().removeAll(staticDataSeries.getData());
        dataStatic.remove(0,dataStatic.size());
        fileData = null; //Creating a new, empty matrix. Cannot make it null?
    }
    public  void resetDataAnimated(){
        //Reset Legend TODO
        animatedDataSeries.getData().removeAll(animatedDataSeries.getData());
        dataAnimated.remove(0,dataAnimated.size());
        fileData = null; //Creating a new, empty matrix. Cannot make it null?
    }
    //****************************************************************************************************//

    //*****************************************READ DATA FROM FILE****************************************//

    public double[][] readFile(File file, ObservableList<Integer> progressList){
    //public double[][] readFile(File file){
        String[] dataSet;
        int i=0;

        try {
            System.out.println("Trying to read file...");
            BufferedReader br = new BufferedReader(new FileReader(file));
            int rows=Integer.parseInt(br.readLine());
            System.out.println("rows= " + rows);
            progressList.set(1,rows);
            dataSet=new String[rows];
            String line;
            while((line=br.readLine()) != null){
                dataSet[i]=line;
                i++;
                int row=i;
                progressList.set(0,row);
                //System.out.println("Reading file lines. Line: " +i);
            }
        }catch(IOException e){
            System.out.println("FileNotFound");
            e.printStackTrace();
            dataSet=new String[0];
        }
        //********PARSE STRING[] TO DOUBLE[][]
        int length = dataSet.length;
        System.out.println(length);
        double[][] save = new double[length][14];
        String[] tmp;
        System.out.println("Parsing string[] to double[][]");
        for (int j = 0; j < length; j++) {
            tmp = dataSet[j].split(", ");
            //System.out.println(tmp[1]); // DEBUG
            for (int k = 0; k < 14; k++) {
                save[j][k] = Double.parseDouble(tmp[k]);
            }
        }
        System.out.println("String[] parsed to double[][]");
        return save;
    }
    //********************************************WRITE DATA TO FILE************************************************//
    public void writeFile(File file, ObservableList<Integer> progressList, boolean  readFromChip){
        double[][] data;
        if(readFromChip){
            data=continousData;
        }
        else{
            data=fileData;
        }
        if(data==null){
            System.out.println("Error in writeFile: fileData null. You have to have something to save!");
        }
        else{
            int length= fileData.length;
            String[] rows = new String[length];
            for(int i=0; i<length; i++){
                String currentRow="";
                for(int j=0; j<fileData[0].length; j++){
                    if(j != fileData[0].length) {
                        currentRow+= String.valueOf(fileData[i][j]) + ", ";
                    }
                    else{
                        currentRow+= String.valueOf(fileData[i][j]) + ",";
                    }
                }
                rows[i]=currentRow;
            }
            //Write to a specific file
            PrintWriter fileWriter=null;
            try{
                fileWriter = new PrintWriter(file);
                fileWriter.write(String.valueOf(length));
                fileWriter.println();
                for(int i=0; i<length; i++){
                    fileWriter.write(rows[i]);
                    fileWriter.println();
                }
                fileWriter.close();
                System.out.println("File written successfully");

            }catch(IOException e){
                System.out.println("Error in fileWriter!");
                e.printStackTrace();
                fileWriter.close();
            }
        }
    }
}

