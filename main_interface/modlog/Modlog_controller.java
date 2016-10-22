package main_interface.modlog;

/**
 * Created by Leander on 15.10.2016.
 * Controller of the modlog display, handles sizing of the cells and adding new items
 */
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class Modlog_controller {

    @FXML
    private Text title;

    @FXML
    private ListView<Text> list;

    private ObservableList<Text> textlist = FXCollections.observableArrayList();

    @FXML
    public void initialize()
    {
        list.setItems(textlist);
        list.setStyle("-fx-border-color: black;-fx-border-width: 2px");
        list.setCellFactory(params -> new Custom_cell());
    }

    //Sets the textlist to the given textlist
    void set_items(ObservableList<Text> textlist)
    {
        this.textlist = textlist;
        list.setItems(textlist);
        if(textlist.size()!=0)list.scrollTo(textlist.get(textlist.size()-1));
    }

    //Sets the title to the given String
    void set_title(String title)
    {
        this.title.setText(title);
    }

    //Custom cell class, used to bind the height of the cells to the content height
    private class Custom_cell extends ListCell<Text>
    {
        Custom_cell(){}
        @Override
        protected void updateItem(Text t,boolean empty)
        {
            super.updateItem(t,empty);

            if(t!=null&&!empty)
            {
                minHeightProperty().bind(t.yProperty());
                setGraphic(t);
            }
            else
            {
                setGraphic(null);
            }
        }
    }

}
