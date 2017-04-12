package sample;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 * Created by ericl on 2017-04-12.
 */
public class ConfirmBox {

    private static boolean answer = false;

    public static boolean display(String title, String message){
        Stage window = new Stage();
        window.setTitle(title);
        window.setWidth(300);
        Label label = new Label(message);
        Button b1 = new Button("Yes");
        Button b2 = new Button("No");

        FlowPane layout = new FlowPane();
        layout.setAlignment(Pos.CENTER);
        layout.setHgap(20);
        layout.getChildren().addAll(label,b1,b2);

        b1.setOnAction(e-> {
           answer = true;
            window.close();
        });

        b2.setOnAction(e->{
            window.close();
        });

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();

        return answer;
    }

    public boolean getAnswer(){
        return answer;
    }

}
