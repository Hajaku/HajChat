package chat_interface.chat_window;

import channel_logic.Channel_handler;
import channel_logic.misc_util.Constants;
import chat_interface.chatbox.Chatbox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.ArrayList;

/**
 * Created by Leander
 * Controller of the chat window, contains logic regarding the automatic resizing of chatboxes inside of the chatwindow
 * and takes care of the funtionality of the buttons&textfield of the chatwindow
 */

public class Chat_window_controller {

    @FXML
    private Text slow_label;

    @FXML
    private Circle indicator_sub;

    @FXML
    private Circle indicator_emote;

    @FXML
    private Circle indicator_r9k;

    @FXML
    private TextArea messagefield;

    @FXML
    private VBox window;

    @FXML
    private Button send_button;

    @FXML
    private ListView<Node> scroll_pane;
    private ObservableList<Node> chatmessages = FXCollections.observableArrayList();
    private ArrayList<Chatelement_wrapper> chatwrappers = new ArrayList<>();
    private ArrayList<Chatelement_wrapper> cleared_chatboxes = new ArrayList<>();
    private ScrollBar scrollbar;

    private ArrayList<Chatelement_wrapper> message_cache = new ArrayList<>();

    private boolean chatpaused = false;
    private boolean focused = false;

    private Channel_handler channel_handler;

    private Constants constants;


    //called after creating new chat window controller
    @FXML
    void initialize(Constants constants)
    {
        this.constants = constants;
        scroll_pane.setCellFactory(param -> new custom_cell());
        scroll_pane.setItems(chatmessages);
        //Add enter detection to messagefield
        messagefield.addEventFilter(KeyEvent.KEY_PRESSED,enter_validation());
    }

    @FXML
    void mouse_enter(MouseEvent event)
    {
        focused = true;
        scroll_pane.requestFocus();
    }

    @FXML
    void mouse_exit(MouseEvent event)
    {
        focused = false;
        unpause_chat();
    }

    //pauses the chat
    private void pause_chat()
    {
        if(focused){chatpaused = true;}
    }

    //unpauses the chat
    private void unpause_chat()
    {
        chatpaused = false;
        clear_cache();
    }

    //handles the keypress to pause the chat
    @FXML
    public void pause_chat_key(KeyEvent event)
    {
        if(focused&&event.getCode().equals(constants.get_keycode()))
        {
            pause_chat();
        }
    }

    //handles the keyrelease to unpause the chat
    @FXML
    public void unpause_chat_key(KeyEvent event)
    {
        if(event.getCode().equals(constants.get_keycode()))
        {
            unpause_chat();
        }
    }

    //set status of the subonly indicator
    void set_sub_indicator(boolean status)
    {
        if(status)
        {
            Platform.runLater(() -> indicator_sub.setStyle("-fx-fill: lawngreen"));
        }
        else
        {
            Platform.runLater(() -> indicator_sub.setStyle("-fx-fill: red"));
        }

    }

    //set status of the emoteonly indicator
    void set_emote_indicator(boolean status)
    {
        if(status)
        {
            Platform.runLater(() -> indicator_emote.setStyle("-fx-fill: lawngreen"));
        }
        else
        {
            Platform.runLater(() -> indicator_emote.setStyle("-fx-fill: red"));
        }
    }

    //set status of the r9k indicator
    void set_r9k_indicator(boolean status)
    {
        if(status)
        {
            Platform.runLater(() -> indicator_r9k.setStyle("-fx-fill: lawngreen"));
        }
        else
        {
            Platform.runLater(() -> indicator_r9k.setStyle("-fx-fill: red"));
        }
    }

    //set slowmode display
    void set_slowmode_indicator(String slow)
    {
        Platform.runLater(() -> slow_label.setText("Slow: "+slow));
    }

    //Fetches message from the textfield and sends it to the string handler
    @FXML
    private void send_button_pressed() {
        String text = messagefield.getText();
        if(text==null){return;}//prevents enter hold spam, prevents empty messages being sent
        System.out.println(text);
        channel_handler.get_irc_handler().write_string(text);
        messagefield.setText(null);
    }

