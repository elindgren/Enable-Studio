package sample;

import com.jfoenix.controls.JFXRippler;
import javafx.scene.control.Accordion;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MaterialAccordion extends Accordion {
    private JFXRippler rippler;

    public MaterialAccordion(){
        super();
        //Adding css styling
        this.getStylesheets().add("file:src/sample/agixmaterialfx.css");
        this.getStyleClass().add("root");
        this.getStyleClass().add("material-accordion");
    }
}
