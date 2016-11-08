package main_interface;

import channel_logic.Channel_wrapper;
import channel_logic.irc_connection_and_parsers.Whisper_handler;
import channel_logic.misc_util.Constants;
import chat_interface.chatbox.Chatbox;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main_interface.favorites_whispers_options.Favorite_box_controller;
import main_interface.favorites_whispers_options.Options_controller;
import main_interface.favorites_whispers_options.Whisper_controller;
import main_interface.login.Login_information;
import main_interface.logviewer.Logviewer;
import main_interface.modlog.Modlog_handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Created by Leander
 * Controller for the main window of the application, handles the different chat_windows, tabs, whispers, favorite box, etc.
 */

public class Main_window_controller {
    private final static Logger LOGGER = Logger.getGlobal();


    @FXML
    private ListView<VBox> chat_list;

    @FXML
    private TextField channel_field;

    @FXML
    private Button channel_button;

    @FXML
    private Button options_button;

    @FXML
    private TextField favorite_textfield;

    @FXML
    private Button favorite_button;

    @FXML
    private ListView<HBox> favorite_list;

    @FXML
    private TabPane tab_pane;

    @FXML
    private ListView<VBox> whisper_list;

    @FXML
    private Tab whisper_tab;

    @FXML
    private Tab channel_tab;

    @FXML
    private TextField logviewer_username;

    @FXML
    private TextField logviewer_channel;

    private ObservableList<VBox> chat_windows = FXCollections.observableArrayList();
    private ObservableList<VBox> whisper_windows = FXCollections.observableArrayList();
    private HashMap<VBox,Whisper_controller> whisper_controllers = new HashMap<>();
    private DoubleProperty chat_windows_size = new SimpleDoubleProperty(0);
    private ObservableList<HBox> favorite_observable_list = FXCollections.observableArrayList();
    private DoubleProperty whisper_windows_size = new SimpleDoubleProperty(0);
    private Login_information login;
    private HashMap<String, Channel_wrapper> channel_wrapper_map = new HashMap<>();
    private ArrayList<String> chat_windows_stringlist = new ArrayList<>();
    private ArrayList<String> favorites_stringlist = new ArrayList<>();
    private ArrayList<VBox> saved_whisper_windows = new ArrayList<>();
    private ArrayList<String> saved_whisper_strings = new ArrayList<>();
    private ArrayList<String> current_whispers = new ArrayList<>();
    private Tab selected_tab = channel_tab;
    private Whisper_handler whisper_handler;
    private Modlog_handler modlog_handler;

    private Preferences favorites = Preferences.userNodeForPackage(Favorite_box_controller.class);




