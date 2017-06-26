/**
 * Created by simon on 2017-06-15.
 */
import java.util.*;
import java.io.*;

public class FileTest {
    public static void main(String[] args) {
        File testFile = new File("minTestFil");
        if(!testFile.exists()){
            try{
                PrintWriter test = new PrintWriter(new FileWriter(testFile,true));
                System.out.println("minTestFil created");
                test.println("Hello World!");
                test.close();
                test = new PrintWriter(new FileWriter(testFile,true));
                test.println("Hej Världen!");
                test.close();
            } catch (Exception e){
                System.out.println("ERROR");
            }
        }else {
            System.out.println("minTestFil already exists!");
        }
    }
}
