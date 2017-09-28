/**
 * Created by simon on 2017-07-03.
 */

package sample;

import com.fazecast.jSerialComm.*;
import com.sun.org.apache.xpath.internal.SourceTree;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Scanner;
import java.util.concurrent.*;

public class ReadSerialPort {
    private SerialPort[] portsList;
    private SerialPort port;
    protected Scanner portScanner;
    private byte[] buffer;
    protected double[][] continuousMatrix;
    private int count;
    private int fileSize = 0; //Total number of lines
    private int row = 0;
    private final int nbrOfColumns = 17;
    private boolean connected = false;
    private boolean busy; //TODO

    private boolean chipHasBeenConnected = false;

    private ObservableList<Integer> statusList = FXCollections.observableArrayList();
    private ObservableList<Integer> progressList = FXCollections.observableArrayList();

    /*
    public ReadSerialPort() {
        makeSerialPortList();
        setPort(0);
        initializePort();
        isConnected();
        statusList.add(0,2);
        if(connected){
            statusList.set(0,1);
        }else{
            statusList.set(0,0);
        }
        setBuffer(6);
        //this.port.setBaudRate(250000);
        continuousMatrix = new double[(int) (2.5 * Math.pow(10, 5))][nbrOfColumns];

        // TODO try to read for 1 sec if connected
    }
    */
    public ReadSerialPort () {
        try {
            makeSerialPortListNew();
            statusList.add(0,0);
            if (setPortNew(0)) {
                if (openPortNew()) {
                    System.out.println("Port: " + port.getDescriptivePortName() + " Successfully opened");
                    System.out.println("Scanning port");
                    scanPort();
                    System.out.println("Running isConnected");
                    isConnectedNew();
                    if(connected) {
                        System.out.println("Trying to set buffer");
                        setBuffer(6);
                        statusList.add(0,1);
                        continuousMatrix = new double[(int) (2.5 * Math.pow(10, 5))][nbrOfColumns];
                        chipHasBeenConnected=true;
                    }
                    else{
                        System.out.println("The sensor wasn't connected");
                    }
                }
                else {
                    System.out.println("Port: " + port.getDescriptivePortName() + " Failed to open");
                }
            } else {
                System.out.println("No device connected. Check USB-cable");
            }
        } catch (Exception e) {
            //TODO
        }
        System.out.println("Starting UI...");
    }
    /*
    public ReadSerialPort(int port, int mode) {
        makeSerialPortList();
        setPort(port);
        initializePort();
        isConnected();
        setBuffer(mode);
        //this.port.setBaudRate(250000);
        continuousMatrix = new double[(int) (2.5 * Math.pow(10, 5))][nbrOfColumns];
    }
    */
    public ReadSerialPort(int port, int mode) {
        try {
            makeSerialPortListNew();
            if (setPortNew(port)) {
                if (openPortNew()) {
                    System.out.println("Port: " + this.port.getDescriptivePortName() + " Successfully opened");
                    scanPort();
                    isConnectedNew();
                    setBuffer(mode);
                    continuousMatrix = new double[(int) (2.5 * Math.pow(10, 5))][nbrOfColumns];
                }
                else {
                    System.out.println("Port: " + this.port.getDescriptivePortName() + " Failed to open");
                }
            } else {
                System.out.println("No device connected. Check USB-cable");
            }
        } catch (Exception e) {
            //TODO
        }
    }
    //#########################################NEW METHODS###############################
    private void makeSerialPortListNew() {
        portsList = SerialPort.getCommPorts();
    }
    private boolean setPortNew(int port) {
        if (portsList.length < 1) {
            return false;
        } else {
            this.port = portsList[port];
            return true;
        }
    }
    private boolean openPortNew() {
        int tmp = 0;
        while (!port.isOpen() && tmp < 10) {
            System.out.println("Connecting...");
            port.openPort();
            tmp++;
            delay(100);
        }
        return port.isOpen();
    }
    public boolean isConnectedNew() {
        //Setting the chips active port to the new one
        if(chipHasBeenConnected) {
            makeSerialPortListNew();
            if(setPortNew(portsList.length-1) ){
                if(openPortNew()){
                    scanPort();
                }
                else{
                    System.out.println("Port not opened");
                }
            }else{
                System.out.println("Port not found!");
            }
        }

        setBuffer(7);
        //Adding a timeout in case of the chip being in setup mode to not hinder the boot of the program.
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Object> task = new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                writeBytesSerial();
                delay(500);
                return serialRead().equals("Connected");
            }
        };
        Future<Object> future = executor.submit(task);
        try{
            Object result = future.get(3, TimeUnit.SECONDS);
            if(result!=null){
                connected=(boolean)result;
                if(connected){
                    chipHasBeenConnected=true;
                }
            }else{
                connected=false;
            }

        }
        catch (TimeoutException ex){
            System.out.println("Sensor didn't answer, continuing boot of program");
            connected = false;
            return connected;
        }
        catch(InterruptedException e){
            System.out.println("WriteByteSerialTask was interrupted, booting program in non-sensor mode");
            connected = false;
            e.printStackTrace();
        }
        catch(ExecutionException e){
            System.out.println("There was a problem executing the WriteByteSerialTask");
            connected = false;
            e.printStackTrace();
        }
        //writeBytesSerial();
        //connected = serialRead().equals("Connected");
        return connected;
    }
    //###################################################################################

    private void makeSerialPortList() {
        portsList = SerialPort.getCommPorts();
    }

    private void setPort(int port) {
        this.port = portsList[port];
    }

    public void newMeassurment() {

        setBuffer(8);
        writeBytesSerial();
        System.out.println("Starting new measurement");
    }

    private boolean openPort() {
        int tmp = 0;
        while (!port.isOpen() && tmp < 10) {
            System.out.println("Connecting...");
            port.openPort();
            tmp++;
            delay(100);
        }
        if (tmp < 10){
            System.out.println("Port Successfully Opened" + "\n");
            return true;
        } else {
            return false;
        }
    }
    private void closePort() {
        while (port.isOpen()) {
            System.out.println("Closing...");
            port.closePort();
        }
        System.out.println("Port Successfully Closed");
    }
    private void scanPort() {
        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 100, 100);
        portScanner = new Scanner(port.getInputStream());
    }
    private void initializePort() {
        if (openPort()) {
            scanPort();
        }
    }
    public void writeBytesSerial() {
        port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_SEMI_BLOCKING, 100, 100);
        port.writeBytes(buffer, buffer.length);
        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 100, 100); //reset to Scanner-mode
        System.out.println("Writing serial to chip");
    }
    private String serialRead() {
        String tmp = portScanner.nextLine();
        String save = "";
        while(!tmp.equals("EndOfLine")){
            System.out.println(tmp); //DEBUG
            save = tmp;
            tmp = portScanner.nextLine();
        }
        return save;
    }
    public String[] serialToStringArray() { //mode2
        writeBytesSerial();
        String tmp = serialRead();
        System.out.println("tmp = " + tmp);
        fileSize = Integer.parseInt(tmp);
        String[] save = new String[fileSize];
        tmp = portScanner.nextLine();
        while(!tmp.equals("EndOfLine")){
            save[row] = tmp;
            //System.out.println(save[row] + " " + row); //DEBUG
            tmp = portScanner.nextLine();
            row++;
        }
        System.out.println(tmp);
        fileSize = 0;
        row = 0;
        return save;
    }
    public double[][] stringArrayToDoubleMatrix(){ //mode2
        String[] arrTmp = serialToStringArray();
        int length = arrTmp.length;
        double[][] save = new double[length][nbrOfColumns];
        String[] tmp;
        for(int i = 0; i < length; i++){
            tmp = arrTmp[i].split(", ");
            //System.out.println(tmp[1]); // DEBUG
            for(int j = 0; j<nbrOfColumns; j++){
                save[i][j] = Double.parseDouble(tmp[j]);
            }
        }
        return save;
    }
    public String[] fileToStringArray(int fileNbr) { //mode4
        writeBytesSerial();
        serialRead();
        setBuffer(fileNbr);
        return serialToStringArray();
    }
    public double[][] fileToDoubleMatrix(int fileNbr) { //mode4
        writeBytesSerial();
        serialRead();
        setBuffer(fileNbr);
        return stringArrayToDoubleMatrix();
    }
    public void fileToFile(int fileNbr) { //Mode4
        writeBytesSerial();
        serialRead();
        setBuffer(fileNbr);
        File file = new File("log" + fileNbr + ".txt");
        if(!file.exists()){
            try {
                PrintWriter writer = new PrintWriter(new FileWriter(file,true));
                writeBytesSerial();
                fileSize =  Integer.parseInt(serialRead());
                String tmp = portScanner.nextLine();
                while(!tmp.equals("EndOfLine")){
                    writer.println(tmp);
                    tmp = portScanner.nextLine();
                    row++;
                }
                System.out.println("EndOfLine");
                fileSize = 0;
                row = 0;
                writer.close();
            } catch (Exception e){
                System.out.println("ERROR");
            }
        } else {
            setBuffer(0); //No file is log0
            writeBytesSerial();
            System.out.println("File alreadu exists");
        }
    }
    private void deleteFile(int fileNbr) { //mode5
        setBuffer(5);
        writeBytesSerial();
        serialRead();
        setBuffer(fileNbr);
        writeBytesSerial();
        serialRead();
    }
    public String[] listFiles() {
        setBuffer(3);
        writeBytesSerial();
        String tmp = portScanner.nextLine();
        String[] files = new String[1000];
        int i=0;
        while(!tmp.equals("EndOfLine")){
            //System.out.println(tmp); //DEBUG
            files[i] = tmp;
            tmp = portScanner.nextLine();
            i++;
        }
        return files;
    }
    public boolean isConnected() {
        setBuffer(7);
        try {
            writeBytesSerial();
            connected = serialRead().equals("Connected");
            return connected;
        } catch (Exception e) {
            connected = false;
            return false;
        }
    }
    public void setBuffer(int nbr) {
        buffer = ByteBuffer.allocate(4).putInt(nbr).array(); //1 = mode1 (Logging), 2 = mode2 (Current File), 3 = mode3 (List File) 4 = mode4 (fileToXXX), 5 = mode5 (Delete File)
    }
    private void getPortlist() {
        System.out.println("Portlist length: " + portsList.length);
        for (int i = 0; i < portsList.length; i++) {
            System.out.println("Printing port number: " + i);
            System.out.println(portsList[i].getDescriptivePortName());
        }
        System.out.println("Printing of commPorts has completed.");
    }
    public int getFileLength(File file) {
        int i = 2;
        String line = "";
        char ch;
        try {
            RandomAccessFile RandomFile = new RandomAccessFile(file, "r");
            ch = RandomFile.readChar();
            System.out.println(RandomFile.length());
            while((byte)ch != 10){
                RandomFile.seek(RandomFile.length()-i);
                ch = (char)RandomFile.readByte();
                System.out.println("ch = " + ch + " byte: " + (byte)ch);
                i++;
            }
            //System.out.println("finish");
            ch = (char)RandomFile.readByte();
            while (ch != ','){
                line += Character.toString(ch);
                System.out.println(line);
                ch = (char)RandomFile.readByte();
            }
            RandomFile.close();
        } catch (IOException e) {
            System.out.println("Something went wrong :(");
            e.printStackTrace();
        }
        return (int)Double.parseDouble(line) + 1;
    }
    public void continousToDoubleMatrix() {
        //System.out.println("Starting continousToDoubleMatrix");
        String[] tmp;
        count = 0;
        writeBytesSerial();
        while (portScanner.hasNextLine()) {
            tmp = portScanner.nextLine().split(", ");
            for (int i = 0; i < nbrOfColumns; i++) {
                continuousMatrix[count][i] = Double.parseDouble(tmp[i]);
            }
            //System.out.println("Current count in continous to doublematrix " + count);
            setCount(count);
            count++;
        }
    }
    public void delay(int time) {
        try {
            Thread.sleep(time);
        } catch(InterruptedException ex)
        {
            Thread.currentThread().interrupt();
        }
    }
    public double[][] getContinuousMatrix() {
        return continuousMatrix;
    }
    public int getCount() {
        return count;
    }
    public int getFileSize() {
        return fileSize;
    }
    public int getRow() {
        return row;
    }
    public void setCount(int count) {
        this.count = count;
    }

    public static void main(String[] args) {
        ReadSerialPort test = new ReadSerialPort();
        System.out.println("Printing ports");
        test.getPortlist();
        System.out.println("Have returned to main method. ");
        System.exit(0);



        /*
        GeoToKart GK = new GeoToKart(57.433407, 12.033789, 1.40);
        double[][] tmp = test.fileToDoubleMatrix(28);
        double[] xCoord = new double[tmp.length];
        double[] yCoord = new double[tmp.length];

        for (int i = 0; i < tmp.length; i++){
            xCoord[i] = GK.xLocal(GK.R(tmp[i][2]), tmp[i][2], tmp[i][3], tmp[i][4]);
            yCoord[i] = GK.yLocal(GK.R(tmp[i][2]), tmp[i][2], tmp[i][3], tmp[i][4]);
            System.out.println(xCoord[i] + ", " + yCoord[i]);
        }
        */
        /*
        GeoToKart eval = new GeoToKart();
        double[][] tmp = test.fileToDoubleMatrix(67);
        double[] diffx = new double[tmp.length-1];
        double[] diffy = new double[tmp.length-1];
        double[] diffz = new double[tmp.length-1];
        double[] distance = new double[tmp.length-1];

        for (int i = 0; i < tmp.length-1; i++){
            diffx[i] = eval.x(eval.R(tmp[i+1][2]), tmp[i+1][2], tmp[i+1][3], tmp[i+1][4]) -
                    eval.x(eval.R(tmp[100][2]), tmp[100][2], tmp[100][3], tmp[100][4]);
            diffy[i] = eval.y(eval.R(tmp[i+1][2]), tmp[i+1][2], tmp[i+1][3], tmp[i+1][4]) -
                    eval.y(eval.R(tmp[100][2]), tmp[100][2], tmp[100][3], tmp[100][4]);
            diffz[i] = eval.z(eval.R(tmp[i+1][2]), tmp[i+1][2], tmp[i+1][4]) -
                    eval.z(eval.R(tmp[100][2]), tmp[100][2], tmp[100][4]);
        }

        for (int j = 0; j < tmp.length-1; j++){
            distance[j] = Math.sqrt(Math.pow(diffx[j],2) + Math.pow(diffy[j],2) + Math.pow(diffz[j],2));
            System.out.println(distance[j] + " metres");
        }
        */
    }

    public ObservableList<Integer> getStatusList() {
        return statusList;
    }

    public ObservableList<Integer> getProgressList() {
        return progressList;
    }

    public boolean getConnected(){
        return connected;
    }
}