    @FXML
    void initialize()
    {

        //Need to manually add Label here to be able to change color later
        Label whisper_label = new Label("Whispers");
        whisper_label.setStyle("-fx-text-fill: black;-fx-rotate: 0");
        whisper_tab.setGraphic(whisper_label);

        //bind the chatlist size to the actual window size
        chat_list.maxWidthProperty().bind(tab_pane.widthProperty().subtract(40));//subtract so that the content is not hidden behind tabs
        chat_list.minWidthProperty().bind(tab_pane.widthProperty().subtract(40));
        chat_list.maxHeightProperty().bind(tab_pane.heightProperty());
        chat_list.minHeightProperty().bind(tab_pane.heightProperty());

        //bind the whisperlist size to the actual window size
        whisper_list.maxWidthProperty().bind(tab_pane.widthProperty().subtract(40));//subtract so that content is not hidden behind tabs
        whisper_list.minWidthProperty().bind(tab_pane.widthProperty().subtract(40));
        whisper_list.maxHeightProperty().bind(tab_pane.heightProperty());
        whisper_list.minHeightProperty().bind(tab_pane.heightProperty());


        //Initializes the cells for the actual chat view
        chat_list.setCellFactory(param -> new custom_cell_channels());
        chat_list.setItems(chat_windows);
        chat_list.setStyle("-fx-padding: 2px");//create small distance between different chat windows

        //Initializes the cells for the whisper view
        whisper_list.setCellFactory(param -> new custom_cell_whispers());
        whisper_list.setItems(whisper_windows);
        whisper_list.setStyle("-fx-padding: 2px");//Create small distance between different chat windows

        //Initializes the cells for the favorites view
        favorite_list.setItems(favorite_observable_list);
        favorite_list.setCellFactory(param -> new custom_cell_favorites());

        //adds a listener which detects enter presses
        channel_field.addEventFilter(KeyEvent.KEY_PRESSED,enter_validation());
        //adds a listener which detects tab changes
        tab_pane.getSelectionModel().selectedItemProperty().addListener((ov,oldTab,new_tab)->register_tab_change(new_tab));

        //Initializes the add button for the channels and the favorites
        channel_button.setStyle("-fx-max-height: 30px;-fx-min-height: 30px;-fx-max-width: 30px;-fx-min-width: 30px");
        channel_button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/Data/icons/green_plus_square.png"))));
        channel_button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        favorite_button.setStyle("-fx-max-height: 32px;-fx-min-height: 32px;-fx-max-width: 32px;-fx-min-width: 32px");
        favorite_button.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/Data/icons/green_plus_square.png"))));
        favorite_button.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);


