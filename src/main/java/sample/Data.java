package sample;
/*
import com.sun.deploy.security.DeployURLClassPathCallback;
import com.sun.org.apache.xpath.internal.SourceTree;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;
import com.sun.xml.internal.ws.dump.LoggingDumpTube;
*/
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;

import java.io.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

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
    private GeoToKart GK;
    private  int lastCount = 0; // NORMAL = 0 TODO
    private int measurementIteration = 0;
    private double offset;
    private int offsetRow;

    private boolean positionMode = false;
    private boolean resultingData = false;

    private int xAxis = 1;
    private int yAxis = 10;

    public Data(ReadSerialPort rp){
        this.rp=rp;
        GK = new GeoToKart(57.433407, 12.033789, 1.40);
    }

    public void readFile(File file, boolean readFromChip, ObservableList<Integer> progressList) {
        //********************************SETUP OF DATA - from readserialport********************************//
        if (readFromChip) {
            rp.setBuffer(4);
            fileData = rp.fileToDoubleMatrix(32);
            System.out.println("Reading file from chip");
        }
        else{
            //Reads the whole file, row by row, and puts it in a string array.
            System.out.println("Accessing readFile");
            fileData = readFile(file, progressList);
            System.out.println("double[][] retrieved from readFile");
        }
        double offset = fileData[0][1]; //Used to set proper offset, as to make the graph begin at t=0
        if (yAxis == 10) {
            parseArray(fileData, dataSeriesX, dataX, progressList, 0, fileData.length, offset);
        } else if (yAxis == 11) {
            parseArray(fileData, dataSeriesY, dataY, progressList, 0, fileData.length, offset);
        } else if (yAxis == 12) {
            parseArray(fileData,  dataSeriesZ, dataZ, progressList, 0, fileData.length, offset);
        } else {
            parseArray(fileData, dataSeriesX, dataX, progressList, 0, fileData.length, offset);
            System.out.println("Non-valid axis supplied. Using default axis (x)");
        }
    }

    public void readContinouslyFromFile(File file, boolean readFromChip, ObservableList<Integer> progressList){
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

        parseArray(fileData, dataSeriesX, dataX, progressList, 0, fileData.length, offset);
    }
    //****************************************************************************************************//

    public void readContinously(){
        //***********************************************SETUP OF DATA***********************************//
        if (lastCount == 0){
            rp.setBuffer(6);
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
            //System.out.println("Lastcount = " + lastCount);
        }

        int newCount = rp.getCount(); //Used to set proper offset, as to make the graph begin at 0.
        //System.out.println("Lastcount = " + newCount);
        //***************************************************************************************************//
        parseArray(continousData, dataSeriesX, dataX, null, lastCount, newCount, offset);
        lastCount = newCount;
    }
    //****************************************************************************************************//

    public void parseArray(double[][] arr, XYChart.Series<Number,Number> series, ObservableList<DataPoint2D> list, ObservableList<Integer> progressList, int lastCount, int newCount, double offset){

        //Pre-allocates two temporary lists
        List seriesList = new ArrayList();
        if(progressList!=null) {
            seriesList = new ArrayList(progressList.get(1));
        }else{
            //System.out.println("progressList is null in ParseArray");
        }
        //List dataList = new ArrayList(progressList.get(1));
        //***************************************************************************************************//
        //System.out.println("Beginning to setup datapoints");
        for (int i=lastCount; i<newCount; i++ ) {
            if(progressList!=null) {
                progressList.set(0, i);
            }
            //********************************** XYCHART.Series, Static*********************************************//
            XYChart.Data<Number, Number> staticDataPoint;
            DataPoint2D dataPoint;
            if(positionMode){
                staticDataPoint = new XYChart.Data<Number, Number>(GK.xLocal(GK.R(arr[i][2]), arr[i][2], arr[i][3], arr[i][4]),GK.yLocal(GK.R(arr[i][2]), arr[i][2], arr[i][3], arr[i][4]));
                dataPoint = new DataPoint2D(GK.xLocal(GK.R(arr[i][2]), arr[i][2], arr[i][3], arr[i][4]),GK.yLocal(GK.R(arr[i][2]), arr[i][2], arr[i][3], arr[i][4]),i);
            }else if(resultingData){
                staticDataPoint = new XYChart.Data<Number, Number>(arr[i][xAxis] - offset, Math.sqrt(Math.pow(arr[i][yAxis],2)+Math.pow(arr[i][yAxis+1],2)+Math.pow(arr[i][yAxis+2],2)));
                dataPoint = new DataPoint2D(arr[i][xAxis] - offset, Math.sqrt(Math.pow(arr[i][yAxis],2)+Math.pow(arr[i][yAxis+1],2)+Math.pow(arr[i][yAxis+2],2)), i);
            }
            else {
                staticDataPoint = new XYChart.Data<Number, Number>(arr[i][xAxis] - offset, arr[i][yAxis]);
                dataPoint = new DataPoint2D(arr[i][xAxis] - offset, arr[i][yAxis], i); //Number is the number of the object created, referring to its place in tFhe array
            }

            seriesList.add(staticDataPoint);
            //******************************************************************************************************//

            //****************List of DataPoints2D, coupled to the double[][] used in the series above****************//

            //Adding listeners to change the dataTable and the XYChart-data whenever a change occurs in the tableView.
            dataPoint.xProperty().addListener((v, oldValue, newValue) -> {
                //Fetching the new value and inserting it in the XYChart, and then inserting it in the array
                series.getData().get(dataPoint.getIndex()).setXValue(dataPoint.getX().doubleValue());
                arr[dataPoint.getIndex()][xAxis] = newValue.doubleValue();
            });
            dataPoint.yProperty().addListener((v, oldValue, newValue) -> {
                series.getData().get(dataPoint.getIndex()).setYValue(dataPoint.getY().doubleValue());
                arr[dataPoint.getIndex()][yAxis] = newValue.doubleValue();
            });
            //dataList.add(datapoint);
            list.add(dataPoint);
            measurementIteration++; //Used to keep track of how many datapoints are in the current measurement
            //********************************************************************************************************//
        }
        //System.out.println("************* READ CONTINOUSLY ************//");
        //**SERIES**//
        ObservableList<XYChart.Data<Number,Number>> seriesData = FXCollections.observableArrayList(seriesList);
        series.getData().addAll(seriesData);

        //**DATA**//
        //ObservableList<DataPoint2D> tableData = FXCollections.observableArrayList(dataList);
        //list=tableData;

    }
    public void setupX(){
        System.out.println("Setup x");
        this.setData(1, yAxis);
        double offset = fileData[0][1]; //Used to set proper offset, as to make the graph begin at t=0
        parseArray(fileData, dataSeriesX, dataX, null, 0, fileData.length, offset);
    }
    public void setupY(){
        System.out.println("Setup y");
        this.setData(1,yAxis+1);
        double offset = fileData[0][1]; //Used to set proper offset, as to make the graph begin at t=0
        parseArray(fileData, dataSeriesY, dataY, null, 0, fileData.length, offset);
    }

    public void setupZ(){
        System.out.println("Setup z");
        this.setData(1,yAxis+2);
        double offset = fileData[0][1]; //Used to set proper offset, as to make the graph begin at t=0
        parseArray(fileData, dataSeriesZ, dataZ, null, 0, fileData.length, offset);
    }



    //********************************* MATHEMATICAL OPERATIONS **********************************//

    public int findMaxIndex(){
        double xMax=fileData[0][yAxis]; //TODO Correct? Check formatting on files
        double xNew;
        int index=0;
        for(int i = 1; i<fileData.length; i++){
            xNew = fileData[i][yAxis];
            if(xMax<xNew){
                xMax=xNew;
                index=i;
            }
            else{
                //Do nothing
            }
        }
        return index;
    }

    public int findMinIndex(){
        double xMin=fileData[0][yAxis];
        double xNew;
        int index=0;
        for(int i = 1; i<fileData.length; i++){
            xNew = fileData[i][yAxis];
            if(xMin>xNew){
                xMin=xNew;
                index=i;
                System.out.println("New index: " + index);
            }
            else{
                //Do nothing
            }
        }
        System.out.println("Min point: " + fileData[index][5]);
        return index;
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

    //************ FILE DATA ****************//
    public double[][] getFileData(){
        return fileData;
    }

    public boolean getDataMode(){
        return positionMode;
    };
    //****************************************************************************************************//

    //***************************************************RESET********************************************//
    public  void resetData(){
        //Reset Legend TODO
        //dataSeriesX.getData().removeAll(dataSeriesX.getData());
        dataSeriesX=new XYChart.Series<>();
        dataX.remove(0,dataX.size()); //TODO Make this work with larger files, >20000 rows

        //dataSeriesY.getData().removeAll(dataSeriesY.getData());
        dataSeriesY=new XYChart.Series<>();
        dataY.remove(0,dataY.size()); //TODO Make this work with larger files, >20000 rows

        //dataSeriesZ.getData().removeAll(dataSeriesZ.getData());
        dataSeriesZ=new XYChart.Series<>();
        dataZ.remove(0,dataZ.size()); //TODO Make this work with larger files, >20000 rows

        fileData = null; //Creating a new, empty matrix. Cannot make it null?
        measurementIteration = 0;
        resultingData=false;
        xAxis=1;
        yAxis=10;
    }
    //****************************************************************************************************//

    //***************************************************SET**********************************************//

    public void setData(int x, int y){
        //1 = time, 10-12 = x,y,z respectively.
        xAxis = x;
        yAxis = y;
    }

    public void setDataMode(String str){
        if(str=="Acceleration"){
            this.setData(1,10);
            positionMode = false;
        }
        else if(str=="GPS"){
            this.setData(1,13);
            positionMode = false;
        }
        else if(str=="Position"){
            positionMode=true;
        }
        else if (str=="Magnetic"){
            this.setData(1,16);
            positionMode = false;
        }
        else if (str=="Rotation"){
            this.setData(1,8);
            positionMode = false;
        }
        else if(str=="Resultant"){
            resultingData=true;
            positionMode=false;
        }
    }

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
        double[][] save = new double[length][17];
        String[] tmp;
        System.out.println("Parsing string[] to double[][]");
        for (int j = 0; j < length; j++) {
            tmp = dataSet[j].split(", ");
            //System.out.println(tmp[1]); // DEBUG
            for (int k = 0; k < 16; k++) {
                save[j][k] = Double.parseDouble(tmp[k]);
            }
        }
        System.out.println("String[] parsed to double[][]");
        return save;
    }
    //********************************************WRITE DATA TO FILE************************************************//
    public void writeFile(File file, ObservableList<Integer> progressList, boolean  readFromChip){
        double[][] dataCopy;
        if(readFromChip){
            dataCopy=continousData;
        }
        else{
            dataCopy=fileData;
        }
        //System.out.println("Data length: " + dataCopy.length);
        System.out.println(fileData[0].length);
        //double[][] data = new double[measurementIteration][17]; //TODO Temporary, as the prerequisite for this to work is that accX has been measured
        double[][] data = new double[fileData.length][dataCopy[0].length];
        for(int i = 0; i<data.length; i++){ //Can't have more than around 10-20k datapoints
            for(int j=0; j < data[0].length; j++) {
                data[i][j] = dataCopy[i][j];
                System.out.print(data[i][j] + ", ");
            }
            System.out.println();
        }
        //data=dataCopy;
        //TODO Known bug: The process doesn't copy over the last column of the fileData matrix for some reason. It's all zeroes.
        if(data==null){
            System.out.println("Error in writeFile: fileData null. You have to have something to save!");
        }
        else{
            int length= data.length;
            progressList.set(1, length);
            String[] rows = new String[length];
            for(int i=0; i<length; i++){
                String currentRow = "";
                if(readFromChip) { //If it's a measurement, then it doesn't have the line indexies.
                     currentRow = i + ", ";
                }
                for(int j=0; j<data[0].length; j++){
                    if (j != data[0].length - 1) {
                        currentRow += String.valueOf(data[i][j]) + ", ";
                    } else {
                        currentRow += String.valueOf(data[i][j]);
                    }
                }

                rows[i]=currentRow;
                progressList.set(0,i);
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
                    progressList.set(0,i);
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

