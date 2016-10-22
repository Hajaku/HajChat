package main_interface.favorites_whispers_options;

import channel_logic.misc_util.Constants;
import channel_logic.misc_util.Saver;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Created by Leander
 * Controller for the options window, initializes the options with currently set values and saves new values if changed
 */

public class Options_controller {
    private final static Logger LOGGER = Logger.getGlobal();

    @FXML
    private ColorPicker highlight_picker;

    @FXML
    private ChoiceBox<Integer> buttoncount_picker;

    @FXML
    private Text keyword_display;

    @FXML
    private Text keycode_display;

    @FXML
    private Button keycode_button;

    @FXML
    private ListView<String> button_times_list;

    @FXML
    private TextField new_keywords;

    @FXML
    private ChoiceBox<String> chatboxstyle_picker;

    private String[] timeout_times_old;
    private boolean initialized = false;

    @FXML
    void request_new_keycode()
    {
        Text enter_key = new Text("Enter new keycode");
        HBox boundary = new HBox();
        boundary.getChildren().addAll(enter_key);
        Scene keyrecorder = new Scene(boundary,30,30);
        Stage keystage = new Stage();
        keystage.setScene(keyrecorder);
        keyrecorder.setOnKeyPressed(keyevent ->{
            keycode_display.setText(keyevent.getCode().getName());
            keyevent.consume();
            keystage.close();
        });
        keystage.show();
    }

    @FXML
    //saves all the changes made in the options
    void save_changes(ActionEvent event)
    {
        Saver save = new Saver();

        String chatboxtype = chatboxstyle_picker.getValue().equals("Compact")?"1":"0";

        String buttoncount_s = ""+buttoncount_picker.getValue();
        String timeoutdurations_s = "";
        for(int i = 0;i<button_times_list.getItems().size();i++)
        {
            timeoutdurations_s += ","+button_times_list.getItems().get(i);
        }
        timeoutdurations_s = timeoutdurations_s.substring(1);//cut first comma
        String keywords_s = new_keywords.getText();
        if(keywords_s.equals(""))keywords_s=null;
        String keycode_s = keycode_display.getText();
        String color_s = highlight_picker.getValue().toString().substring(2,8); // only select actual hexcode, cut 0x and alpha

        save.save_state(buttoncount_s,timeoutdurations_s,keywords_s,keycode_s,color_s,chatboxtype);

        //Close options menu after saving
        Node n  = (Node)event.getSource();
        Stage stage = (Stage)n.getScene().getWindow();
        stage.close();
    }


    @FXML
    //Loads the config file and sets the currently selected values for all option fields
    private void initialize()
    {
        ObservableList<String> chatboxstyles = FXCollections.observableArrayList("Normal","Compact");
        chatboxstyle_picker.setItems(chatboxstyles);

        ObservableList<Integer> buttoncount = FXCollections.observableArrayList(1,2,3,4,5);
        buttoncount_picker.setItems(buttoncount);
        ObservableList<String> timeoutdurations = FXCollections.observableArrayList();

        buttoncount_picker.addEventHandler(ActionEvent.ANY, event -> update_buttontime_list());

        try
        {
            Preferences current_state = Preferences.userNodeForPackage(Constants.class);

            String compact_boxes = current_state.get("chatbox_type","");
            if(!compact_boxes.equals(""))chatboxstyle_picker.setValue(compact_boxes.equals("1")?"Compact":"Normal");

            String button_count = current_state.get("buttoncount","");
            if(!button_count.equals(""))buttoncount_picker.setValue(Integer.parseInt(button_count));

            String[] times = current_state.get("button_times","").split(",");
            if(times.length>0&&!times[0].equals(""))
            {
                timeout_times_old = times;
                for(String s:times)
                {
                    timeoutdurations.add(s);
                }
                button_times_list.setItems(timeoutdurations);
                button_times_list.setCellFactory(TextFieldListCell.forListView());
                //Ensure only numbers are entered.
                button_times_list.setOnEditCommit(event -> {
                    if(!event.getNewValue().matches("^\\d+$"))
                    {
                        event.consume();
                    }
                    else
                    {
                        button_times_list.getItems().set(event.getIndex(),event.getNewValue());
                    }
                });
            }

            String keywords = current_state.get("keywords","");
            if(!keywords.equals(""))keyword_display.setText(keywords);

            String keycode = current_state.get("key_code","");
            if(!keycode.equals(""))keycode_display.setText(keycode);

            String color = current_state.get("color_keywords","");
            if(!color.equals(""))
            {
                String hexcolor = current_state.get("color_keywords","");
                int hex = Integer.parseInt(hexcolor,16);
                int r = (hex & 0xFF0000)>>16;
                int g = (hex & 0xFF00)>>8;
                int b = (hex & 0xFF);
                highlight_picker.setValue(new Color(r/255.0,g/255.0,b/255.0,1));
            }

            initialized = true;
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }


    //Refreshes the timeout list after a buttoncount is picked
    private void update_buttontime_list()
    {
        if(!initialized)return;
        button_times_list.getItems().clear();
        for(int i = 0;i<buttoncount_picker.getValue()-1;i++)
        {
            if(i<timeout_times_old.length)
            {
                button_times_list.getItems().add(timeout_times_old[i]);
            }
            else
            {
                button_times_list.getItems().add("1");
            }
        }
    }


}
