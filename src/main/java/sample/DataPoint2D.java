package sample;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

/**
 * Created by ericl on 2017-06-14.
 */

public class DataPoint2D{
    private SimpleDoubleProperty x = new SimpleDoubleProperty(this, "x", 0);
    private SimpleDoubleProperty y = new SimpleDoubleProperty(this, "y", 0);
    private int index;


    public DataPoint2D(Number a, Number b, int i){
        x=new SimpleDoubleProperty(this, "x",a.doubleValue());
        y=new SimpleDoubleProperty(this, "y",b.doubleValue());
        index=i;
    }

    public Number getX() {
        return x.get();
    }

    public DoubleProperty xProperty(){
        return x;
    }
    public void setX(Number x){
        this.x.set(x.doubleValue());
    }

    public Number getY() {
        return y.get();
    }
    public DoubleProperty yProperty(){
        return y;
    }
    public void setY(Number y){
        this.y.set(y.doubleValue());
    }

    public int getIndex(){
        return index;
    }

}
