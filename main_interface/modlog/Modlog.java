package main_interface.modlog;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main_interface.Main_window;

import java.util.logging.Logger;

/**
 * Created by Leander on 15.10.2016.
 * Class used as an adapter for the methods of the modlog_controller, created for each channel
 */
class Modlog {
    private final static Logger LOGGER = Logger.getGlobal();

    private String channel;
    private ObservableList<Text> textlist = FXCollections.observableArrayList();
    private Modlog_controller controller = null;

    Modlog(String channel)
    {
        this.channel = channel;
    }


    //Adds a line to textlist in this channel, also adds to the modlog_interface if it's open right now
    void add_line(String line)
    {
        Text t = new Text(line);
        Platform.runLater(() -> add_line_internal(t));
    }

    //Internal method that implements the line adding logic, called from add_line in a threadsafe fashion
    private void add_line_internal(Text t)
    {
        textlist.add(t);
        while(textlist.size()>50)
        {
            textlist.remove(0);
        }
    }

    //Loads the modlog interface and sets the content
    void show_modlog()
    {
        try {
            Stage modlog_stage = new Stage();
            modlog_stage.getIcons().add(new Image(getClass().getResourceAsStream("/Data/icons/main_icon_cut.png")));
            modlog_stage.setTitle("Modlog");
            Pane modlog_pane = new Pane();
            Scene modlog_scene = new Scene(modlog_pane, 490, 590);
            FXMLLoader fxmlLoader = new FXMLLoader(Main_window.class.getResource("layout_elements/modlog_interface.fxml"));
            VBox modlog_window = fxmlLoader.load();
            controller = fxmlLoader.getController();
            controller.set_items(textlist);
            controller.set_title("Modlogs in "+channel);

            modlog_pane.getChildren().add(modlog_window);
            modlog_stage.setScene(modlog_scene);
            modlog_stage.setResizable(false);
            modlog_stage.show();
        }catch (Exception e) {e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    @Override
    public String toString()
    {
        return channel;
    }
}
