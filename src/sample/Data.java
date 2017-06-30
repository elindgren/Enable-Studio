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
    private  ObservableList<DataPoint2D> dataX = FXCollections.observableArrayList();
    private  ObservableList<DataPoint2D> dataY = FXCollections.observableArrayList();
    private  ObservableList<DataPoint2D> dataZ= FXCollections.observableArrayList();

    private  XYChart.Series<Number,Number> dataSeriesX= new XYChart.Series<>();
    private  XYChart.Series<Number,Number> dataSeriesY= new XYChart.Series<>();
    private  XYChart.Series<Number,Number> dataSeriesZ= new XYChart.Series<>();

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
        if (series == "x") {
            parseArray(fileData, "x", dataSeriesX, dataX, progressList, 0, fileData.length, offset);
        } else if (series == "y") {
            parseArray(fileData, "y", dataSeriesY, dataY, progressList, 0, fileData.length, offset);
        } else if (series == "z") {
            parseArray(fileData, "z", dataSeriesZ, dataZ, progressList, 0, fileData.length, offset);
        } else {
            parseArray(fileData, "x", dataSeriesX, dataX, progressList, 0, fileData.length, offset);
            System.out.println("Non-valid axis supplied. Using default axis (x)");
        }



    }

    public void readContinouslyFromFile(File file, String series, boolean readFromChip, ObservableList<Integer> progressList){
        //********************************SETUP OF DATA - from readserialport********************************//
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

        double offset = fileData[0][1];

        parseArray(fileData, "x", dataSeriesX, dataX, progressList, 0, fileData.length, offset);
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
        parseArray(continousData, "x", dataSeriesX, dataX, null, lastCount,newCount, offset);
        lastCount = newCount;
    }
    //****************************************************************************************************//

    public void parseArray(double[][] arr, String axis, XYChart.Series<Number,Number> series, ObservableList<DataPoint2D> list, ObservableList<Integer> progressList, int lastCount, int newCount, double offset){
        int nbr;
        if (axis == "x") {
            nbr = 5;
        } else if (axis == "y") {
            nbr = 6;
        } else if (axis == "z") {
            nbr = 7;
        } else {
            nbr = 5;
            System.out.println("Non-valid axis supplied. Using default axis (x)");
        }

        //***************************************************************************************************//
        System.out.println("Beginning to setup datapoints");
        for (int i=lastCount; i<newCount; i++ ) {
            if(progressList!=null) {
                progressList.set(0, i);
            }
            //System.out.println("Starting line: " + i+1);
            //********************************** XYCHART.Series, Static*********************************************//
            XYChart.Data<Number, Number> staticDataPoint = new XYChart.Data<Number, Number>(fileData[i][1] - offset, fileData[i][nbr]);
            series.getData().add(staticDataPoint);
            //******************************************************************************************************//

            //****************List of DataPoints2D, coupled to the double[][] used in the series above****************//
            DataPoint2D datapoint = new DataPoint2D(fileData[i][1] - offset, fileData[i][nbr], i); //Number is the number of the object created, referring to its place in the array

            //Adding listeners to change the dataTable and the XYChart-data whenever a change occurs in the tableView.
            datapoint.xProperty().addListener((v, oldValue, newValue) -> {
                //Fetching the new value and inserting it in the XYChart, and then inserting it in the array
                series.getData().get(datapoint.getIndex()).setXValue(datapoint.getX().doubleValue());
                arr[datapoint.getIndex()][1] = newValue.doubleValue();
            });
            datapoint.yProperty().addListener((v, oldValue, newValue) -> {
                series.getData().get(datapoint.getIndex()).setYValue(datapoint.getY().doubleValue());
                arr[datapoint.getIndex()][nbr] = newValue.doubleValue();
            });

            list.add(datapoint);
            //********************************************************************************************************//
        }
    }
    public void setupX(){
        double offset = fileData[0][1]; //Used to set proper offset, as to make the graph begin at t=0
        parseArray(fileData, "x", dataSeriesX, dataX, null, 0, fileData.length, offset);
    }
    public void setupY(){
        double offset = fileData[0][1]; //Used to set proper offset, as to make the graph begin at t=0
        parseArray(fileData, "y", dataSeriesY, dataY, null, 0, fileData.length, offset);
    }

    public void setupZ(){
        double offset = fileData[0][1]; //Used to set proper offset, as to make the graph begin at t=0
        parseArray(fileData, "z", dataSeriesZ, dataZ, null, 0, fileData.length, offset);
    }


    //***************************************************GET**********************************************//

    //***************GET DATA*****************//
    public  ObservableList<DataPoint2D> getDataX(){return dataX;}

    public  ObservableList<DataPoint2D> getDataY(){return dataY;}

    public  ObservableList<DataPoint2D> getDataZ(){return dataZ;}

    //*****************************************//

    //*************LINE CHART DATA*************//
    public XYChart.Series<Number,Number> getDataSeriesX(){
        return dataSeriesX;
    }

    public XYChart.Series<Number,Number> getDataSeriesY(){
        return dataSeriesY;
    }

    public XYChart.Series<Number,Number> getDataSeriesZ(){
        return dataSeriesZ;
    }

    //*****************************************//
    //****************************************************************************************************//

    //***************************************************RESET********************************************//
    public  void resetData(){
        //Reset Legend TODO
        dataSeriesX.getData().removeAll(dataSeriesX.getData());
        dataX.remove(0,dataX.size());
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
            //int rows=Integer.parseInt(br.readLine());
            int rows = rp.getFileLength(file);
            System.out.println("rows= " + rows);
            progressList.set(1,rows);
            System.out.println("Beginning to read file");
            dataSet=new String[rows];
            String line;
            while((line=br.readLine()) != null){
                dataSet[i]=line;
                i++;
                int row=i;
                progressList.set(0,row);
                System.out.println("Reading file lines. Line: " +i);
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
                //fileWriter.write(String.valueOf(length)); //TODO
                //fileWriter.println();
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

