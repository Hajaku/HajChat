package channel_logic;

import channel_logic.misc_util.Constants;
import chat_interface.chat_window.Chat_window;
import chat_interface.chat_window.Chat_window_thread_wrapper;

/**
 * Created by Leander on 07.08.2016.
 * testclass which directly connects to the chat servers, used for checking message syntax and testing the main chatwindow
 */


public class main {

    public static void main(String[] args) {
        Constants c = new Constants("hajaku",null,null);

        boolean user_interface = false;

        if(user_interface) {
            Chat_window_thread_wrapper cwtw = new Chat_window_thread_wrapper(c);
            cwtw.start();
            System.out.println("User Interface started");
            Chat_window cw = cwtw.get_chat_window();
            System.out.println("User Interface Reference obtained");
            channel_logic.Channel_handler ch = new channel_logic.Channel_handler(c, cw);
            ch.start();
            System.out.println("Channel logic initialized");
            cw.pass_channel_handler(ch);
            System.out.println("Channel setup finished.");
        }else {
            Channel_handler ch1 = new Channel_handler(c,null);
            ch1.start();
        }

    }

}
