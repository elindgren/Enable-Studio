package sample;

import javafx.collections.ObservableList;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.gillius.jfxutils.chart.StableTicksAxis;

/**
 * Created by Eric on 2017-08-09.
 */
public class OnScreenOverlay extends AnchorPane implements OnScreenOverlayInterface {
    private ReadSerialPort rp;
    private ObservableList<Integer> progressList;
    private ObservableList<Integer> statusList;
    private Data data;

    private TableView table;
    private TableColumn xCol;
    private TableColumn yCol;
    private LineChart<Number,Number> lineChart;
    private Graph2D graph;
    private StableTicksAxis xAxis;
    private StableTicksAxis yAxis;

    private ProgressBar progressBar;
    private Label progressLabel;
    private Slider timelineSlider;

    //**** Flags ****//
    private boolean readFromChip = false;
    private boolean cinematicMode = false;

    private boolean settingXHasBeenActivated = false;
    private boolean settingXIsActive = true;
    private boolean settingYHasBeenActivated = false;
    private boolean settingYIsActive = false;
    private boolean settingZHasBeenActivated = false;
    private boolean settingZIsActive = false;

    public OnScreenOverlay(ReadSerialPort rp, ProgressBar progressBar, ObservableList<Integer> progressList, Label progressLabel, ObservableList<Integer> statusList, TableView table, TableColumn xCol, TableColumn yCol, LineChart lineChart){
        this.statusList=statusList;
        this.progressList=progressList;
        this.rp=rp;
        this.progressBar = progressBar;
        this.progressLabel = progressLabel;
        data= new Data(rp);
        //this.timelineSlider = slider;
    }

    public void setupButtons(){

    }
    public void setupSlider(){

    }
    public void setupTable(){

    }
    public void setupStatusListener(){

    }

}
