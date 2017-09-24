package sample;

import com.jfoenix.controls.*;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.gillius.jfxutils.chart.StableTicksAxis;

import java.awt.*;
import java.io.File;

/**
 * Created by ericl on 2017-06-29.
 */
public class StandardView implements View {

    private int measurementIndex = 0;
    private int frameTime = 10; //Polling rate of the cinematic view
    //**** Inter-class communication ****//
    private Data data;
    private ReadSerialPort rp;
    private JFXProgressBar progressBar;
    private ObservableList<Integer> progressList;
    private ObservableList<Integer> statusList;

    private Label onScreenArrow;

    //**** CONTAINERS ****//
    private AnchorPane onScreenPaneSlide;
    private AnchorPane onScreenPaneHover;
    private TabPane tabPane;
    private Tab tab;
    private TableView table;
    private TableColumn xCol;
    private TableColumn yCol;
    private LineChart<Number, Number> lineChart;
    private Graph2D graph;
    private StableTicksAxis xAxis;
    private StableTicksAxis yAxis;


    //**** Buttons ****//
    private JFXButton chartSetting;
    private JFXButton readButton;
    private JFXButton resetButton;
    private JFXButton saveButton;
    private ToggleButton chipModeToggle;
    private ToggleButton cinematicModeToggle;

    private JFXCheckBox settingX;
    private JFXCheckBox settingY;
    private JFXCheckBox settingZ;
    private JFXCheckBox settingClickData;
    private JFXCheckBox settingGPSData;
    private JFXCheckBox settingForceZeroInRange;

    //**** SLIDER ****//
    private Slider timelineSlider;

    //**** Flags ****//
    private boolean readFromChip = false;
    private boolean cinematicMode = false;

    private boolean settingXIsActive = true;
    private boolean settingYIsActive = false;
    private boolean settingZIsActive = false;

    private boolean statusChart = false; //TODO Change data so that it is the resulting data
    private boolean clickStatusChart = false;
    private int statusPan = 1;

    //**MATH**//
    private boolean findMaxFlag = false;
    private boolean findMinFlag = false;

    //**** DATA ****//
    private ObservableList<DataPoint2D> dataListX = FXCollections.observableArrayList();
    private ObservableList<DataPoint2D> dataListY = FXCollections.observableArrayList();
    private ObservableList<DataPoint2D> dataListZ = FXCollections.observableArrayList();
    private XYChart.Series<Number, Number> seriesX = new XYChart.Series<Number, Number>();
    private XYChart.Series<Number, Number> seriesY = new XYChart.Series<>();
    private XYChart.Series<Number, Number> seriesZ = new XYChart.Series<>();
    //**** TASKS ****//
    private Task<Void> readTask;
    private Task<Void> saveTask;

    //**** TIMELINE ****//
    KeyFrame keyFrameAnimated;
    private Timeline timeline;
    private boolean timelineIsFinished = false;
    private boolean timelineIsStopped = false;
    private int timelineIteration = 0;

