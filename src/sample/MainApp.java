package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Created by Eric on 2017-04-15.
 */
public class MainApp extends Application {
    private Stage mainWindow;

    //Main timeline
    private static Timeline timeline;
    //Variable for storing actual frame
    private static Integer i=0;
    private static Integer secs=0;
    private static XYChart.Series<Number,Number> animatedSeries = new XYChart.Series<Number,Number>();
    private static XYChart.Series<Number,Number> staticSeries = new XYChart.Series<Number,Number>();

    public static void main(String[] args) {
        launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        mainWindow = primaryStage;
        //Load the fxml-file
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        Scene scene = new Scene(root, 900, 500);
        mainWindow.setTitle("FXML-test");
        mainWindow.setScene(scene);
        mainWindow.show();

        //**************************************** CLOSE PROGRAM *********************************************//
        //Adding a function for the user to make sure that they want to close the program (we will also be able
        //to save data if we want)
        mainWindow.setOnCloseRequest(e -> {
            e.consume();
            closeProgram();
        });
        //***************************************************************************************************//
    }

    public static void animatedChart() {
        //Creating a timeline for updating the graph
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE); //Indefinite cycles of the timeline finishing
        timeline.setAutoReverse(true);
        //****************** EVENT_HANDLER FOR KEYFRAME ***************************//
        EventHandler onFinished = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                //series.getData().add(new XYChart.Data<Number, Number>(secs, Math.random() * 10));
                animatedSeries.getData().add(Data.getAnimatedAcc(secs));
                secs++;
                //reset frametracker
                i = 0;
            }
        };
        //*************************************************************************//

        Duration duration = Duration.millis(20);

        //I don't know how to do actionhandling for keyframe with lambda expression
        KeyFrame keyFrame = new KeyFrame(duration, onFinished);
        timeline.getKeyFrames().add(keyFrame);
    }

    public static void staticChart(){
        staticSeries=Data.getStaticDataGraph();
    }
    //Method for closing program.
    private void closeProgram(){
        Boolean answer = ConfirmBox.display("Close","Are you sure you want to exit?");
        if(answer){ //If the user wants to exit the program exit, else do nothing
            mainWindow.close();
            System.out.println("File saved, output message when we have this functionality");
        }
    }

    public static void startAnimatedTimeline(){
        animatedChart();
        timeline.play();
    }

    public static void stopAnimatedTimeline(){
        timeline.stop();
    }

    public static void setAnimatedSeries(XYChart.Series ser){
        animatedSeries=ser;
        animatedSeries.getData().setAll(new XYChart.Data(0,0));
    }

    public static XYChart.Series getAnimatedSeries(){
        return animatedSeries;
    }

    public static XYChart.Series getStaticSeries(){return staticSeries;
    }

    public static void setStaticSeries(XYChart.Series ser){
        staticSeries=ser;
        staticSeries.getData().setAll(new XYChart.Data(0,0));
    }
}
