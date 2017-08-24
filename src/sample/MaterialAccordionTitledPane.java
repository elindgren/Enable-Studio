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
        this.getStyleClass().add("-material-accordion-titled-pane");

        //Creating rippler for ripple effect.
        rippler = new JFXRippler(this);
    }
}
