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
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Created by Eric on 2017-04-15.
 */
public class MainApp extends Application {
    private Stage mainWindow;
    public static void main(String[] args) { launch(args);
    }

    public void start(Stage primaryStage) throws Exception {
        //*******************************************SETUP OF MAINWINDOW************************************//
        mainWindow = primaryStage;
        mainWindow.getIcons().add(new Image("file:resources/images/1497470325_Map_-_Location_Solid_Style_30.png"));
        //Load the fxml-file
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));


        Scene scene = new Scene(root, 900, 500);
        mainWindow.setTitle("FXML-test");
        mainWindow.setScene(scene);
        mainWindow.show();

        //***************************************************************************************************//

        //**************************************** CLOSE PROGRAM - MAIN *********************************************//
        //Adding a function for the user to make sure that they want to close the program (we will also be able
        //to save data if we want)
        mainWindow.setOnCloseRequest(e -> {
            e.consume();
            closeProgram();
            //TODO - End threads
            System.exit(0);
        });
        //**********************************************************************************************************//
    }

    //**********************************CLOSE PROGRAM - METHOD ****************************************************//
    private void closeProgram(){
        Boolean answer = ConfirmBox.display("Close","Are you sure you want to exit?");
        if(answer){ //If the user wants to exit the program exit, else do nothing
            mainWindow.close();
            System.out.println("File saved, output message when we have this functionality");
        }
    }
}
