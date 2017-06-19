/**
 * Created by simon on 2017-06-15.
 */
public class ArrayTest {
    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE);
        System.out.println((int)(2*Math.pow(10,8)));
        String[] test = new String[(int)(2.5*Math.pow(10,8))];
        int i = 0;
        while (true){
            System.out.println(i++);
            try
            {
                Thread.sleep(1000);
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }
        }
    }
}
