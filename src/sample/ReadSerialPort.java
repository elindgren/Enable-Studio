/**
 * Created by simon on 2017-06-19.
 */
package sample;
import com.fazecast.jSerialComm.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

public class ReadSerialPort {
    private SerialPort[] portsList;
    private SerialPort port;
    protected Scanner portScanner;
    private byte[] buffer;
    protected double[][] continuousMatrix;
    private int count;
    private int fileSize = 0; //Total number of lines
    private int row = 0;
    private ObservableList<Integer> statusList = FXCollections.observableArrayList(1);
    private ObservableList<Integer> progressList = FXCollections.observableArrayList();
    private int[] rowsToRead = new int[1];


    public ReadSerialPort() {
        makeSerialPortList();

    }

    public void setupPorts(){
        setPort(0);
        initializePort();
        setBuffer(3);
        this.port.setBaudRate(115200);
        continuousMatrix = new double[(int) (2.5 * Math.pow(10, 5))][14];
    }

    public ReadSerialPort(int port, int mode) {
        makeSerialPortList();
        setPort(port);
        initializePort();
        setBuffer(mode);
        this.port.setBaudRate(115200); // No effect
        continuousMatrix = new double[(int) (2.5 * Math.pow(10, 5))][14];
    }

    private void makeSerialPortList() {
        portsList = SerialPort.getCommPorts();
    }

    private void setPort(int port) {
        this.port = portsList[port];
    }

    private void openPort() {
        while (!port.isOpen()) {
            System.out.println("Connecting...");
            port.openPort();
        }
        System.out.println("Port Successfully Opened" + "\n");
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
        openPort();
        scanPort();
    }
    public void writeBytesSerial() {
        port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_SEMI_BLOCKING, 100, 100);
        port.writeBytes(buffer, buffer.length);
        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 100, 100); //reset to Scanner-mode
    }
    private String OldserialRead() { //NOT IN USE
        String tmp = "";
        String save = "";
        while (portScanner.hasNextLine()) {
            tmp = portScanner.nextLine();
            System.out.println(tmp);
            if (tmp.equals("EndOfLine")) {
                break;
            }
            save = tmp;
        }
        return save;

                /* //OLD while-loop in serialToStringArray
        while (true) {
            tmp = portScanner.nextLine();
            if (tmp.equals("EndOfLine")) {
                System.out.println(tmp);
                break;
            }
            save[i] = tmp;
            System.out.println(save[i] + " " + i); //DEBUG
            i++;
        }
        */
    }
    private String serialRead() {
        String tmp = portScanner.nextLine();
        String save = "";
        while(!tmp.equals("EndOfLine")){
            System.out.println(tmp);
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
        rowsToRead[0]=fileSize;
        String[] save = new String[fileSize];
        tmp = portScanner.nextLine();
        while(!tmp.equals("EndOfLine")){
            save[row] = tmp;
            //System.out.println(save[row] + " " + row); //DEBUG
            tmp = portScanner.nextLine();
            row++;
            progressList.set(0,row);
        }
        System.out.println(tmp);
        //fileSize = 0;
        row = 0;
        if(isConnected()){
            statusList.set(0,1);
        } else{
            statusList.set(0,0);
        }
        return save;
    }
    public double[][] stringArrayToDoubleMatrix(){ //mode2
        String[] arrTmp = serialToStringArray();
        int length = arrTmp.length;
        double[][] save = new double[length][14];
        String[] tmp;
        for(int i = 0; i < length; i++){
            tmp = arrTmp[i].split(", ");
            //System.out.println(tmp[1]); // DEBUG
            for(int j = 0; j<14; j++){
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
    public void listFiles() {
        setBuffer(3);
        writeBytesSerial();
        serialRead();
    }
    public boolean isConnected() {
        setBuffer(7);
        try{
            writeBytesSerial();
            if (serialRead().equals("Connected")){
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            return false;
        }
    }
    public void setBuffer(int nbr) {
        buffer = ByteBuffer.allocate(4).putInt(nbr).array(); //1 = mode1 (Logging), 2 = mode2 (Current File), 3 = mode3 (List File) 4 = mode4 (fileToXXX), 5 = mode5 (Delete File)
    }
    private void getPortlist() {
        for (int i = 0; i < portsList.length; i++) {
            System.out.println(portsList[i].getDescriptivePortName());
        }
    }
    public double[][] getContinuousMatrix() {
        return continuousMatrix;
    }
    public int getCount() {
        return count;
    }
    public ObservableList<Integer> getStatusList() {
        return statusList;
    }
    public ObservableList<Integer> getProgressList() {
        return progressList;
    }
    public int[] getRowsToRead(){
        return rowsToRead;
    }
    public void setCount(int count) {
        this.count = count;
    }


    public static void main(String[] args) {
        ReadSerialPort test = new ReadSerialPort(0,7);
        boolean tmp = test.isConnected();
        System.out.println(tmp);
    }


    public int getFileLength(File file) {
        int i = 2;
        String line = "";
        char ch;
        try {
            RandomAccessFile RandomFile = new RandomAccessFile(file, "r");
            ch = RandomFile.readChar();
            System.out.println("File length: " + RandomFile.length());
            while((byte)ch != 10){
                RandomFile.seek(RandomFile.length()-i);
                ch = (char)RandomFile.readByte();
                //System.out.println("ch = " + ch + " byte: " + (byte)ch);
                i++;
            }
            System.out.println("Found char");
            ch = (char)RandomFile.readByte();
            while (ch != ','){
                line += Character.toString(ch);
                ch = (char)RandomFile.readByte();
            }
            System.out.println("Closing RandomFile");
            RandomFile.close();
        } catch (IOException e) {
            System.out.println("Something went wrong :(");
            e.printStackTrace();
        }
        System.out.println("Returning integer rows");
        return Integer.parseInt(line) + 1;
    }


    //**************************ERIC's changes******************//

    public void continousToDoubleMatrix() {
        System.out.println("run");
        String[] tmp;
        count = 0;
        this.writeBytesSerial();
        while (this.portScanner.hasNextLine()) {
            tmp = this.portScanner.nextLine().split(", ");
            for (int i = 0; i < 14; i++) {
                this.continuousMatrix[count][i] = Double.parseDouble(tmp[i]);
            }
            //System.out.println(count);
            this.setCount(count);
            count++;
        }
    }
}
