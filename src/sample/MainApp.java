package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Created by Eric on 2017-04-15.
 */
public class MainApp extends Application {
    Stage mainWindow;

    public void start(Stage primaryStage) throws Exception{
        mainWindow = primaryStage;
        //Load the fxml-file
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        Scene scene = new Scene(root,900,500);
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

    }

    public static void main(String[] args) {
        launch(args);
    }


    //Method for closing program.
    private void closeProgram(){
        Boolean answer = ConfirmBox.display("Close","Are you sure you want to close?");
        if(answer){ //If the user wants to exit the program exit, else do nothing
            mainWindow.close();
            System.out.println("File saved, output message when we have this functionality");
        }
    }
}
