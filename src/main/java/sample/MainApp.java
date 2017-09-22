package sample;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialogLayout;
import javafx.application.Application;
import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.awt.*;
import java.util.Properties;

/**
 * Created by Eric on 2017-04-15.
 */
public class MainApp extends Application{
    private Stage mainWindow;

    public static void main(String[] args) {
        launch(args);
    }

    public void init(){


    }

    public void start(Stage primaryStage) throws Exception {
        /*
        SplashScreenPreLoader splash = new SplashScreenPreLoader();
        Stage splashStage = new Stage();
        splashStage.initStyle(StageStyle.TRANSPARENT);
        splash.start(splashStage);
        System.out.println("Launching splashscreen");
        */
        //*******************************************SETUP OF MAINWINDOW************************************//
        mainWindow = primaryStage;
        /*mainWindow.getIcons().add(new Image("file:src/main/resources/images/enable_no_text.png"));*/
        mainWindow.getIcons().add(new Image("file:classes/images/enable_no_text.png"));
        //Load the fxml-file
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("fxml/sample.fxml"));

        //JFoenix Decorator:
        //JFXDecorator decorator = new JFXDecorator(mainWindow,root);
        //decorator.setCustomMaximize(true);

        Scene scene = new Scene(root, 1200, 900); //Add root instead of decorator for default look

        mainWindow.setTitle("Enable Studio");
        mainWindow.setScene(scene);
        System.out.println("Delaying splashScreen");
        /*
        try{
            Thread.sleep(2000);
        }catch (Exception e){
            System.out.println("Exception in start delay");
            e.printStackTrace();
        }
        */
        System.out.println("Stopping splashscreen");
        //splashStage.hide();
        mainWindow.show();


        //***************************************************************************************************//

        //**************************************** CLOSE PROGRAM - MAIN *********************************************//
        //Adding a function for the user to make sure that they want to close the program (we will also be able
        //to save data if we want)
        mainWindow.setOnCloseRequest(e -> {
            mainWindow.setAlwaysOnTop(true);

            StackPane stackPane = new StackPane();
            JFXDialogLayout content = new JFXDialogLayout();
            JFXButton yesButton = new JFXButton("Yes");
            JFXButton noButton = new JFXButton("No");
            JFXButton helpButton = new JFXButton("Help me");


            content.setHeading(new Text("Close program"));
            content.setBody(new Text("Are you sure you want to exit? All unsaved data will be lost."));
            content.setActions(yesButton,noButton);

            JFXDialog dialog = new JFXDialog(stackPane, content, JFXDialog.DialogTransition.CENTER);
            dialog.show((StackPane)root);
            yesButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    e.consume();
                    System.exit(0);
                }
            });
            noButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    dialog.close();
                    e.consume();
                }
            });

            helpButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    getHostServices().showDocument("http://www.pentagon.gov");
                }
            });
            mainWindow.setAlwaysOnTop(false);
            e.consume(); //If the user presses outside of the dialog.






            /*
            Alert closeAlert = new Alert(Alert.AlertType.CONFIRMATION);
            closeAlert.setTitle("Exit");
            closeAlert.setHeaderText(null);
            closeAlert.setContentText("Are your sure you want to exit?");


            Optional<ButtonType> result = closeAlert.showAndWait();
            if(result.get() == ButtonType.OK){
                e.consume();
                //TODO - End threads
                System.exit(0);
            }
            else{
                e.consume();
            }
            */
        });
        //**********************************************************************************************************//
    }


    //**********************************CLOSE PROGRAM - METHOD ****************************************************//
    /* UNUSED AT THE MOMENT
    private void closeProgram(){
        Boolean answer = ConfirmBox.display("Close","Are you sure you want to exit?");
        if(answer){ //If the user wants to exit the program exit, else do nothing
            mainWindow.close();
            System.out.println("File saved, output message when we have this functionality");
        }
    }
    */
}