        //Initializes the list of favorites;
        initialize_favorites_list();
    }

    //Registers a tab change, sets the according text in the channel_field, sets color of whisper text to black(if whisper_tab selected)
    private void register_tab_change(Tab new_tab)
    {
        selected_tab = new_tab;
        if(new_tab.equals(whisper_tab))
        {
            channel_field.setPromptText("Add whisper");
            Label whisper_label = new Label("Whispers");
            whisper_label.setStyle("-fx-text-fill: black;-fx-rotate: 0");
            whisper_tab.setGraphic(whisper_label);
        }
        else if(new_tab.equals(channel_tab))
        {
            channel_field.setPromptText("Add channel");
        }
    }

    //lookup name in the logviewer
    @FXML
    public void lookup_logs()
    {
        String user = logviewer_username.getText();
        String channel = logviewer_channel.getText();
        logviewer_channel.setText(null);
        logviewer_username.setText(null);
        if(user==null||user.equals("")||channel==null||channel.equals(""))return;
        Logviewer log = new Logviewer(user,channel);
        log.show_logs(10);
    }


    //initializes the favorite list with the saved values
    private void initialize_favorites_list()
    {
        String favorite_string = favorites.get("favorites","");
        if(favorite_string.equals("")){return;}
        String[] favorites = favorite_string.split(";");
        for(String f:favorites)
        {
            String name = f.substring(0,f.indexOf(","));
            String chatlist = f.substring(f.indexOf(","));
            add_favorite(name,chatlist);
        }
    }


    //Used to detect enter presses in the new channel field
    private EventHandler<KeyEvent> enter_validation()
    {
       return event ->
       {
           if(event.getCode().equals(KeyCode.ENTER))
           {
                event.consume();
                add_chat();
           }
       };
    }

    //adds a new chat to the interface
    private void add_chat(String chatname)
    {
        if(login==null)return;
        if(chatname==null||chatname.equals("")||chatname.equals("null"))return;
        VBox boundary = generate_channelbox(chatname);

        chat_windows_stringlist.add(chatname);
        chat_windows.add(boundary);

        //Add channel to modlogger
        if(modlog_handler!=null)modlog_handler.add_channel(chatname);
    }

    @FXML
    //called when clicking the + button, adds a new chat or whisper to the interface
    private void add_chat()
    {
        if(login==null)return;
        String s = channel_field.getText();
        if(s==null||s.equals(""))return;
        if(s.charAt(0)=='#')s = s.substring(1);
        channel_field.setText(null);
        if(!s.contains(",")) {
            if(selected_tab == null||selected_tab.equals(channel_tab))
            {
                add_chat(s);
            }
            else if(selected_tab.equals(whisper_tab))
            {
                add_whisperbox(s);
            }
        }
        else
        {
            for(String chat:s.split(","))
            {
                if(selected_tab==null||selected_tab.equals(channel_tab))
                {
                    add_chat(chat);
                }
                else if(selected_tab.equals(whisper_tab))
                {
                    add_whisperbox(chat);
                }
            }
        }
    }

    @FXML
    //called when clicking the options button, opens the option menu
    private void open_options()
    {
        try
        {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("layout_elements/options_interface.fxml"));
            HBox options = fxmlLoader.load();
            Options_controller options_controller = fxmlLoader.getController();
            Stage options_stage = new Stage();
            options_stage.getIcons().add(new Image(getClass().getResourceAsStream("/Data/icons/options_icon.png")));
            options_stage.setScene(new Scene(options,600,600));
            options_stage.setResizable(false);
            options_stage.show();

        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}

    }

    //sets the login, called by mainwindow after initializing
    void set_login(Login_information login, boolean read_only)
    {
        this.login = login;
        if(!read_only)
        {
            start_whisper_handler();
            start_modlog_handler();
        }
    }

    //in case of an actual login(not read only) whisper handler is started
    private void start_whisper_handler()
    {
        whisper_handler = new Whisper_handler(login.get_user(), login.get_pw(), this);
        whisper_handler.start();
    }

    //in case of an actual login(not read only) mod_logger is started
    private void start_modlog_handler()
    {
        modlog_handler = new Modlog_handler(login);
    }

    //opens the modlogs of a specific channel, first checks if modlog_handler exists
    private void open_modlogs(String channel)
    {
        if(modlog_handler!=null)
        {
            modlog_handler.display_modlog(channel);
        }
    }

    //Forwards the whisper to the corresponding chatbox, as add_whisper is called from outside of the application thread
    //it forwards the request in a threadsafe manner
    public void add_whisper(String[] whisper)
    {
        Platform.runLater(() -> add_whisper_internal(whisper));
    }

    //adds a whisper to the corresponding chatbox, if not already created creates new one
    private void add_whisper_internal(String[] whisper)
    {
        if(whisper.length!=3||whisper[0].equals("NULL"))return;
        //Color tab red to indicate new whispers
        if(selected_tab==null||!selected_tab.equals(whisper_tab))
        {
            Label whisper_label = new Label("Whispers");
            whisper_label.setStyle("-fx-text-fill: red;-fx-font-weight: bold;-fx-rotate: 0;");
            Platform.runLater(() ->whisper_tab.setGraphic(whisper_label));
        }
        //check if whisperbox of user is not open yet, open if that's the case
        if(!current_whispers.contains(whisper[0]))
        {
            add_whisperbox(whisper[0]);
        }
        ReadOnlyDoubleProperty widthproperty = whisper_windows.get(current_whispers.indexOf(whisper[0])).widthProperty();
        Chatbox c = new Chatbox(new Constants("",login.get_user(),login.get_pw()),widthproperty);
        c.fill_whisper_message(whisper);
        Node n = c.get_node();
        whisper_controllers.get(whisper_windows.get(current_whispers.indexOf(whisper[0]))).add_whisper(n);
    }

    //sends a whisper, called by whisper_controllers
    public void send_whispers(String user, String message)
    {
        whisper_handler.write_whisper(user,message);

        //display own message, not in correct color atm
        String[] whisper = {login.get_user(),"#000000",message};
        ReadOnlyDoubleProperty widthproperty = whisper_windows.get(current_whispers.indexOf(user)).widthProperty();
        Chatbox c = new Chatbox(new Constants("",login.get_user(),login.get_pw()),widthproperty);
        c.fill_whisper_message(whisper);
        Node n = c.get_node();
        whisper_controllers.get(whisper_windows.get(current_whispers.indexOf(user))).add_whisper(n);
    }


    //adds a whisperbox of a specific user to the chat, checks if chat is cached
    private void add_whisperbox(String user)
    {
        if(current_whispers.contains(user))return;
        VBox new_whisperbox;
        whisper_windows_size.setValue(whisper_windows_size.doubleValue()+1);
        current_whispers.add(user);
        if(saved_whisper_strings.contains(user))
        {
            new_whisperbox = saved_whisper_windows.get(saved_whisper_strings.indexOf(user));
        }
        else
        {
            new_whisperbox = generate_whisperbox(user);
            saved_whisper_strings.add(user);
            saved_whisper_windows.add(new_whisperbox);
        }
        whisper_windows.add(new_whisperbox);
    }

    //removes a whisperbox from the whisper list, adds it to the cache
    public void remove_whisperbox(VBox whisperbox_to_remove,String user)
    {
        whisper_windows.remove(whisperbox_to_remove);
        current_whispers.remove(user);
        whisper_windows_size.setValue(whisper_windows_size.doubleValue()-1);

        if(!saved_whisper_strings.contains(user))
        {
            saved_whisper_strings.add(user);
            saved_whisper_windows.add(whisperbox_to_remove);
        }

    }

    //generates the actual whisperbox to add to chat
    private VBox generate_whisperbox(String user)
    {
        VBox whisper_window = null;
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("layout_elements/whisper_chatwindow.fxml"));
            whisper_window = fxmlLoader.load();
            Whisper_controller whisper_controller = fxmlLoader.getController();
            whisper_controllers.put(whisper_window,whisper_controller);
            whisper_controller.initialize_values(user,this);
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
        return whisper_window;
    }

    //generates the actual chatbox to add to chat
    private VBox generate_channelbox(String channel)
    {
        try {
            chat_windows_size.set(chat_windows_size.doubleValue() + 1);//increase chat counter by one
            System.out.println("\nAdding new chat: " + channel);
            Channel_wrapper new_chat = new Channel_wrapper(channel, login);
            channel_wrapper_map.put(channel,new_chat);
            VBox new_chat_vbox = new_chat.get_chatwindow();
            VBox boundary = new VBox();
            boundary.setStyle("-fx-border-color: black;-fx-border-width: 2pt");//set black border around chatwindow

            //Bind the different elemements widths and heights to each other
            boundary.prefHeightProperty().bind(chat_list.heightProperty().subtract(50));

            new_chat_vbox.prefHeightProperty().bind(chat_list.heightProperty().subtract(30));//subtract so that boundary is visible
            new_chat_vbox.maxHeightProperty().bind(chat_list.heightProperty().subtract(30));//and that there is enough space for options
            new_chat_vbox.maxHeightProperty().bind(chat_list.heightProperty().subtract(30));

            new_chat_vbox.prefWidthProperty().bind(boundary.widthProperty().subtract(6));//subtract so that boundary is visible
            new_chat_vbox.maxWidthProperty().bind(boundary.widthProperty().subtract(6));
            new_chat_vbox.minWidthProperty().bind(boundary.widthProperty().subtract(6));

            //End of binding - Create additional info&options bar at the top
            HBox infobox = new HBox();
            infobox.setMaxHeight(15);
            infobox.setStyle("-fx-border-color: black;-fx-border-width: 1pt");
            Text channel_t = new Text(" " + channel);
            channel_t.setStyle("-fx-font-weight: bold");
            //Create buttons for the infobox
            String buttonstyle = "-fx-background-radius: 5em;-fx-min-width: 10px;-fx-min-height: 10px;-fx-max-height: 10px;-fx-max-width: 10px";
            Button refresh = new Button();
            refresh.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/Data/icons/refresh_icon.png"))));
            refresh.setStyle(buttonstyle);
            refresh.setOnAction(event -> refresh_channel(boundary, channel));
            Button close = new Button();
            close.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/Data/icons/close_icon.png"))));
            close.setStyle(buttonstyle);
            close.setOnAction(event -> remove_channel(boundary, channel));

            //Add HBox which contains the button to open modlogs for this specfic channel
            HBox modlog_bounds = new HBox();
            HBox.setHgrow(modlog_bounds, Priority.ALWAYS);
            modlog_bounds.setAlignment(Pos.CENTER_RIGHT);
            Button modlogs = new Button();
            modlogs.setStyle("-fx-background-radius: 10em;-fx-min-width: 50px;-fx-max-width: 50px;-fx-max-height: 20px;-fx-min-height: 20px");
            Text t = new Text("Modlogs");
            t.setStyle("-fx-font-weight: bold;-fx-font-size: 8pt");
            modlogs.setGraphic(t);
            modlogs.setOnAction(event -> open_modlogs(channel));
            modlog_bounds.getChildren().add(modlogs);

            //Define HBox which acts as seperator for the buttons
            HBox button_bounds = new HBox(5);
            button_bounds.setStyle("-fx-alignment: center-left");
            button_bounds.setPadding(new Insets(0, 0, 0, 5));
            button_bounds.maxHeight(10);
            button_bounds.maxWidth(21);
            button_bounds.getChildren().addAll(close, refresh);
            infobox.getChildren().addAll(button_bounds, channel_t,modlog_bounds);

            boundary.getChildren().addAll(infobox, new_chat_vbox);

            return boundary;
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
        return null;
    }


    //removes a channelview from the listview and readds a new instance of that channel at the same position (acts as a refresh)
    private void refresh_channel(VBox channel_to_refresh, String channel)
    {
        int index = chat_windows.indexOf(channel_to_refresh);
        chat_windows.remove(channel_to_refresh);
        //Shutdown the channel wrapper and remove from map
        channel_wrapper_map.get(channel).shutdown();
        channel_wrapper_map.remove(channel);

        chat_windows_size.setValue(chat_windows_size.doubleValue()-1);
        VBox refreshed_channel = generate_channelbox(channel);
        chat_windows.add(index,refreshed_channel);
    }
    //removes a channel from the listview
    private void remove_channel(VBox channel_to_remove, String channel)
    {
        //Shutdown the channel wrapper and remove from map
        channel_wrapper_map.get(channel).shutdown();
        channel_wrapper_map.remove(channel);

        chat_windows_stringlist.remove(chat_windows_stringlist.indexOf(channel));//Ensure only one instance gets removed
        chat_windows.remove(channel_to_remove);
        chat_windows_size.setValue(chat_windows_size.doubleValue()-1);
    }

    //called when clicking the add favorites button
    @FXML
    private void add_favorite()
    {
        if(favorite_observable_list.size()>=7)return;
        try {
            String name = favorite_textfield.getText();
            favorite_textfield.setText(null);
            String chatlist = "";
            for (String s : chat_windows_stringlist) {
                chatlist += "," + s;
            }
            add_favorite(name,chatlist);
        }catch (Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    //adds a generic favorite to the favorite list
    private void add_favorite(String name, String chatlist)
    {
        try {
            if (name == null || name.equals("")||chatlist.equals(""))return;
            chatlist = chatlist.substring(1);//strip the first comma
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("layout_elements/favorite_list_box.fxml"));
            HBox new_favorite = fxmlLoader.load();
            Favorite_box_controller new_favorite_controller = fxmlLoader.getController();
            new_favorite_controller.set_name(name);
            new_favorite_controller.set_chatlist(chatlist);
            new_favorite_controller.set_main_window_controller(this);
            favorite_observable_list.add(new_favorite);
            favorites_stringlist.add(name+","+chatlist);
            update_favorite_preferences();
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}

    }

    //removes a favorite from the list
    public void remove_favorite(HBox favorite_to_remove)
    {
        int index = favorite_observable_list.indexOf(favorite_to_remove);
        favorite_observable_list.remove(index);
        favorites_stringlist.remove(index);
        update_favorite_preferences();
    }

    //Loads the favorite , called from Favorite_box_controller
    public void load_favorite(String chatlist)
    {
        String[] chats_to_add = chatlist.split(",");
        for(String chat:chats_to_add)
        {
            add_chat(chat);
        }
    }

    //Update the saved preferences for favorites
    private void update_favorite_preferences()
    {
        String favorite_save = "";
        for(String s:favorites_stringlist)
        {
            favorite_save += ";"+s;
        }
        if(!favorite_save.equals(""))favorite_save = favorite_save.substring(1);

        favorites.put("favorites",favorite_save);
    }

    //custom cell class used for displaying different chat windows, has small distance between cells and always resizes content
    //so that no scrolling is required
    private class custom_cell_channels extends ListCell<VBox>
    {
        custom_cell_channels()
        {
            setStyle("-fx-padding: 2px;-fx-background-insets: 0px,2px");//create small distance between individual cells
        }

        @Override
        protected void updateItem(VBox item, boolean empty)
        {
            super.updateItem(item,empty);

            if(item != null) {
                SimpleDoubleProperty six = new SimpleDoubleProperty(6);
                prefWidthProperty().bind(chat_list.widthProperty().divide(chat_windows_size).subtract(six.divide(chat_windows_size)));
                maxWidthProperty().bind(chat_list.widthProperty().divide(chat_windows_size).subtract(six.divide(chat_windows_size)));
                minWidthProperty().bind(chat_list.widthProperty().divide(chat_windows_size).subtract(six.divide(chat_windows_size)));
                item.prefWidthProperty().bind(prefWidthProperty().subtract(2));
                item.maxWidthProperty().bind(prefWidthProperty().subtract(2));
                item.minWidthProperty().bind(prefWidthProperty().subtract(2));
            }
            setGraphic(item);
        }

    }

    //custom cell class used for displaying different whisper windows, resizes so that no scrolling is required
    private class custom_cell_whispers extends ListCell<VBox>
    {
        custom_cell_whispers()
        {
            setStyle("-fx-padding: 2px;-fx-background-insets: 0px,2px");
        }

        @Override
        protected void updateItem(VBox item, boolean empty)
        {
            super.updateItem(item,empty);

            if(item!=null)
            {
                SimpleDoubleProperty six = new SimpleDoubleProperty(12);
                //bind only the maxwidth, so that individual whisper windows are still created at pref size, but get smaller when needed
                maxWidthProperty().bind(whisper_list.widthProperty().divide(whisper_windows_size).subtract(six.divide(whisper_windows_size)));
                minWidthProperty().bind(whisper_list.widthProperty().divide(whisper_windows_size).subtract(six.divide(whisper_windows_size)));

                maxHeightProperty().bind(whisper_list.heightProperty());
                minHeightProperty().bind(whisper_list.heightProperty());

                item.maxWidthProperty().bind(maxWidthProperty().subtract(2));
                //item.minWidthProperty().bind(maxWidthProperty().subtract(2));
                item.maxHeightProperty().bind(prefHeightProperty());
                item.minHeightProperty().bind(prefHeightProperty());
            }
            setGraphic(item);
        }
    }

    //custom class for cells of the favorite box, ensures cells have the correct height
    private class custom_cell_favorites extends ListCell<HBox>
    {
        @Override
        protected void updateItem(HBox item, boolean empty) {
            if(item!=null) {
                super.updateItem(item, empty);
                maxHeightProperty().bind(item.heightProperty());
                minHeightProperty().bind(item.heightProperty());
                item.maxWidthProperty().bind(widthProperty());
                setGraphic(item);
            }
            else
            {
                setGraphic(null);
            }
        }
    }
}
