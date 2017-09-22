package sample;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.gillius.jfxutils.chart.ChartPanManager;
import org.gillius.jfxutils.chart.StableTicksAxis;

/**
 * Created by simon on 2017-06-20.
 */
/*
public class InitializeStaticGraph implements Runnable {
    private Data data;
    private ChartPanManager panManagerStaticView;
    //TableView - Static View
    private TableView tableStatic;
    private TableColumn xDataStaticCol;
    private TableColumn yDataStaticCol;
    private XYChart.Series<Number,Number> seriesStatic;

    //LineChart - Static View
    private LineChart lineChartStatic;
    private StableTicksAxis xAxisStatic;
    private StableTicksAxis yAxisStatic;
    private Graph2D staticGraph;

    public InitializeStaticGraph(Data data, TableView tableStatic, TableColumn yDataStaticCol, TableColumn xDataStaticCol, LineChart lineChartStatic, StableTicksAxis xAxisStatic, StableTicksAxis yAxisStatic, Graph2D staticGraph, ChartPanManager panManagerStaticView, XYChart.Series<Number,Number> seriesStatic ){
        this.panManagerStaticView=panManagerStaticView;
        this.data=data;
        this.tableStatic=tableStatic;
        this.xDataStaticCol=xDataStaticCol;
        this.yDataStaticCol=yDataStaticCol;
        this.lineChartStatic=lineChartStatic;
        this.xAxisStatic=xAxisStatic;
        this.yAxisStatic=yAxisStatic;
        this.staticGraph=staticGraph;
        this.seriesStatic=seriesStatic;
    }
    public void run(){
        //Fetching data for table
        //tableStatic.setItems(data.getTableDataStatic());
        //tableStatic.getColumns().setAll(xDataStaticCol,yDataStaticCol);

        //Setup of series
        seriesStatic = data.getStaticDataAcc("x");
        staticGraph = new Graph2D(lineChartStatic, seriesStatic, xAxisStatic, yAxisStatic,"readStatic", panManagerStaticView);
        staticGraph.setup();
    }

    public XYChart.Series<Number,Number> getSeriesStatic(){
        return seriesStatic;
    }

}
*/
