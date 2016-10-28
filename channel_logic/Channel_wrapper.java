package channel_logic;

import channel_logic.misc_util.Constants;
import chat_interface.chat_window.Chat_window;
import chat_interface.chat_window.Chat_window_thread_wrapper;
import javafx.scene.layout.VBox;
import main_interface.login.Login_information;

import java.util.logging.Logger;

/**
 * Created by Leander on 06.09.2016.
 * Handles the initialization and connection process for a chat window, creates the chatwindow wrapper and channel handler
 */
public class Channel_wrapper {
    private final static Logger LOGGER = Logger.getGlobal();

    private Chat_window cw;
    private channel_logic.Channel_handler ch;

    //Creates irc handlers and a channel window to be used to display the chat of a channel
    public Channel_wrapper(String channel, Login_information login)
    {
        try {
            Constants c = new Constants(channel, login.get_user(), login.get_pw());
            Chat_window_thread_wrapper cwtw = new Chat_window_thread_wrapper(c);
            cwtw.start();
            System.out.println("User Interface started");
            Chat_window cw = cwtw.get_chat_window();
            if(cw==null)return;
            System.out.println("User Interface Reference obtained");
            ch = new channel_logic.Channel_handler(c, cw);
            ch.start();
            System.out.println("Channel logic initialized");
            cw.pass_channel_handler(ch);
            System.out.println("Channel setup finished.");

            this.cw = cw;
        }catch (Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    //returns a reference to the actual chatwindow, passes request to the chat_window
    public VBox get_chatwindow()
    {
        return cw.get_chatwindow();
    }

    //shuts down everything related to this chat connection
    public void shutdown()
    {
        ch.shutdown_irc_handlers();
    }


}
