package chat_interface.chat_window;

import chat_interface.chatbox.Chatbox;
import javafx.scene.Node;

/**
 * Created by Leander on 06.10.2016.
 * Class used as a wrapper object for all given types of objects added to the chatwindow
 */
class Chatelement_wrapper {
    private Chatbox chat = null;
    private Node node;

    //Constructor used for creation of wrapper with a chatbox as content
    Chatelement_wrapper(Chatbox chat) {
        this.chat = chat;
    }

    //Constructor used in case the chatelement is not a chatbox, but rather a generic node
    Chatelement_wrapper(Node n) {
        this.node = n;
    }

    //In case a chatbox is set it returns the node from the chatbox, otherwise the node field of this class instance
    Node get_node() {
        if (chat != null) return chat.get_node();
        return node;
    }

    //Returns null if chatbox is not set, otherwise clears the chatbox and returns this wrapper
    Chatelement_wrapper clear_and_get_wrapper() {
        if (chat == null) return null;
        chat.reset_chatbox();
        return this;
    }

    //Direct access to the chatbox
    Chatbox get_chatbox_direct()
    {
        if(chat==null)throw new RuntimeException("Chatboxfield is null!");
        return chat;
    }

    //Can be used to check if chatbox !=null
    boolean chatbox_set()
    {
        return chat!=null;
    }


}
