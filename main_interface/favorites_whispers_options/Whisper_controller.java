package main_interface.favorites_whispers_options;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import main_interface.Main_window_controller;

/**
 * Created by Leander
 * Controller for the whisper class, handles sending whispers and displaying individual whispers
 */

public class Whisper_controller {

    @FXML
    private VBox boundary;

    @FXML
    private HBox whisper_controls;

    @FXML
    private TextField whisper_chatfield;

    @FXML
    private ListView<Node> whisper_view;

    private ObservableList<Node> whisper_list = FXCollections.observableArrayList();

    private Main_window_controller mwc;
    private String username;

    @FXML
    public void initialize()
    {
        whisper_view.setItems(whisper_list);
        whisper_chatfield.addEventFilter(KeyEvent.KEY_PRESSED,enter_validation());
    }

    @FXML
    private void send_whisper()
    {
        String s = whisper_chatfield.getText();
        whisper_chatfield.setText(null);
        if(s==null||s.equals(""))return;
        mwc.send_whispers(username,s);
    }

    //Used to detect enter presses in the new channel field
    private EventHandler<KeyEvent> enter_validation()
    {
        return event ->
        {
            if(event.getCode().equals(KeyCode.ENTER))
            {
                event.consume();
                send_whisper();
            }
        };
    }

    //Removes the whisperbox corresponding to the given username
    private void remove_whisperbox(String username)
    {
        if(mwc==null)return;
        mwc.remove_whisperbox(boundary,username);
    }

    //set the values of the username and passes the reference to the mainwindow controller
    public void initialize_values(String username, Main_window_controller mwc)
    {
        this.username = username;
        this.mwc = mwc;
        Text t = new Text("  "+username);
        t.setStyle("-fx-font-weight: bold;");
        String buttonstyle = "-fx-background-radius: 5em;-fx-min-width: 10px;-fx-min-height: 10px;-fx-max-height: 10px;-fx-max-width: 10px";
        Button close = new Button();
        close.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/Data/icons/close_icon.png"))));
        close.setStyle(buttonstyle);
        close.setOnAction(event -> remove_whisperbox(username));
        whisper_controls.getChildren().addAll(close,t);
    }

    public void add_whisper(Node n)
    {
        Platform.runLater(() -> add_whisper_threadsafe(n));
    }
    //Called by add_whisper to have thread safety
    private void add_whisper_threadsafe(Node n)
    {
        whisper_list.add(n);
        whisper_view.scrollTo(n);
        if(whisper_list.size()==50)
        {
            whisper_list.remove(0);
        }
    }


}
