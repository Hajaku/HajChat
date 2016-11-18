package chat_interface.chat_window;

import channel_logic.Channel_handler;
import channel_logic.misc_util.Constants;
import chat_interface.chatbox.Chatbox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import main_interface.Main_window;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by Leander on 19.08.2016.
 * Class which represents a chatwindow for a specific channel, also handles adding new messages which it receives from
 * the string handler class
 */
public class Chat_window {
    private final static Logger LOGGER = Logger.getGlobal();


    private VBox mainwindow;
    private Chat_window_controller controller;
    private Constants c;
    private Chat_window cw;
    private Channel_handler channel_handler;
    private ArrayList<Chatbox> chatboxes = new ArrayList<>();
    private ArrayList<Timeout_wrapper> timeout_list = new ArrayList<>();

    Chat_window(Constants c)
    {
        try {
            cw = this;
            this.c = c;
            c.get_imagehandler().initialize();
            FXMLLoader fxmlLoader = new FXMLLoader(Main_window.class.getResource("layout_elements/chatwindow.fxml"));
            mainwindow = fxmlLoader.load();
            controller = fxmlLoader.getController();
            controller.initialize(c);
        }catch(Exception e){
            LOGGER.info(e.getMessage());
        }
    }


    //Retrieve mainwindow
    public VBox get_chatwindow()
    {
        return mainwindow;
    }


    //Method that gets called from the stringhandler, handles the different types of messages that can exist.
    public void add_message(String[] message)
    {
        //In case of the initial state message wait until controller is initialized
        if(message[0].equals("CHATSTATE"))
        {
            while(controller==null){}
        }
        //Check that everything is loaded properly
        else if(message.length!=6){return;}
        else if(message[0].equals("NULL")){return;}
        else if(controller==null){return;}

        boolean cheer = false;
        switch (message[0]) {
            case "CHEERMSG":
                cheer = true;
            case "PRVMSG":
                //TODO disabled in this build due to caching bug (colors disappearing)
                //Chatelement_wrapper chat1 = controller.get_free_chatbox();

                Chatelement_wrapper chat1;
                if(true || chat1==null)
                {
                    chat1 = new Chatelement_wrapper(new Chatbox(c, cw, mainwindow.widthProperty()));
                }
                else
                {
                    //System.out.println("Reused chatbox"); //TODO remove this
                }
                Chatbox chat = chat1.get_chatbox_direct();
                chat.fill_chat_message(message, cheer);
                add_chatboxlist(chat);
                controller.add_node(chat1);
                break;

            case "SUB":
                if( ! message[4].equals("NULL"))//If resub message is given
                {
                    Text subtext = new Text(message[5]);
                    subtext.setStyle("-fx-font-weight: bold");
                    subtext.wrappingWidthProperty()
                            .bind(mainwindow.widthProperty().subtract(20));//ensure text has correct width
                    Chatbox chat2 = new Chatbox(c,cw,mainwindow.widthProperty());
                    chat2.fill_chat_message(message,false);
                    add_chatboxlist(chat2);
                    Node n = chat2.get_node();
                    VBox wrapper = new VBox(0);
                    wrapper.getChildren().addAll(subtext,n);
                    wrapper.setStyle("-fx-background-color: coral");
                    controller.add_message(wrapper);
                }
                else  //If no resub message is given
                {
                    controller.add_text(message[5]);
                }
                break;

            case "SLOW":
                controller.set_slowmode_indicator((message[5]));
                controller.add_text("Slowmode has been set to "+message[5]+"seconds");
                break;

            case "BAN":
                //checks that timeout messages do not repeat
                if(check_timeout_for_timeoutlist(message[1]))return;

                if(message[5].equals("NULL"))
                {
                    controller.add_text(message[1]+" has been banned.");
                }
                else
                {
                    String reason = message[4].replaceAll("\\\\s"," ");
                    controller.add_text(message[1]+" has been timed out for "+message[5]+" seconds");
                    if(!reason.equals(" "))
                    {
                        controller.add_text("Reason: "+reason);
                    }
                }
                user_banned(message[1]);
                timeout_list.add(new Timeout_wrapper(message[1]));

                break;

            case "STATUSUPDATE":
                String[] splitted_input = message[4].split("=");
                switch(splitted_input[0])
                {
                    case "sub":
                        controller.set_sub_indicator(splitted_input[1].charAt(0)=='1');
                        break;
                    case "emote":
                        controller.set_emote_indicator(splitted_input[1].charAt(0)=='1');
                        break;
                    case "r9k":
                        controller.set_r9k_indicator(splitted_input[1].charAt(0)=='1');
                        break;
                }
                break;

            case "MODS":
                controller.add_text(message[4]);
                break;

            case "CHATSTATE":
                String status = message[4];
                controller.set_sub_indicator(status.charAt(0)=='1');
                controller.set_emote_indicator(status.charAt(1)=='1');
                controller.set_r9k_indicator(status.charAt(2)=='1');
                controller.set_slowmode_indicator(status.substring(3));
                break;
        }
    }

    //add to chatboxlist
    private void add_chatboxlist(Chatbox new_chatbox)
    {
        chatboxes.add(0,new_chatbox);
        if(chatboxes.size()>c.get_chatboxcount())
        {
            chatboxes.remove(chatboxes.size()-1);
        }
    }

    //checks if a message concerning the timeout of the given user was posted in the last n seconds
    private boolean check_timeout_for_timeoutlist(String name_to_check)
    {
        update_timeoutlist();
        for(Timeout_wrapper tw:timeout_list)
        {
            if(tw.username_matches(name_to_check))return true;
        }
        return false;
    }

    //updates the timeoutlist, removes all entries older than n seconds
    private void update_timeoutlist()
    {
        for(int i = 0; i<timeout_list.size();i++)
        {
            if(timeout_list.get(i).timeout_decayed(5000)){timeout_list.remove(i);--i;}//5000= 5 seconds, no repeat of banphrases for 5 seconds
        }
    }

    //Called when a user is banned, fades that specific message
    private void user_banned(String user)
    {
        for(Chatbox c:chatboxes){if(c!=null)c.check_user_ban(user);}
    }


    //pass irc_channel_handler to the controller
    public void pass_channel_handler(Channel_handler channel_handler)
    {
        while(controller==null){}
        this.channel_handler = channel_handler;
        controller.set_channel_handler(channel_handler);
    }


    //get irc_channel_handler
    public Channel_handler get_channel_handler()
    {
        return channel_handler;
    }


    //private class that acts as a wrapper for names and their timeouttime
    private class Timeout_wrapper
    {
        private String user;
        private long timeout_date;

        //initializes the username and sets the timeout_date to the current time in milliseconds
        Timeout_wrapper(String username)
        {
            user = username;
            timeout_date = System.currentTimeMillis();
        }

        //tests if the given username equals the saved username
        boolean username_matches(String user)
        {
            return this.user.equals(user);
        }

        boolean timeout_decayed(int decay_time_in_milliseconds)
        {
            return decay_time_in_milliseconds<(System.currentTimeMillis()-timeout_date);
        }

    }


}
