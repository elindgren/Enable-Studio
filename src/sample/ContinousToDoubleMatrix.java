package sample;

/**
 * Created by simon on 2017-06-16.
 */
public class ContinousToDoubleMatrix implements Runnable {
    private ReadSerialPort rp;
    private int count;
    public ContinousToDoubleMatrix(ReadSerialPort rp){
        this.rp=rp;
    }
    public void run(){
        System.out.println("run");
        String[] tmp;
        count = 0;
        rp.writeBytesSerial();
        while (rp.portScanner.hasNextLine()){
            tmp = rp.portScanner.nextLine().split(", ");
            for (int i = 0; i < 14; i++){
                rp.continuousMatrix[count][i] = Double.parseDouble(tmp[i]);
            }
            //System.out.println(count);
            rp.setCount(count);
            count++;
        }
    }
}
