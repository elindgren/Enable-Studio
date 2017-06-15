package sample;

import javafx.event.EventHandler;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.NumberStringConverter;

/**
 * Created by ericl on 2017-06-15.
 */

//Creates a new column object, depending on what button has been pressed
public class TableCol {
    private TableColumn tableColumn;
    public TableCol(TableColumn tableCol, String str){
        this.tableColumn = tableCol;

        tableColumn.setMinWidth(100);
        tableColumn.setCellValueFactory(new PropertyValueFactory<DataPoint2D, Number>(str));
        tableColumn.setCellFactory(TextFieldTableCell.<DataPoint2D,Number>forTableColumn(new NumberStringConverter()));
        tableColumn.setOnEditCommit(
                new EventHandler<TableColumn.CellEditEvent<DataPoint2D, Number>>() {
                    @Override
                    public void handle(TableColumn.CellEditEvent<DataPoint2D, Number> t) {
                        if(str == "x") {
                            ((DataPoint2D) t.getTableView().getItems().get(
                                    t.getTablePosition().getRow())).setX(t.getNewValue());
                        }
                        else if(str == "y"){
                            ((DataPoint2D) t.getTableView().getItems().get(
                                    t.getTablePosition().getRow())).setY(t.getNewValue());
                        }
                    }
                }
        );
    }
}
