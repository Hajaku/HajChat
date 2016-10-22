package chat_interface.chat_window;

import channel_logic.misc_util.Constants;

/**
 * Created by Leander on 19.08.2016.
 * Wrapper for the chatwindow to enable multithreading, initializes a chatwindow inside of a new thread
 */
public class Chat_window_thread_wrapper extends Thread {

    private Chat_window cw = null;
    private Constants c;
    private boolean initialized = false;

    public Chat_window_thread_wrapper(Constants c)
    {
        this.c = c;
    }

    //Run method for the thread, launches the user interface
    public void run()
    {
        cw = new Chat_window(c);
        synchronized (this) {
            initialized = true;
        }
    }

    //Returns a reference to the started application, waits till applications starts.
    public Chat_window get_chat_window()
    {
        while(true){
            synchronized (this)
            {
                if (initialized) {break;}
            }
        }
        return cw;
    }
}
