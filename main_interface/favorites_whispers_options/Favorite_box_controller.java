package main_interface.favorites_whispers_options;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import main_interface.Main_window_controller;

/**
 * Created by Leander on 06.09.2016.
 * Controller for individual favorite boxes, adds functionality to the buttons
 */

public class Favorite_box_controller {

    @FXML
    private HBox boundary;

    @FXML
    private Label chatlist;

    @FXML
    private Button load_button;

    @FXML
    private Button remove_button;

    @FXML
    private Label name;

    private Main_window_controller mwc;

    private String chatlist_s;

    @FXML
    public void initialize()
    {
        remove_button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/Data/icons/close_icon.png"))));
        remove_button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        load_button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/Data/icons/blue_arrow_small.png"))));
        load_button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

    }

    //called when clicking the remove_button
    @FXML
    private void remove_from_favorite()
    {
        if(mwc==null)return;
        mwc.remove_favorite(boundary);
    }

    //call to set the name (content of the bigger textfield)
    public void set_name(String name) {
        name = name.replaceAll(",|;","");
        this.name.setText(name);
    }

    //call to set the list of chats
    public void set_chatlist(String chatlist)
    {
        this.chatlist.setText(chatlist);
        chatlist_s = chatlist;
    }

    //Calls the loading method of the favorite list in Main_window_controller
    @FXML
    private void load_favorite()
    {
        if(mwc==null)return;
        mwc.load_favorite(chatlist_s);
    }

    //sets the Main_window_controller
    public void set_main_window_controller(Main_window_controller mwc)
    {
        this.mwc = mwc;
    }


}
