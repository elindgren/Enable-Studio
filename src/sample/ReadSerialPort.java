/**
 * Created by simon on 2017-06-13.
 */
package sample;

import com.fazecast.jSerialComm.*;
import com.sun.org.apache.xpath.internal.SourceTree;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Scanner;


public class ReadSerialPort  {
    public SerialPort[] portsList;
    private SerialPort port;
    public Scanner portScanner;
    private byte[] buffer;
    public double[][] continuousMatrix;
    private int count;

    public ReadSerialPort(){
        makeSerialPortList();
        this.getPortlist();
        //setPort(0);
        //initializePort();
        //setBuffer(3);
        continuousMatrix = new double[(int)(2.5*Math.pow(10,5))][14];
    }
    public ReadSerialPort(int port, int mode){
        makeSerialPortList();
        setPort(port);
        initializePort();
        setBuffer(mode);
        continuousMatrix = new double[(int)(2.5*Math.pow(10,5))][14];
    }

    private void makeSerialPortList(){
        portsList = SerialPort.getCommPorts();
    }
    private void setPort(int port){
        this.port = portsList[port];
    }
    private void openPort() {
        while (!port.isOpen()){
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
    private void scanPort(){
        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 100 , 100);
        portScanner = new Scanner(port.getInputStream());
    }
    private void initializePort() {
        openPort();
        scanPort();
    }
    public void writeBytesSerial(){
        port.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_SEMI_BLOCKING, 100, 100);
        port.writeBytes(buffer, buffer.length);
        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 100, 100); //reset to Scanner-mode
    }
    private void commandPromptOutput() {
        String tmp = "";
        while (portScanner.hasNextLine()){
            tmp = portScanner.nextLine();
            System.out.println(tmp);
            if (tmp.equals("EndOfLine")) {
                break;
            }
        }
    }
    private String readOneLineSerial() {
        while (!portScanner.hasNextLine()){
            ;
        }
        return portScanner.nextLine();
    }
    public String[] serialToStringArray() { //mode2
        writeBytesSerial();
        String tmp = readOneLineSerial();
        System.out.println("tmp = " + tmp);
        String[] save = new String[Integer.parseInt(tmp)-1]; // -1 because starts with 0

        int i = 0;
        while (portScanner.hasNextLine() && i < save.length){ //sometimes loses 1 row of data due to hasNextline
            save[i] = portScanner.nextLine();
            //System.out.println(save[i] + " " + i); //DEBUG
            i += 1;
        }
        return save;
    }
    public double[][] stringArrayToDoubleMatrix(String[] apa){ //mode2
        //String[] arrTmp = serialToStringArray();
        String[] arrTmp = apa;
        int length = arrTmp.length;
        double[][] save = new double[length][14];
        //String[] tmp;
        for(int i = 0; i < length; i++){
            String[] tmp = arrTmp[i].split(", ");
            //System.out.println(tmp[1]); // DEBUG
            for(int j = 0; j<14; j++){
                save[i][j] = Double.parseDouble(tmp[j]);
            }
        }
        return save;
    }
    public String[] fileToStringArray(int fileNbr) { //mode4
        writeBytesSerial();
        commandPromptOutput();
        setBuffer(fileNbr);
        return serialToStringArray();
    }
    public double[][] fileToDoubleMatrix(int fileNbr) { //mode4
        /*
        writeBytesSerial();
        commandPromptOutput();
        setBuffer(fileNbr);
        return stringArrayToDoubleMatrix();
        */
        String[] test = new String[303];
        String line;
        int i = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File("resources/log100.txt")));
            while((line = br.readLine()) != null){
                test[i] = line;
                //System.out.println("test i " + test[i]);
                i++;
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return stringArrayToDoubleMatrix(test);
    }
    public void deleteFile(int fileNbr) { //mode5
        writeBytesSerial();
        commandPromptOutput();
        setBuffer(fileNbr);
        writeBytesSerial();
        commandPromptOutput();
    }
    public void fileToFile(int fileNbr) { //Mode4
        writeBytesSerial();
        commandPromptOutput();
        setBuffer(fileNbr);
        File file = new File("log" + fileNbr + ".txt");
        if (!file.exists()){
            try {
                PrintWriter writer = new PrintWriter(new FileWriter(file,true));
                writeBytesSerial();
                String tmp = readOneLineSerial();
                System.out.println("tmp = " + tmp);

                int i = 0;
                while(portScanner.hasNextLine() && i < (Integer.parseInt(tmp) - 1)) {
                    writer.println(portScanner.nextLine());
                    i++;
                }
                writer.close();
            } catch (Exception e){
                System.out.println("ERROR");
            }
        } else {
            setBuffer(0); //No file is log0
            writeBytesSerial();
            System.out.println("File already exists");
        }

    }
    public void setBuffer(int nbr){
        buffer = ByteBuffer.allocate(4).putInt(nbr).array(); //1 = mode1 (Logging), 2 = mode2 (SD), 3 = mode3 (Serial Print)
    }
    private void getPortlist(){
        for(int i = 0; i < portsList.length; i++){
            System.out.println(portsList[i].getDescriptivePortName());
        }
    }
    public double[][] getContinuousMatrix(){
        return continuousMatrix;
    }
    public int getCount(){
        return count;
    }

    public void setCount(int count){
        this.count=count;
    }

    public static void main(String[] args) {
        ReadSerialPort test = new ReadSerialPort(0,4);
        test.fileToDoubleMatrix(100);

        /*
        test.continuousToDoubleMatrix();
        double[][] tmp = test.getContinuousMatrix();
        for (int i = 0; i <= 100; i++){
            for (int j = 0; j < 14; j++){
                System.out.print(tmp[i][j] + ", ");
            }
            System.out.println();
        }
        */
    }
}
