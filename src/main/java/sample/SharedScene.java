package sample;

import javafx.scene.Parent;

/* Contact interface between application and preloader */
public interface SharedScene {
    /* Parent node of the application */
    Parent getParentNode();
}