    public StandardView(boolean isStandard, Tab tab, TabPane tabPane, ReadSerialPort rp, JFXProgressBar progressBar, ObservableList<Integer> progressList, ObservableList<Integer> statusList) {
        this.statusList = statusList;
        this.progressList = progressList;
        this.tabPane = tabPane;
        this.rp = rp;
        this.progressBar = progressBar;
        this.tab = tab;
        data = new Data(rp);

        if (isStandard) {
            table = (TableView) ((StackPane) ((Parent) tab.getContent()).getChildrenUnmodifiable().get(0)).getChildren().get(0);
            lineChart = (LineChart<Number, Number>) ((AnchorPane) ((StackPane) ((Parent) tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(0)).getChildren().get(0);
            chartSetting = (JFXButton) ((AnchorPane) ((StackPane) ((Parent) tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(0)).getChildren().get(1);
            xAxis = (StableTicksAxis) lineChart.getXAxis();
            yAxis = (StableTicksAxis) lineChart.getYAxis();

            onScreenPaneHover = ((AnchorPane) ((StackPane) ((Parent) tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1));
            onScreenPaneSlide = (AnchorPane) ((AnchorPane) ((StackPane) ((Parent) tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1)).getChildren().get(0);
            timelineSlider = (Slider) ((VBox) ((Parent) onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(0);
            cinematicModeToggle = (ToggleButton) ((HBox) ((VBox) ((Parent) onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(1)).getChildren().get(0);
            chipModeToggle = (ToggleButton) ((HBox) ((VBox) ((Parent) onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(1)).getChildren().get(1);
            readButton = (JFXButton) ((HBox) ((VBox) ((Parent) onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(1)).getChildren().get(2);
            resetButton = (JFXButton) ((HBox) ((VBox) ((Parent) onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(1)).getChildren().get(3);
            saveButton = (JFXButton) ((HBox) ((VBox) ((Parent) onScreenPaneSlide).getChildrenUnmodifiable().get(0)).getChildren().get(1)).getChildren().get(4);
            onScreenArrow = (Label)(Parent)onScreenPaneSlide.getParent().getChildrenUnmodifiable().get(1);
            prepareSlideButtonAnimation();
            /*
            cinematicModeToggle = (ToggleButton) ((HBox)((VBox)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1)).getChildren().get(0);
            chipModeToggle = (ToggleButton) ((HBox)((VBox)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1)).getChildren().get(1);
            readButton = (Button)((HBox)((VBox)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1)).getChildren().get(2);
            resetButton = (Button)((HBox)((VBox)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1)).getChildren().get(3);
            saveButton = (Button)((HBox)((VBox)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(1)).getChildren().get(4);
            timelineSlider=(Slider)((VBox)((Parent)tab.getContent()).getChildrenUnmodifiable().get(1)).getChildren().get(2);
            */
        } else {
            BorderPane bp = new BorderPane();
            table = new TableView();
            xAxis = new StableTicksAxis();
            yAxis = new StableTicksAxis();
            lineChart = new LineChart<Number, Number>(xAxis, yAxis);

            chartSetting = new JFXButton();


            cinematicModeToggle = new ToggleButton("Cinematic Mode");
            cinematicModeToggle.applyCss();

            chipModeToggle = new ToggleButton("Chip");
            chipModeToggle.applyCss();

            readButton = new JFXButton("Read");
            readButton.applyCss();

            resetButton = new JFXButton("Reset");
            resetButton.applyCss();

            saveButton = new JFXButton("Save");
            saveButton.applyCss();

            timelineSlider = new JFXSlider();
            timelineSlider.applyCss();

            HBox buttonBox = new HBox(cinematicModeToggle, chipModeToggle, readButton, resetButton, saveButton);

            HBox lineChartBox = new HBox(lineChart, chartSetting);

            VBox centerBox = new VBox(lineChartBox, buttonBox, timelineSlider);

            bp.setLeft(table);
            bp.setCenter(centerBox);

            tab.setContent(bp);

        }
        setupButtons();
        setupTable();
        setupSlider();
        setupStatusListener();
        if (statusList.get(0) == 0) {
            chipModeToggle.setDisable(true);
        }
        saveButton.setDisable(true);

        //Setup of axis - making sure they are squared
        xAxis.setLowerBound(0);
        xAxis.setUpperBound(5);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(5);
        System.out.println("Setup complete. Launching program.");
    }

    public void findMax() {
        int index = data.findMaxIndex();
        if (!findMaxFlag && statusChart) {
            System.out.println("Trying to find max");
            System.out.println("Max: " + index);
            Circle circ = new Circle(4, Color.TRANSPARENT);
            circ.setStroke(Color.FORESTGREEN);
            seriesX.getData().get(index).setNode(circ);
            lineChart.getData().setAll(seriesX);
            table.scrollTo(index);
            table.getSelectionModel().select(index);
            findMaxFlag = true;
        } else if(findMaxFlag && statusChart) {
            seriesX.getData().get(index).getNode().setVisible(false);
            findMaxFlag = false;
            System.out.println("Max removed");
        }

    }

    public void findMin() {
        int index = data.findMinIndex();
        if (!findMinFlag && statusChart) {
            System.out.println("Trying to find Min");
            System.out.println("Min: " + index);
            Circle circ = new Circle(4, Color.TRANSPARENT);
            circ.setStroke(Color.FIREBRICK);
            seriesX.getData().get(index).setNode(circ);
            lineChart.getData().setAll(seriesX);
            table.scrollTo(index);
            table.getSelectionModel().select(index);
            findMinFlag = true;
        } else if(findMinFlag && statusChart){
            seriesX.getData().get(index).getNode().setVisible(false);
            findMinFlag = false;
        }
    }

    public void prepareSlideButtonAnimation() {
        //Arrow- on screen slide
        Node iconArrowUp = GlyphsDude.createIcon(MaterialDesignIcon.DOTS_VERTICAL,"25px");
        iconArrowUp.getStyleClass().add("material-icon-arrow");
        onScreenArrow.setGraphic(iconArrowUp);
        onScreenArrow.getStyleClass().setAll("material-icon-container");

        FadeTransition changeArrowDown = new FadeTransition(new Duration(350), onScreenArrow);




        TranslateTransition openSlide = new TranslateTransition(new Duration(350), onScreenPaneSlide);
        TranslateTransition openHover = new TranslateTransition(new Duration(350), onScreenPaneHover);
        openSlide.setToY(onScreenPaneSlide.getHeight() + 2);
        openHover.setToY(onScreenPaneHover.getHeight() + 2);
        TranslateTransition closeSlide = new TranslateTransition(new Duration(350), onScreenPaneSlide);
        TranslateTransition closeHover = new TranslateTransition(new Duration(350), onScreenPaneHover);

        onScreenPaneHover.setOnMouseEntered(e -> {
            changeArrowDown.setToValue(0);
            openHover.play();
            openSlide.play();
            changeArrowDown.play();
        });

        onScreenPaneHover.setOnMouseExited(e -> {
            closeSlide.setToY(onScreenPaneHover.getHeight());
            closeHover.setToY(onScreenPaneSlide.getHeight() - 38);
            changeArrowDown.setToValue(1);
            changeArrowDown.play();
            closeHover.play();
            closeSlide.play();
        });
    }


    public void setupButtons() {
        //**********************************************BUTTON SETUP*******************************************//
        //******Toggle: Cinematic mode***********//
        cinematicModeToggle.setOnAction(e -> {
            if (cinematicMode) {
                cinematicMode = false;
                readButton.getStyleClass().set(1, "read-button");
            } else {
                cinematicMode = true;
                readButton.getStyleClass().set(1, "play-button");
            }
        });
        //*****************************************//
        //******Toggle: Chip or Computer***********//
        chipModeToggle.setOnAction(e -> {
            if (readFromChip) {
                readFromChip = false;
            } else {
                readFromChip = true;
            }
        });
        //*****************************************//
        //**SETUP OF SETTING-box**//
        settingX = new JFXCheckBox("X");
        //settingX.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/chartLineSmallBlack.png")));
        settingX.setSelected(true);
        //settingX.setDisable(true);
        settingY = new JFXCheckBox("Y");
        //settingY.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/chartLineSmallBlack.png")));
        //settingY.setDisable(true);
        settingZ = new JFXCheckBox("Z");
        //settingZ.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/chartLineSmallBlack.png")));
        //settingZ.setDisable(true);

        settingGPSData = new JFXCheckBox("Position");
        //settingGPSData.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/gpsOff.png")));
        settingGPSData.setGraphic(new ImageView(new Image("file:classes/images/iconsBlack/gpsOff.png")));

        settingClickData = new JFXCheckBox("Clickable data");
        //settingClickData.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/chartClickSmallBlack.png")));
        settingClickData.setGraphic(new ImageView(new Image("file:classes/images/iconsBlack/chartClickSmallBlack.png")));
        settingClickData.setDisable(true);

        settingForceZeroInRange = new JFXCheckBox("Force zero in range");


        settingX.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!settingX.isSelected() && !statusChart) {
                    settingXIsActive = true;
                } else if (settingX.isSelected() && !statusChart) {
                    settingXIsActive=false;
                } else if (!settingXIsActive && !settingX.isSelected() && statusChart && !settingYIsActive && !settingZIsActive) {
                    settingXIsActive = true;
                } else if (settingX.isSelected() && !settingXIsActive && statusChart && seriesX.getData().size()==0) {
                    data.setupX();
                    seriesX = data.getDataSeriesY();
                    seriesX.setName("x");
                    lineChart.getData().add(seriesX);
                    settingXIsActive = true;
                } else if (!settingXIsActive && settingX.isSelected() && statusChart) {
                    lineChart.getData().add(seriesX);
                    settingXIsActive = true;
                } else if (settingXIsActive && statusChart) {
                    lineChart.getData().removeAll(seriesX);
                    settingXIsActive = false;
                } else if (settingXIsActive && statusChart) {
                    lineChart.getData().removeAll(seriesX);
                    settingXIsActive = false;
                }
            }
        });


        settingY.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!settingYIsActive && !statusChart) {
                    settingYIsActive = true;
                } else if (settingYIsActive && !statusChart) {
                    settingYIsActive = false;
                } else if (settingY.isSelected() && !settingYIsActive && statusChart && seriesY.getData().size()==0) {   //Adding a check to keep track if the series have been loaded from data before
                    System.out.println("3");
                    data.setupY();
                    seriesY = data.getDataSeriesY();
                    seriesY.setName("y");
                    lineChart.getData().add(seriesY);
                    settingYIsActive = true;
                } else if (!settingYIsActive && settingY.isSelected() && statusChart) {
                    lineChart.getData().add(seriesY);
                    settingYIsActive = true;
                } else if (settingYIsActive && statusChart) {
                    lineChart.getData().removeAll(seriesY);
                    settingYIsActive = false;
                } else if (settingYIsActive && statusChart) {
                    lineChart.getData().removeAll(seriesY);
                    settingYIsActive = false;
                }
            }
        });

        settingZ.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!settingZIsActive && !statusChart) {
                    settingZIsActive = true;
                } else if (settingZIsActive && !statusChart) {
                    settingZIsActive = false;
                } else if (settingZ.isSelected() && !settingZIsActive && statusChart && seriesZ.getData().size()==0) {
                    data.setupZ();
                    seriesZ = data.getDataSeriesZ();
                    seriesZ.setName("z");
                    lineChart.getData().add(seriesZ);
                    settingZIsActive = true;
                } else if (!settingZIsActive && settingZ.isSelected() && statusChart) {
                    lineChart.getData().add(seriesZ);
                    settingZIsActive = true;
                } else if (settingZIsActive && statusChart) {
                    lineChart.getData().removeAll(seriesZ);
                    settingZIsActive = false;
                } else if (settingZIsActive && statusChart) {
                    lineChart.getData().removeAll(seriesZ);
                    settingZIsActive = false;
                }
            }
        });

        settingGPSData.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (!data.getDataMode()) {
                    data.setDataMode("GPS");
                    settingGPSData.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/gpsOn.png")));
                    xCol.setText("Long.");
                    yCol.setText("Lat.");
                } else {
                    /*
                    data.setDataMode("Acceleration");
                    chartSetting.getItems().add(0,settingX);
                    chartSetting.getItems().add(1,settingY);
                    chartSetting.getItems().add(2,settingZ);
                    settingAcc.setDisable(true);
                    */
                    settingGPSData.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/gpsOff.png")));
                    xCol.setText("Time <s>");
                    yCol.setText("Lat. <m/s^2>");

                }
            }
        });

        settingForceZeroInRange.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (settingForceZeroInRange.getText() == "Force zero in range") {
                    xAxis.setForceZeroInRange(false);
                }
            }
        });

        VBox menus = new VBox();
        menus.setAlignment(Pos.CENTER);
        menus.setSpacing(5);

        MaterialAccordion accMenu = new MaterialAccordion();
        MaterialAccordionTitledPane acc = new MaterialAccordionTitledPane();
        VBox settingsBoxAcc = new VBox();
        acc.setText("Acceleration");
        acc.setContent(settingsBoxAcc);
        accMenu.getPanes().add(acc);

        //acc.getItems().setAll(settingX,settingY,settingZ);

        MaterialAccordion gpsMenu = new MaterialAccordion();
        MaterialAccordionTitledPane gps = new MaterialAccordionTitledPane();
        VBox settingsBoxGPS = new VBox();
        gps.setText("GPS");
        gps.setContent(settingsBoxGPS);
        gpsMenu.getPanes().add(gps);

        MaterialAccordion rotationMenu = new MaterialAccordion();
        MaterialAccordionTitledPane rotation = new MaterialAccordionTitledPane();
        VBox settingsBoxRotation = new VBox();
        rotation.setText("Rotation");
        rotation.setContent(settingsBoxRotation);
        rotationMenu.getPanes().add(rotation);

        MaterialAccordion magneticMenu = new MaterialAccordion();
        MaterialAccordionTitledPane magnetic = new MaterialAccordionTitledPane();
        VBox settingsBoxMagnetic = new VBox();
        magnetic.setText("Magnetic");
        magnetic.setContent(settingsBoxMagnetic);
        magneticMenu.getPanes().add(magnetic);

        MaterialAccordion utilitiesMenu = new MaterialAccordion();
        MaterialAccordionTitledPane utilities = new MaterialAccordionTitledPane();
        utilities.setText("Utilities");
        utilities.setContent(settingClickData);
        utilitiesMenu.getPanes().add(utilities);


        menus.getChildren().setAll(accMenu, gpsMenu, rotationMenu, magneticMenu, utilitiesMenu);

        /*
        acc = new Menu();
        gps = new Menu();
        rotation = new Menu();
        magnetic = new Menu();
        */

        acc.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                System.out.println("Acc");
                if (settingX.getParent() != settingsBoxAcc) {
                    if(settingX.getParent()!= null) {
                        ((VBox) settingX.getParent()).getChildren().removeAll(settingX, settingY, settingZ);
                        System.out.println("Removed settings from previous contextmenu");
                    }
                    settingsBoxAcc.getChildren().addAll(settingX, settingY, settingZ);
                    data.setDataMode("Acceleration");
                    settingX.setText("X");
                    settingY.setText("Y");
                    settingZ.setText("Z");
                    xCol.setText("Time <s>");
                    yCol.setText("Acc. <m/s^2>");
                }



                /*
                if(settingX.getParentMenu()!= null) {
                    settingX.getParentMenu().getItems().remove(0, 3); //Removing the menuitem from the other menus, if it has already been used
                    System.out.println("Removed settings from previous contextmenu");
                }
                */

            }
        });

        gps.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                System.out.println("GPS");
                if (settingX.getParent() != settingsBoxGPS) {
                    if(settingX.getParent()!= null) {
                        ((VBox) settingX.getParent()).getChildren().removeAll(settingX, settingY, settingZ);
                        System.out.println("Removed settings from previous contextmenu");
                    }
                    settingsBoxGPS.getChildren().addAll(settingX, settingY, settingZ);
                    data.setDataMode("GPS");
                    settingX.setText("Longtitude");
                    settingY.setText("Latitude");
                    settingZ.setText("Altitude");

                    xCol.setText("Time <s>");
                    yCol.setText("Long. <m>");

                }
                /*
                if(settingsBox.getParent() != null) {
                    settingsBox.getParent().getChildrenUnmodifiable().remove(settingsBox); //Removing the menuitem from the other menus, if it has already been used
                    System.out.println("Removed settings from previous contextmenu");
                }
                */

            }
        });


        magnetic.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                System.out.println("Magnetic");
                if (settingX.getParent() != settingsBoxMagnetic) {
                    if(settingX.getParent()!= null) {
                        ((VBox) settingX.getParent()).getChildren().removeAll(settingX, settingY, settingZ);
                        System.out.println("Removed settings from previous contextmenu");
                    }
                    settingsBoxMagnetic.getChildren().addAll(settingX, settingY, settingZ);
                    data.setDataMode("Magnetic");
                    settingX.setText("X");
                    settingY.setText("Y");
                    settingZ.setText("Z");

                    xCol.setText("Time <s>");
                    yCol.setText("Magn.");
                }
                /*
                if(settingsBox.getParent() != null) {
                    settingsBox.getParent().getChildrenUnmodifiable().remove(settingsBox); //Removing the menuitem from the other menus, if it has already been used
                    System.out.println("Removed settings from previous contextmenu");
                }
                */

            }
        });

        rotation.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                System.out.println("Rotation");
                if (settingX.getParent() != settingsBoxRotation) {
                    if(settingX.getParent()!= null) {
                        ((VBox) settingX.getParent()).getChildren().removeAll(settingX, settingY, settingZ);
                        System.out.println("Removed settings from previous contextmenu");
                    }
                    settingsBoxRotation.getChildren().addAll(settingX, settingY, settingZ);
                    data.setDataMode("Rotation");
                    settingX.setText("X");
                    settingY.setText("Y");
                    settingZ.setText("Z");

                    xCol.setText("Time <s>");
                    yCol.setText("Rad. <>");
                }
                /*
                if(settingsBox.getParent() != null) {
                    settingsBox.getParent().getChildrenUnmodifiable().remove(settingsBox); //Removing the menuitem from the other menus, if it has already been used
                    System.out.println("Removed settings from previous contextmenu");
                }
                */

            }
        });

        acc.setDisable(false);
        gps.setDisable(true);
        magnetic.setDisable(true);
        rotation.setDisable(true);

        /*



        magnetic.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Magnetic");
                if(settingX.getParentMenu() != null) {
                    settingX.getParentMenu().getItems().remove(0, 3); //Removing the menuitem from the other menus, if it has already been used
                    System.out.println("Removed settings from previous contextmenu");
                }
                data.setDataMode("Magnetic");
                magnetic.getItems().setAll(settingX,settingY,settingZ);
                settingX.setText("X");
                settingY.setText("Y");
                settingZ.setText("Z");
                magnetic.show();
            }
        });
        */
        settingClickData.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if (clickStatusChart) {
                    graph.setDataClickable(clickStatusChart);
                    clickStatusChart = false;
                } else {
                    graph.setDataClickable(clickStatusChart);
                    clickStatusChart = true;
                }
            }
        });

        //Creating a listview and adding each of the menuItems to it. This will in turn be added to the popup.
        //JFXPopup
        JFXPopup menuPopup = new JFXPopup(menus);
        menuPopup.setStyle("-fx-padding: 2px;");


        chartSetting.setOnMouseClicked(new EventHandler<javafx.scene.input.MouseEvent>() {
            @Override
            public void handle(javafx.scene.input.MouseEvent event) {
                if (!menuPopup.isShowing()) {
                    menuPopup.show(chartSetting, JFXPopup.PopupVPosition.BOTTOM, JFXPopup.PopupHPosition.RIGHT);
                    //menuPopup.show(chartSetting, JFXPopup.PopupVPosition.BOTTOM, JFXPopup.PopupHPosition.RIGHT, event.getX(), event.getY());
                }
            }
        });
        chartSetting.setButtonType(JFXButton.ButtonType.RAISED);

        //******************************BUTTONS - STATIC VIEW********************************//
        readButton.setOnAction(e -> {
            if (cinematicMode) {
                System.out.println("Trying to read from cinematically");
                if (readFromChip) {
                    if (statusChart && !timelineIsStopped) {
                        timeline.stop();
                        timelineIsStopped = true;
                    }else if(timelineIsFinished) {
                        System.out.println("Timeline is already finished!");
                    }
                    else if (statusChart && timelineIsStopped) {
                        timeline.play();
                        timelineIsStopped = false;
                        //System.out.println("Graph reset");
                        //resetGraph();
                    } else {
                        readChipCinematic();
                    }
                    //statusChart=true;
                /*
                initAnimatedGraph();
                this.startAnimatedTimeline("chip");
                */
                } else {
                    if (statusChart && !timelineIsFinished && !timelineIsStopped) {
                        System.out.println("Stopping timeline");
                        stopAnimatedTimeline();
                        timelineIsStopped = true;
                    } else if (statusChart && timelineIsFinished && timelineIteration + 1 == dataListX.size()) {
                        System.out.println("Resetting chart");
                        resetGraph();
                        readFileCinematic();
                    } else if (!timelineIsFinished && statusChart || (timelineIteration + 1 != dataListX.size() && timelineIsFinished)) {
                        //timeline.play();
                        System.out.println("Starting timeline");
                        timelineIsStopped = false;
                        startAnimatedTimeline("file");
                    } else {
                        readFileCinematic();
                    }
                }
                //statusChart = true;

                settingX.setDisable(false);
                settingY.setDisable(false);
                settingZ.setDisable(false);
                settingClickData.setDisable(false);
                saveButton.setDisable(false);

                System.out.println("readStatic");
            } else {
                //Logic for determining whether to read from chip or from disk
                if (readFromChip) {
                    if (statusChart) {
                        System.out.println("Graph Reset");
                        resetGraph();
                    }
                    Task<Void> readTask = new Task<Void>() {
                        @Override
                        protected Void call() throws Exception {
                            //Fetching data for table
                            System.out.println("Reading data");

                            if (settingXIsActive) {
                                System.out.println("reading x...");
                                data.readFile(null, true, progressList);
                            }
                            if (settingYIsActive) {
                                data.readFile(null, true, progressList);
                            }
                            if (settingZIsActive) {
                                data.readFile(null, true, progressList);
                            }
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    initStaticGraph();
                                }
                            });
                            return null;
                        }
                    };
                    Thread th = new Thread(readTask);
                    System.out.println("Starting new readThread");
                    progressBar.setVisible(true);
                    th.start();

                    statusChart = true;

                    settingX.setDisable(false);
                    settingY.setDisable(false);
                    settingZ.setDisable(false);
                    settingClickData.setDisable(false);
                    saveButton.setDisable(false);

                    System.out.println("readStatic");
                } else if (!readFromChip) {
                    if (statusChart) {
                        System.out.println("Graph reset");
                        resetGraph();
                    }
                    FileChooser fileChoose = new FileChooser();
                    FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("txt File(*.txt)", "*.txt");
                    fileChoose.getExtensionFilters().add(extensionFilter);

                    File file = fileChoose.showOpenDialog(tabPane.getScene().getWindow());

                    if (file != null) {
                        System.out.println("File found");
                        //Open up a new task to read the file.
                        readTask = new Task<Void>() {
                            @Override
                            protected Void call() {
                                System.out.println("Reading data");
                                if (settingXIsActive) {
                                    System.out.println("Reading x");
                                    //data.setData(1,10);
                                    data.readFile(file, false, progressList);
                                }
                                if (settingYIsActive) {
                                    System.out.println("Reading y");
                                    //data.setData(1,11);
                                    data.readFile(file, false, progressList);
                                }
                                if (settingZIsActive) {
                                    System.out.println("Reading z");
                                    //data.setData(1,12);
                                    data.readFile(file, false, progressList);
                                }
                                System.out.println("Read successful");
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        initStaticGraph();
                                        progressBar.setVisible(false);
                                    }
                                });
                                return null;
                            }
                        };
                        Thread th = new Thread(readTask);
                        //progressBarStaticView.setProgress(0);
                        System.out.println("Starting new readThread");
                        progressBar.setVisible(true);
                        th.start();

                        statusChart = true;

                        settingX.setDisable(false);
                        settingY.setDisable(false);
                        settingZ.setDisable(false);
                        settingClickData.setDisable(false);
                        saveButton.setDisable(false);

                        System.out.println("readStatic");
                    } else {
                        System.out.println("File not found! Please try again. This is here to prevent an exception");
                    }
                    /*
                        try{
                            while(true) {
                                System.out.println(readTask.getProgress());
                                Thread.sleep(10);
                            }
                        }catch(Exception a){
                            ;
                        }
                        */
                        /* //Not necessary at the moment TODO
                        String fileName = file.getName();
                        String fileExtension = fileName.substring(fileName.indexOf(".") + 1, file.getName().length());
                        System.out.println(fileExtension);
                        while (fileExtension != "txt") {
                            Alert noSuchFileAlert = new Alert(Alert.AlertType.INFORMATION);
                            noSuchFileAlert.setTitle("Error in loading file");
                            noSuchFileAlert.setHeaderText(null);
                            noSuchFileAlert.setContentText("Unallowed file. Please try again.");
                            noSuchFileAlert.showAndWait();
                            file = fileChoose.showOpenDialog(tabPane.getScene().getWindow());
                            fileChoose.showOpenDialog(tabPane.getScene().getWindow());
                            fileName = file.getName();
                            fileExtension = fileName.substring(fileName.indexOf(".") + 1, file.getName().length());
                            System.out.println(fileExtension);
                        }
                        */

                } else {
                    Alert processExitAlert = new Alert(Alert.AlertType.INFORMATION);
                    processExitAlert.setTitle("Read Exit");
                    processExitAlert.setHeaderText(null);
                    processExitAlert.setContentText("Reading of file will now terminate");
                    processExitAlert.showAndWait();
                    //Styling in css
                    DialogPane dialogPane = processExitAlert.getDialogPane();
                    dialogPane.getStylesheets().add(getClass().getResource("stylesheet.css").toExternalForm());
                    dialogPane.getStyleClass().add("stylesheet");
                }
            }


        });


        resetButton.setOnAction(e -> {
            System.out.println("resetStatic");
            resetGraph();
        });

        saveButton.setOnAction(e -> {
            FileChooser fileChoose = new FileChooser();
            FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("txt File(*.txt)", "*.txt");
            fileChoose.getExtensionFilters().add(extensionFilter);

            File file = fileChoose.showOpenDialog(tabPane.getScene().getWindow());
            if (file != null) {
                saveTask = new Task<Void>() {
                    protected Void call() {
                        data.writeFile(file, progressList, readFromChip);

                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisible(false);
                            }
                        });
                        return null;
                    }
                };
                Thread thread = new Thread(saveTask);
                thread.start();
                progressBar.setVisible(true);
            } else {
                System.out.println("Error in saveButton: valid file must be supplied");
            }

        });
    }

    public void setupStatusListener() {
        statusList.addListener(new ListChangeListener<Integer>() {
            @Override
            public void onChanged(Change<? extends Integer> c) {
                if (statusList.get(0).intValue() == 0) {
                    chipModeToggle.setDisable(true);
                } else if (statusList.get(0).intValue() == 1) {
                    chipModeToggle.setDisable(false);
                }
            }
        });
    }

    //**********************************************STATIC GRAPH*****************************************************//
    public void initStaticGraph() {


        dataListX = data.getDataX();
        table.setItems(dataListX);
        table.getColumns().setAll(xCol, yCol);
        //Setup of series

        if (settingX.isSelected()) {
            seriesX = data.getDataSeriesX();
            graph = new Graph2D(lineChart, seriesX, xAxis, yAxis, "readStatic", progressList);
            graph.setup("x");
            if (!settingY.isSelected() && !settingZ.isSelected()) {
                System.out.println("Only x will be printed");
            } else if (settingY.isSelected() && !settingZ.isSelected()) {
                data.setupY();
                seriesY = data.getDataSeriesY();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;
            } else if (!settingY.isSelected() && settingZ.isSelected()) {
                data.setupZ();
                seriesZ = data.getDataSeriesZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            } else if (settingY.isSelected() && settingZ.isSelected()) {
                data.setupY();
                seriesY = data.getDataSeriesY();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;

                data.setupZ();
                seriesZ = data.getDataSeriesZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            }
        } else if (settingY.isSelected()) {
            seriesY = data.getDataSeriesY();
            graph = new Graph2D(lineChart, seriesY, xAxis, yAxis, "readStatic", progressList);
            graph.setup("y");
            if (!settingX.isSelected() && !settingZ.isSelected()) {
                System.out.println("Only y will be printed");
            } else if (settingX.isSelected() && !settingZ.isSelected()) {
                data.setupX();
                seriesX = data.getDataSeriesX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;
            } else if (!settingX.isSelected() && settingZ.isSelected()) {
                data.setupZ();
                seriesZ = data.getDataSeriesZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            } else if (settingX.isSelected() && settingZ.isSelected()) {
                data.setupX();
                seriesX = data.getDataSeriesX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;

                data.setupZ();
                seriesZ = data.getDataSeriesZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            }
        } else if (settingZ.isSelected()) {
            seriesY = data.getDataSeriesY();
            graph = new Graph2D(lineChart, seriesY, xAxis, yAxis, "readStatic", progressList);
            graph.setup("z");
            if (!settingX.isSelected() && !settingY.isSelected()) {
                System.out.println("Only y will be printed");
            } else if (settingX.isSelected() && !settingY.isSelected()) {
                data.setupX();
                seriesX = data.getDataSeriesX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;
            } else if (!settingX.isSelected() && settingY.isSelected()) {
                data.setupY();
                seriesY = data.getDataSeriesY();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;
            } else if (settingX.isSelected() && settingY.isSelected()) {
                data.setupX();
                seriesX = data.getDataSeriesX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;

                data.setupY();
                seriesY = data.getDataSeriesY();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;
            }
        } else {

            seriesX = data.getDataSeriesX();
            graph = new Graph2D(lineChart, seriesX, xAxis, yAxis, "readStatic", progressList);
            graph.setup("x");

        }
    }

    public void resetGraph() {
        if (timeline != null && statusChart && !timelineIsFinished && !timelineIsStopped) {
            System.out.println("Stopping timeline");
            timeline.stop();
        }

        if (settingClickData.selectedProperty().get()) { //resetting ClickableData
            graph.setDataClickable(clickStatusChart);
            clickStatusChart = false;
            settingClickData.setSelected(false);
        }

        if (settingX.selectedProperty().getValue()) {
            lineChart.getData().removeAll(seriesX);
            seriesX = new XYChart.Series<>();
            //settingXIsActive=false;
            //settingX.setText("Show x");
        } else {
            settingXIsActive = true; //Due to it being default
            settingX.setSelected(true);
        }
        if (settingY.selectedProperty().getValue()) {
            lineChart.getData().removeAll(seriesY);
            seriesY = new XYChart.Series<>();
            settingY.setSelected(false);
            settingYIsActive = false;
        }
        if (settingZ.selectedProperty().getValue()) {
            lineChart.getData().removeAll(seriesZ);
            seriesZ = new XYChart.Series<>();
            settingZ.setSelected(false);
            settingZIsActive = false;
        }


        settingClickData.setDisable(true);
        settingGPSData.setSelected(false);
        settingGPSData.setGraphic(new ImageView(new Image("file:resources/images/iconsBlack/gpsOff.png")));

        findMaxFlag=false;
        findMinFlag=false;


        settingX.setDisable(false);
        settingY.setDisable(false);
        settingZ.setDisable(false);

        timelineIsFinished = false;
        timelineIsStopped = false;
        timelineIteration = 0;
        timelineSlider.setDisable(true);
        progressBar.setVisible(false);
        saveButton.setDisable(true);

        data.resetData();
        statusChart = false;
    }

    //***************************************************************************************************************//

    public void setupTable() {
        //*************SETUP OF STATIC TABLEVIEW***************//
        //Setup of static TableView.
        table.setEditable(true);
        //X-column
        xCol = new TableColumn("Time, <s>");
        TableCol xColStatic = new TableCol(xCol, "x");
        //Y-column
        yCol = new TableColumn("Acc, <m/s^2>");
        TableCol yColStatic = new TableCol(yCol, "y");
        table.getColumns().setAll(xCol, yCol);
        table.setPlaceholder(new Label(""));
        //****************************************************//
    }

    //********************************************ANIMATED GRAPH*****************************************************//

    public void initAnimatedGraph() {
        //Fetching data for table
        dataListX = data.getDataX();
        table.setItems(dataListX);

        //Setup of the graphs
        if (settingX.isSelected()) {
            graph = new Graph2D(lineChart, seriesX, xAxis, yAxis, "readStatic", progressList);
            graph.setup("x");
            if (!settingY.isSelected() && !settingZ.isSelected()) {
                System.out.println("Only x will be printed");
            } else if (settingY.isSelected() && !settingZ.isSelected()) {
                data.setupY();
                dataListY = data.getDataY();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;
            } else if (!settingY.isSelected() && settingZ.isSelected()) {
                data.setupZ();
                dataListZ = data.getDataZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            } else if (settingY.isSelected() && settingZ.isSelected()) {
                data.setupY();
                dataListY = data.getDataY();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;

                data.setupZ();
                dataListZ = data.getDataZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            }
        } else if (settingY.isSelected()) {
            graph = new Graph2D(lineChart, seriesY, xAxis, yAxis, "readStatic", progressList);
            graph.setup("y");
            if (!settingX.isSelected() && !settingZ.isSelected()) {
                System.out.println("Only y will be printed");
            } else if (settingX.isSelected() && !settingZ.isSelected()) {
                data.setupX();
                dataListX = data.getDataX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;
            } else if (!settingX.isSelected() && settingZ.isSelected()) {
                data.setupZ();
                dataListZ = data.getDataZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            } else if (settingX.isSelected() && settingZ.isSelected()) {
                data.setupX();
                dataListX = data.getDataX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;

                data.setupZ();
                dataListZ = data.getDataZ();
                seriesZ.setName("z");
                lineChart.getData().add(seriesZ);
                settingZIsActive = true;
            }
        } else if (settingY.isSelected()) {
            graph = new Graph2D(lineChart, seriesY, xAxis, yAxis, "readStatic", progressList);
            graph.setup("z");
            if (!settingX.isSelected() && !settingY.isSelected()) {
                System.out.println("Only y will be printed");
            } else if (settingX.isSelected() && !settingY.isSelected()) {
                data.setupX();
                dataListX = data.getDataX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;
            } else if (!settingX.isSelected() && settingY.isSelected()) {
                data.setupY();
                dataListZ = data.getDataZ();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;
            } else if (settingX.isSelected() && settingY.isSelected()) {
                data.setupX();
                dataListX = data.getDataX();
                seriesX.setName("x");
                lineChart.getData().add(seriesX);
                settingXIsActive = true;

                data.setupY();
                dataListZ = data.getDataZ();
                seriesY.setName("y");
                lineChart.getData().add(seriesY);
                settingYIsActive = true;
            }
        } else {
            seriesX = data.getDataSeriesX();
            graph = new Graph2D(lineChart, seriesX, xAxis, yAxis, "readAnimated", progressList);
            graph.setup("x");

        }
    }
    //***************************************************************************************************************//

    //******************************************TIMELINE HANDLING****************************************************//

    public void timelineAnimated(String str) {
        //****************** EVENT HANDLER FOR KEYFRAME ***************************//
        EventHandler onFinishedFile = new EventHandler<ActionEvent>() {
            public void handle(ActionEvent t) {
                if (settingXIsActive) {
                    XYChart.Data<Number, Number> datapoint = new XYChart.Data<>(dataListX.get(timelineIteration).getX(), dataListX.get(timelineIteration).getY());
                    seriesX.getData().add(datapoint);
                }
                if (settingYIsActive) {
                    XYChart.Data<Number, Number> datapoint = new XYChart.Data<>(dataListY.get(timelineIteration).getX(), dataListY.get(timelineIteration).getY());
                    seriesY.getData().add(datapoint);
                }
                if (settingZIsActive) {
                    XYChart.Data<Number, Number> datapoint = new XYChart.Data<>(dataListZ.get(timelineIteration).getX(), dataListZ.get(timelineIteration).getY());
                    seriesZ.getData().add(datapoint);
                }

                //double progress = Math.round(((double) timelineIteration / dataListX.size()) * 100);
                double progress = Math.round(((double) timelineIteration / data.getFileData().length) * 100);
                System.out.println("Progress: " + progress);
                System.out.println("timeline Iteration: " + timelineIteration);
                timelineSlider.setValue(progress);
                timelineIteration++;
            }
        };

        EventHandler onFinishedChip = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                data.readContinously();
                XYChart.Series<Number, Number> series = data.getDataSeriesX();

                for (int i = 0; i < series.getData().size(); i++) {
                    //seriesX.getData().setAll(series.getData().get(i)); //TODO, CLEAR UP
                    if (series.getData().get(series.getData().size() - 1) != null) { //If reaches the end without the file having stopped.
                        timeline.stop();
                    }
                    //seriesX = series;
                    if (settingXIsActive) {
                        seriesX = data.getDataSeriesX();
                    }

                    if (settingYIsActive) {
                        seriesY = data.getDataSeriesY();
                    }
                    if (settingZIsActive) {
                        seriesZ = data.getDataSeriesZ();
                    }
                }

                /*
                try {

                    //seriesX.getData().addAll(datapoint);
                }catch(Exception e){
                    System.out.println("Error");
                }
                */

            }
        };
        //*************************************************************************//


        //Creating a timeline for updating the graph
        timeline = new Timeline();
        if (str == "file") {
            System.out.println("entered file");
            timeline.setCycleCount(data.getFileData().length - timelineIteration); //Cycles of the timeline finishing according to the size of the series //
            //timeline.setAutoReverse(true);
            timelineIsFinished = false;
            timeline.setOnFinished(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    timelineIsFinished = true; //TODO BUG! Even though the timeline is finished, it's possible to press play and try to read again => exception
                    readButton.setStyle(".play-button");
                    System.out.println("Done!");
                }

            });
            Duration duration = Duration.millis(frameTime);
            //I don't know how to do actionhandling for keyframe with lambda expression
            keyFrameAnimated = new KeyFrame(duration, onFinishedFile);
            timeline.getKeyFrames().add(keyFrameAnimated);
        } else if (str == "chip") {
            System.out.println("Reading chip");
            timeline.setCycleCount(Timeline.INDEFINITE);
            Duration duration = Duration.millis(frameTime);
            keyFrameAnimated = new KeyFrame(duration, onFinishedChip);
            timeline.getKeyFrames().add(keyFrameAnimated);
        }
    }

    public void startAnimatedTimeline(String str) {
        readButton.getStyleClass().set(1, "pause-button");
        timelineAnimated(str);
        timelineSlider.setDisable(false);
        timeline.play();
    }

    public void stopAnimatedTimeline() {
        readButton.getStyleClass().set(1, "play-button");
        System.out.println("Timeline stopped");
        timeline.stop();
    }

    public void readFileCinematic() {
        FileChooser fileChoose = new FileChooser();
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("txt File(*.txt)", "*.txt");
        fileChoose.getExtensionFilters().add(extensionFilter);

        File file = fileChoose.showOpenDialog(tabPane.getScene().getWindow());

        if (file != null) {
            System.out.println("File found");
            //Open up a new task to read the file. But do so continously.
            readTask = new Task<Void>() {
                @Override
                protected Void call() {
                    System.out.println("Reading data from file...");
                    if (settingXIsActive) {
                        System.out.println("Reading x");
                        data.setData(1, 10);
                        data.readContinouslyFromFile(file, false, progressList);
                    }else{
                        settingX.setDisable(true);
                    }
                    if (settingYIsActive) {
                        System.out.println("Reading y");
                        data.setData(1, 11);
                        data.readContinouslyFromFile(file, false, progressList);
                    }
                    else{
                        settingY.setDisable(true);
                    }
                    if (settingZIsActive) {
                        System.out.println("Reading z");
                        data.setData(1, 12);
                        data.readContinouslyFromFile(file, false, progressList);
                    }
                    else{
                        settingZ.setDisable(true);
                    }
                    System.out.println("Read successful");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            initAnimatedGraph();
                            startAnimatedTimeline("file");
                            //progressLabelStaticView.setText("Done");
                        }
                    });
                    return null;
                }
            };
            statusChart=true;
            Thread th = new Thread(readTask);
            //progressBarStaticView.setProgress(0);
            System.out.println("Starting new readThread: from file");
            //progressBarStaticView.setVisible(true);
            //progressLabelStaticView.setVisible(true);
            th.start();
        } else {
            System.out.println("File not found! Please try again. This is here to prevent an exception");
        }
    }

    public void readChipCinematic() {
        readTask = new Task<Void>() {
            @Override
            protected Void call() {
                System.out.println("Reading data from chip...");
                rp.setBuffer(6);
                rp.continousToDoubleMatrix();
                System.out.println("Read successful");
                /*
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        initAnimatedGraph();
                        startAnimatedTimeline("chip");
                    }
                });
                */
                return null;
            }
        };
        progressBar.setVisible(false);
        Thread th = new Thread(readTask);
        System.out.println("Starting new readThread: from chip");
        th.start();
        initAnimatedGraph();
        startAnimatedTimeline("chip");
    }
    //***************************************************************************************************************//

    public void setupSlider() {
        //**********************SLIDER SETUP*********************//
        timelineSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //progressBarStaticView.setProgress(newValue.doubleValue()/timelineSlider.getMax()); //sets the progressbar's progress
                double progress = newValue.doubleValue() / 100;
                int max = timeline.getCycleCount() - 1;
                int newTimelineIteration;
                if (timelineIsFinished) {
                    newTimelineIteration = (int) Math.round(progress * max);
                    if (newTimelineIteration > timelineIteration) {
                        lineChart.getData().removeAll(seriesX);
                        for (int i = timelineIteration; i < newTimelineIteration; i++) {
                            XYChart.Data<Number, Number> datapoint = new XYChart.Data<Number, Number>(dataListX.get(i).getX(), dataListX.get(i).getY());
                            seriesX.getData().add(datapoint);
                        }
                        lineChart.getData().add(seriesX);
                    } else if (newTimelineIteration < timelineIteration) {
                        lineChart.getData().removeAll(seriesX);
                        seriesX.getData().remove(newTimelineIteration, timelineIteration);
                        lineChart.getData().add(seriesX);
                    }
                    timelineIteration = newTimelineIteration;
                    System.out.println("timelineIteration new: " + timelineIteration);
                }

            }
        });
        //*******************************************************//
    }



    public boolean getChartStatus(){
        return statusChart;
    }
}
