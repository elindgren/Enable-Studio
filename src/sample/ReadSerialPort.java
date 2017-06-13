package sample;
import com.fazecast.jSerialComm.*;
//import arduino.*;
import com.sun.org.apache.xpath.internal.SourceTree;

import java.nio.ByteBuffer;
import java.util.Scanner;

/**
 * Created by simon on 2017-02-10.
 */
public class ReadSerialPort {
    public static SerialPort[] portsList;
    public static SerialPort port;
    private Scanner portScanner;
    private byte[] buffer;

    /*
    public ReadSerialPort(){
        makeSerialPortList();
        setPort(0);
        initializePort();
        buffer = ByteBuffer.allocate(4).putInt(2).array(); //1 = mode1 (Logging), 2 = mode2 (SD), 3 = mode3 (Serial Print)
    }
    */

    //Place-holder constructor
    public ReadSerialPort(){
        System.out.println("Dummy constrcutor in ReadSerialPort. Object created");
    }

    private void makeSerialPortList(){
        portsList = SerialPort.getCommPorts();
    }
    private void findChipPort(){
        //TODO CORRECT
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
    private String readOneLineSerial() {
        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 100 , 100); //Change back to Scanner-timeout
        while (!portScanner.hasNextLine()){
            ;
        }
        port.setComPortTimeouts(SerialPort. TIMEOUT_WRITE_SEMI_BLOCKING, 100, 100);
        return portScanner.nextLine();

    }
    private void writeOneByteSerial() {
        for (int i=0; i<buffer.length; i++){
            System.out.println(buffer[i]);
        }
        port.setComPortTimeouts(SerialPort. TIMEOUT_WRITE_SEMI_BLOCKING, 100, 100);
        System.out.println(port.writeBytes(buffer, buffer.length));
    }
    private void commandPromptOutput() {
        while (portScanner.hasNextLine()){
            System.out.println(portScanner.nextLine());
        }
    }
    //Place-holder serialToStringArray()
    private String[] serialToStringArray() {
        writeOneByteSerial();
        String tmp = readOneLineSerial();
        System.out.println("tmp = " + tmp);
        String[] save = new String[Integer.parseInt(tmp)];

        int i = 0;
        port.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 100 , 100);

        while (portScanner.hasNextLine() && i < save.length-1){ //Jump Problem || null on last row, need to fix
            save[i] = portScanner.nextLine();
            //System.out.println(save[i] + " " + i);
            i += 1;
        }

        return save;
    }
    public double[][] stringArrayToDoubleMatrix(){
        /*
        String[] arrTmp = serialToStringArray();
        int length = arrTmp.length-1;
        double[][] save = new double[length][14];
        String[] tmp;
        for(int i = 0; i < length; i++){
            tmp = arrTmp[i].split(", ");
            System.out.println(tmp[1]);
            for(int j = 0; j<14; j++){
                save[i][j] = Double.parseDouble(tmp[j]);
            }
        }
        */
        double[][] save = {{1,10},{2, 20},{3,30}, {4,5}, {5,15}};
        return save;
    }
}