    //Used to detect enter presses
    private EventHandler<KeyEvent> enter_validation()
    {
        return event -> {
            if(event.getCode().equals(KeyCode.ENTER))
            {
                event.consume();
                send_button_pressed();
            }
        };
    }

    //sets the channel handler, needed for sending messages
    void set_channel_handler(Channel_handler channel_handler)
    {
        this.channel_handler = channel_handler;
    }


    //wraps the chatbox in a wrapper and forwards it to add_node, wrapper is used for chatbox reuse
    void add_message(Chatbox chat)
    {
        add_node(new Chatelement_wrapper(chat));
    }

    //wraps the node in a wrapper and forwards it to add_node

    void add_message(Node node)
    {
        add_node(new Chatelement_wrapper(node));
    }


    //adds a String message to the chat, mainly used for status messages
    void add_text(String s)
    {
        Text text = new Text(s);
        text.wrappingWidthProperty().bind(scroll_pane.widthProperty().subtract(20));
        add_node(new Chatelement_wrapper(text));
    }

    //adds a node to the chatbox
    void add_node(Chatelement_wrapper chat)
    {
        //Request focus when mouse is hovered, prevents bug of chat getting stuck
        if(focused)Platform.runLater(() ->scroll_pane.requestFocus());
        if(!chatpaused) {


            Node n = chat.get_node();
            Platform.runLater(() ->
            {
                chatmessages.add(n);
                chatwrappers.add(chat);
                cleanup_messages();
                scroll_pane.scrollTo(n);
            });
        }
        else
        {
            message_cache.add(chat);
        }

    }

    //Clears the message cache and adds each message to the chatbox
    private void clear_cache()
    {
        for(Chatelement_wrapper n:message_cache)
        {
            chatmessages.add(n.get_node());
            chatwrappers.add(n);
            scroll_pane.scrollTo(n.get_node());
            cleanup_messages();
        }
        message_cache.clear();
    }

    //removes old messages from the chat, prevents endless growth
    private void cleanup_messages()
    {
        while(chatmessages.size()>constants.get_chatboxcount())
        {
            Chatelement_wrapper cleared_chatbox = chatwrappers.get(0).clear_and_get_wrapper();
            if(cleared_chatbox!=null&&cleared_chatboxes.size()<100)cleared_chatboxes.add(cleared_chatbox);
            chatmessages.remove(0);
            chatwrappers.remove(0);
        }
    }

    //return a chatbox_wrapper from the list of freed chatboxwrappers, returns null if no chatbox is freed
    Chatelement_wrapper get_free_chatbox()
    {
        if(cleared_chatboxes.size()==0)return null;
        Chatelement_wrapper return_chatbox = cleared_chatboxes.get(0);
        cleared_chatboxes.remove(0);
        return return_chatbox;
    }

    //Custom cell used to display chatboxes, binds height and width of cell to content in case message is a VBox or HBox
    //uses standard cell otherwise
    private class custom_cell extends ListCell<Node>
    {

        custom_cell(){}

        @Override
        protected void updateItem(Node item, boolean empty)
        {
            super.updateItem(item, empty);

            if(!empty&&item instanceof Text)
            {
                setGraphic(item);
            }
            else if(!empty&&item instanceof  VBox)
            {
                VBox v = (VBox)item;
                minHeightProperty().bind(v.heightProperty());
                //prefHeightProperty().bind(v.heightProperty());
                //maxHeightProperty().bind(v.heightProperty());
                setGraphic(v);
            }
            else if(!empty &&item instanceof HBox)
            {
                HBox h = (HBox)item;
                minHeightProperty().bind(h.heightProperty());
                //prefHeightProperty().bind(h.heightProperty());
                //maxHeightProperty().bind(h.heightProperty());
                setGraphic(item);
            }
            else
            {
                setGraphic(null);
            }

        }
    }

}

