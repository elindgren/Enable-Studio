package sample;

import com.jfoenix.controls.JFXProgressBar;
import javafx.animation.FadeTransition;
import javafx.application.Preloader;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashScreenPreLoader extends Preloader {
    JFXProgressBar bar;
    Stage stage;
    Scene splash;


    public SplashScreenPreLoader() {
        super();
        this.splash = createPreloaderScene();
    }

    private Scene createPreloaderScene() {
        bar = new JFXProgressBar();
        bar.getStylesheets().add("file:classes/fxml/agixmaterialfx.css");
        bar.getStyleClass().add("root");
        bar.getStyleClass().add("jfx-progressbar");
        bar.setPrefWidth(500);
        bar.setPrefHeight(5);
        //bar.setProgress(-1);
        StackPane pane = new StackPane();
        pane.setAlignment(Pos.BOTTOM_CENTER);
        pane.getChildren().add(bar);
        pane.getChildren().get(0).setTranslateY(-40);


        //BorderPane p = new BorderPane();
        //p.setBottom(bar);
        //p.setCenter(new StackPane());
        pane.setStyle("-fx-background-image: url('file:classes/images/splashscreenEnable.png');" +
                "-fx-background-size: 500, 300;" + "-fx-background-repeat: no-repeat;" + "-fx-background-color: dimgray");

        return new Scene(pane, 500, 300);
    }

    public void start(Stage stage) throws Exception {
        this.stage = stage;
        stage.setScene(createPreloaderScene());
        stage.show();
    }

    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        bar.setProgress(pn.getProgress());
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
            stage.hide();
        }
    }
}


