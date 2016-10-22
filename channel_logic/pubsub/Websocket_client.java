package channel_logic.pubsub;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketFactory;

import java.net.URI;
import java.util.logging.Logger;

/**
 * Created by Leander on 14.10.2016.
 * Client used to connect to websockets, used for connecting to the Twitch pubsub system
 * uses websocket library nv-websocket-client
 */
class Websocket_client {
    private final static Logger LOGGER = Logger.getGlobal();

    private WebSocket socket;
    private Pub_sub_connection pub_sub_connection;

    //Constructor, tries to connect to the websocket given as string, forwards received messages to the pub_sub_connection
    Websocket_client(String websocket, Pub_sub_connection pub_sub_connection)
    {
        try
        {
            this.pub_sub_connection = pub_sub_connection;
            URI uri = new URI(websocket);
            socket = new WebSocketFactory().createSocket(uri).addListener(new WebSocketAdapter(){
                @Override
                public void onTextMessage(WebSocket ws,String message)
                {
                    pub_sub_connection.handle_message(message);
                }
            }).connect();
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    //closes the user_session, used to shutdown websocket in case of reconnect
    void close()
    {
        try {
            socket.disconnect();
        }catch (Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }


    //Sends a message to the websocket
    void send_message(String message)
    {
        if(socket!=null)
        {
            socket.sendText(message);
        }
    }
}
