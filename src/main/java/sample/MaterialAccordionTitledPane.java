package sample;

import com.jfoenix.controls.JFXRippler;
import javafx.event.EventHandler;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;

public class MaterialAccordionTitledPane extends TitledPane {
    private JFXRippler rippler;

    public MaterialAccordionTitledPane(){
        super();

        //Adding css styling
        //this.getStylesheets().add("file:src/main/resources/fxml/agixmaterialfx.css");
        this.getStylesheets().add("file:classes/fxml/agixmaterialfx.css");
        this.getStyleClass().add("root");
        this.getStyleClass().add("material-accordion-titled-pane");

        //Creating rippler for ripple effect.
        rippler = new JFXRippler(this, JFXRippler.RipplerMask.CIRCLE, JFXRippler.RipplerPos.BACK);
    }
}
