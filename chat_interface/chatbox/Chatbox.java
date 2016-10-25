package chat_interface.chatbox;

import channel_logic.misc_util.Constants;
import channel_logic.misc_util.Image_handler;
import chat_interface.chat_window.Chat_window;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.logging.Logger;

/**
 * Created by Leander on 17.08.2016.
 * Represents a generic chatbox object to be added to chatwindows, can be filled with normal or whisper messages
 */
public class Chatbox {
    private final static Logger LOGGER = Logger.getGlobal();

    private HBox chatbox;
    private FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/main_interface/layout_elements/chatbox.fxml"));
    private Chatbox_controller controller;
    private Constants constants;
    private Button[] buttons;
    private Image_handler imh;
    private String user = "";
    private Chat_window cw;

    //Constructor for the chatbox, used for regular chatboxes
    public Chatbox(Constants constants, Chat_window cw, ReadOnlyDoubleProperty widthproperty)
    {
        try {
            this.cw = cw;
            this.constants = constants;
            imh = constants.get_imagehandler();
            chatbox = fxmlLoader.load();
            controller = fxmlLoader.getController();
            //subtract 30 so content is not hidden behind scrollbar
            chatbox.maxWidthProperty().bind(widthproperty.subtract(30));
            chatbox.minWidthProperty().bind(widthproperty.subtract(30));


        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    //Constructor for the chatbox, used for whisperboxes
    public Chatbox(Constants c, ReadOnlyDoubleProperty widthproperty)
    {
        try {
            constants = c;
            imh = constants.get_imagehandler();
            chatbox = fxmlLoader.load();
            controller = fxmlLoader.getController();
            //subtract 30 so content is not hidden behind scrollbar
            chatbox.maxWidthProperty().bind(widthproperty.subtract(30));
            chatbox.minWidthProperty().bind(widthproperty.subtract(30));
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    //Called on user ban, calls same method in controller
    public void check_user_ban(String user)
    {
        if(this.user.toLowerCase().equals(user.toLowerCase())&&controller!=null){controller.user_ban();}
    }

    //resets the chatbox to an empty state
    public void reset_chatbox()
    {
        controller.reset_chatbox();
    }

    //Called to fill the chatbox with a normal message
    //String[] layout normal chat:   [MSGTYPE,USER,BADGES,COLOR,MSG,TIME]
    public void fill_chat_message(String[] message, boolean cheer)
    {
        Platform.runLater(() -> {
            add_buttons();
            add_badges(message[2]);
            user = message[1];
            add_username(user, message[3]);
            add_message(message[4], cheer);
            check_keywords(message[4]);
        });
    }

    //called to fill the chatbox with a whisper message
    //String[] layout whisper chat: [USER,COLOR,MESSAGE]
    public void fill_whisper_message(String[] message)
    {
        System.out.println(message[0]+" "+message[1]+" "+message[2]);
        Platform.runLater(() -> {
            user = message[0];
            add_username(user, message[1]);
            add_message(message[2], false);
            System.out.println(message[2]);
        });
    }


    //Returns the generated node which contains all the content
    public Node get_node()
    {
        return chatbox;
    }

    //checks the message for keywords, if found marks message
    private void check_keywords(String message)
    {
        message = message.replaceAll("\\(\\d+\\)"," ");
        //Check for mention
        if(message.toLowerCase().contains(constants.get_user().toLowerCase()))
        {
            controller.mark_keywords(constants.get_keyword_color());
            return;
        }
        //Check for occurence of keywords
        String[] keywords = constants.get_keywords();
        for(String keyword:keywords)
        {
            if(message.toLowerCase().contains(keyword.toLowerCase()))
            {
                controller.mark_keywords(constants.get_keyword_color());
                return;
            }
        }


    }

    //Adds the number of buttons specified in misc_util
    private void add_buttons()
    {
        int button_amount = constants.get_buttoncount();
        buttons = new Button[button_amount];
        for(int i = 0;i<button_amount;i++)
        {
            buttons[i] = new Button();
            buttons[i].getStyleClass().addAll("buttons","button"+i);
            final int j = i;
            buttons[i].setOnAction(event -> handlebuttonpress(j));
        }
        for(int i=0;i<button_amount;i++)
        {
            controller.add_header(buttons[i]);
        }
    }

    //Contains the logic to handle a press on a banbutton, id is the button pressed
    private void handlebuttonpress(int id)
    {
        if(cw == null)return;
        if(id ==0)
        {
            cw.get_channel_handler().ban_user(user);
        }
        else
        {
            cw.get_channel_handler().timeout_user(user,constants.get_timeoutduration()[id-1]);
        }

    }

    //Adds the badges of the user
    private void add_badges(String badges)
    {
        if(badges.equals("NULL"))return;
        String[] badges_separated = badges.split(",");
        for(String b:badges_separated)
        {
            if(constants.get_chatbox_type())
            {
                controller.add_chatbox(imh.request_badge(b));
            }
            else
            {
                controller.add_header(imh.request_badge(b));
            }
        }


    }

    //Adds the name of the user, acts as an adapter to the controller class
    private void add_username(String user, String color)
    {
        Text t = new Text(user+": ");
        controller.add_header(t,color,constants.get_chatbox_type());
    }

    //Displays the message. First it is broken up into Emotes and text and then forwarded to the controller
    private void add_message(String message,boolean cheers)
    {
        boolean colored = false;

        //Check if message is colored
        if(message.length()>7&&message.substring(0,7).matches("\\0001ACTION"))
        {
            colored = true;
            message = message.substring(8);
        }

        String[] splitted_string = message.split("(?= )");
        for(String s:splitted_string)
        {
            //Check if part is emote
            if(s.length()>8&&(s.substring(0,7).equals("[EMOTE]")||s.substring(0,8).equals(" [EMOTE]")))
            {
                s = s.replaceAll(" ","").replaceAll("\\0001","");
                s = s.substring(7);
                String tooltip = s.substring(s.indexOf(")")+1);
                controller.add_chatbox(imh.request_emote(s),tooltip);
            }
            else if(cheers&&s.matches("[\\s|\\0001]?cheer\\d+[\\s|\\0001]?"))
            {
                s = s.substring(s.indexOf('r')+1);
                controller.add_chatbox(new ImageView(imh.request_cheeremote(Integer.parseInt(s),true)));
                controller.add_chatbox(new Text(s));
            }
            else if(s.length()<10)
            {
                Text t = new Text(s);
                if(colored) {
                    controller.add_chatbox_colored(t);
                }
                else
                {
                    controller.add_chatbox(t);
                }
            }else if(s.length()>=10)
            {
                int j = 0;
                for(int i=1;10*i<s.length()-1;i++)
                {

                    j = i;
                    String sub = s.substring((i-1)*10,i*10);
                    Text te = new Text(sub);
                    if(colored) {
                        controller.add_chatbox_colored(te);
                    }
                    else
                    {
                        controller.add_chatbox(te);
                    }
                }
                if(j*10<s.length()-1)
                {
                    String sub = s.substring(j*10);
                    Text te = new Text(sub);
                    if(colored) {
                        controller.add_chatbox_colored(te);
                    }
                    else
                    {
                        controller.add_chatbox(te);
                    }
                }

            }
        }
    }
}
