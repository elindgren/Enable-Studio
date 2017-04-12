import java.util.HashMap;
import java.util.Map;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.MapValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class CordPan extends Application {

    public static final String Column1MapKey = "0";
    public static final String Column2MapKey = "1";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        //Set up the scene with size and title
        Scene scene = new Scene(new Group());
        stage.setTitle("Graphics");
        stage.setWidth(300);
        stage.setHeight(500);

        final Label label = new Label("Coordinates");
        label.setFont(new Font("Arial", 30));

        TableColumn<Map, String> firstDataColumn = new TableColumn<>("X");
        TableColumn<Map, String> secondDataColumn = new TableColumn<>("Y");

        // Column1MapKey refers to which data goes into which column
        firstDataColumn.setCellValueFactory(new MapValueFactory(Column1MapKey));
        firstDataColumn.setMinWidth(130);
        secondDataColumn.setCellValueFactory(new MapValueFactory(Column2MapKey));
        secondDataColumn.setMinWidth(130);

        TableView table_view = new TableView<>(generateDataInMap());

        table_view.setEditable(false);
        table_view.getSelectionModel().setCellSelectionEnabled(true);
        table_view.getColumns().setAll(firstDataColumn, secondDataColumn);
        Callback<TableColumn<Map, String>, TableCell<Map, String>>
                cellFactoryForMap = new Callback<TableColumn<Map, String>,
                TableCell<Map, String>>() {
            @Override
            public TableCell call(TableColumn p) {
                return new TextFieldTableCell(new StringConverter() {
                    @Override
                    public String toString(Object t) {
                        return t.toString();
                    }
                    @Override
                    public Object fromString(String string) {
                        return string;
                    }
                });
            }
        };
        firstDataColumn.setCellFactory(cellFactoryForMap);
        secondDataColumn.setCellFactory(cellFactoryForMap);

        final VBox vbox = new VBox();
        vbox.setSpacing(5);
        vbox.setPadding(new Insets(10, 0, 0, 10));
        vbox.getChildren().addAll(label, table_view);

        ((Group) scene.getRoot()).getChildren().addAll(vbox);

        stage.setScene(scene);
        stage.show();
    }

    private ObservableList<Map> generateDataInMap() {
        int[][] myArray = new int[10][2];
        int cols = myArray[0].length;
        int rows = myArray.length;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++){
                myArray[i][j] = i + j;
                //System.out.println(myArray[i][j]);
            }
        }

        //Each datarow is build up on the values of the matrix in one row. The datarow puts each value in
        //the right column and is then added to the Observable list which is finaly displayed.
        ObservableList<Map> allData = FXCollections.observableArrayList();
        for (int i = 0; i < rows; i++) {
            //creating a HashMap called dataRow which we put the column values of one row (i) into.
            Map<String, String> dataRow = new HashMap<>();

            for (int j=0; j<cols; j++) {

                String value = "" + myArray[i][j];
                System.out.println(i + "||"+  j + "||" + myArray[i][j]);
                // (Associated element, value of element)
                dataRow.put("" + j, value);

            }
            allData.add(dataRow);
        }
        return allData;
    }
}