package channel_logic.pubsub;

import com.google.gson.Gson;
import main_interface.login.Login_information;
import main_interface.modlog.Modlog_handler;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

/**
 * Created by Leander on 11.10.2016.
 * Class used to connect to the Twitch pubsub system, handles listening to new channels and parses json and forwards
 * to the Modlog_handler
 */
public class Pub_sub_connection extends Thread{
    private final static Logger LOGGER = Logger.getGlobal();

    private Login_information login ;
    private Modlog_handler modlog_handler;
    private String user_id;
    private ArrayList<String> channels = new ArrayList<>();
    private ArrayList<Name_wrapper> name_wrappers = new ArrayList<>();
    private boolean pinged = false;
    private boolean initialized = false;

    private Websocket_client client;


    //Constructor for the Pub_sub_connection
    public Pub_sub_connection(Login_information login,String initial_channel, Modlog_handler modlog_handler)
    {
        this.modlog_handler = modlog_handler;
        this.login = login;
        channels.add(initial_channel);
        user_id = retrieve_channelid_from_net(login.get_user());
    }

    //Tries to call establish connection,if the connection fails a reconnect is tried with an exponentially
    // increasing duration, returns if duration > 64
    public void run()
    {
        int initial_delay = 0;
        while(initial_delay<64)
        {
            if(establish_connection(initial_delay))
            {
                initialized = true;
                break;
            }
            if(initial_delay==0)++initial_delay;
            initial_delay *= 2;
        }
    }

    //establishes a connection to the pubsub websocket server,sends listen commands to all current channels
    //also starts the pinger which keeps up the connection
    private boolean establish_connection(int delay_in_seconds)
    {
        try {
            Thread.sleep(delay_in_seconds*1000);
            synchronized (Websocket_client.class) {
                client = new Websocket_client("wss://pubsub-edge.twitch.tv/", this);

                for (String channel : channels) {
                    client.send_message(generate_listen_json(channel));
                }
            }
            Pinger p = new Pinger();
            p.start();
            return true;

        }catch (Exception e)
        {
            e.printStackTrace();
            LOGGER.info(e.getMessage()+" \nTrying to reconnect!.");
            return false;
        }
    }


    //Adds another channel to the list of channels to be listened to, also sends listening message
    public void listen_channel(String channel)
    {
        //Ensure that client is actually initialized
        try {
            synchronized (this) {
                synchronized (Websocket_client.class)
                {
                    if (!channels.contains(channel)) {
                        channels.add(channel);
                        if(client!=null)client.send_message(generate_listen_json(channel));
                     }
                }
            }

        }catch (Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    //Reconnects to all of the channels, called on reconnect or not returned ping
    private void reconnect()
    {
        synchronized (this) {
            client.close();
            establish_connection(0);
        }
    }

    //generates the json which is send to the server to listen to a specific channel
    private String generate_listen_json(String channel)
    {
        String channelid = get_channelid(channel);

        String nonce = ""+new Random().nextLong();
        String oauth = login.get_pw();
        oauth = oauth.substring(6);
        return "{\"type\": \"LISTEN\",\"nonce\": \""+nonce+"\",\"data\": {\"topics\":" +
                " [\"chat_moderator_actions."+user_id+"."+channelid+"\"],\"auth_token\": \""+oauth+"\"}}";
    }

    //Returns the channel id for a given channel name, first checks name wrapper list, if not found,
    //calls method to retrieve id from api
    private String get_channelid(String name)
    {
        for(Name_wrapper nw:name_wrappers)
        {
            if(nw.matches_name(name))return nw.get_number();
        }
        return retrieve_channelid_from_net(name);
    }

    //Returns the channel name for a given channel id, searches the name wrapper list
    private String get_name(String channelid)
    {
        for(Name_wrapper nw:name_wrappers)
        {
            if(nw.matches_number(channelid))return nw.get_name();
        }
        return "";
    }

    //Retrieves the channel id of a given channel from the twitch kraken api and adds a new wrapper containing both to
    //the name wrapper list
    private String retrieve_channelid_from_net(String channel)
    {
        String id = "";
        try {
            URL url = new URL("https://api.twitch.tv/kraken/users/"+channel+"/?client_id=ib3fwre0zolczpxga19t74a6qc3qjxo");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String s = br.readLine();
            id = s.substring(s.indexOf("_id\":")+5,s.indexOf(",\"name\""));

            //Add new namewrapper
            name_wrappers.add(new Name_wrapper(channel,id));
            br.close();
        }catch (Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
        return id;
    }



    //Called by pinger, send a ping to the server
    private void ping()
    {
        pinged = false;
        if(client != null){client.send_message("{\"type\": \"PING\"}");}
    }

    //Called by pinger, checks if a pong has been received, if not, reconnects
    private void check_pong()
    {
        if(!pinged){reconnect();}
    }

    //Handles the messages received by the websocket, first checks for pings or reconnect messages
    void handle_message(String message)
    {


        try {
            Gson g = new Gson();
            Map jsonobject = g.fromJson(message, Map.class);
            if (jsonobject.containsKey("type")) {
                String type = (String) jsonobject.get("type");
                if(type.equals("PONG"))
                {
                    pinged = true;
                    return;
                }
                if(type.equals("RECONNECT"))
                {
                    reconnect();
                    return;
                }

                if (type.equals("MESSAGE"))
                {
                    parse_message(jsonobject,g);
                }
            }
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    //parses a message and extracts the information, forwards to modlog_handler
    private void parse_message(Map jsonobject, Gson g)
    {
        //Format: [CHANNEL,TYPE,USER,TARGET,TIME,COMMENT]
        String[] moderator_action = {"NULL","NULL","NULL","NULL","NULL","NULL"};


        Map data = (Map) jsonobject.get("data");
        String topic = (String) data.get("topic");
        Map msg_map = (Map) g.fromJson((String) data.get("message"), Map.class).get("data");
        String moderation_action = (String) msg_map.get("moderation_action");
        ArrayList<String> args = ((ArrayList<String>) msg_map.get("args"));
        String author = (String) msg_map.get("created_by");

        //Write results into the result string array
        moderator_action[0] = get_name(topic.substring(topic.lastIndexOf(".")+1));
        moderator_action[1] = moderation_action;
        moderator_action[2] = author;
        if(args!=null){
            for(int i=0;i<args.size();i++)
            {
                moderator_action[3+i] = args.get(i);
            }
        }

        modlog_handler.handle_line(moderator_action);
    }


    //Class used to keep up the connection with the websocket, calls ping in 4 minute intervals
    private class Pinger extends Thread
    {

        boolean running = true;

        //Called to start Thread, calls the ping method in the main class every ~4 minutes (random jitter added)
        public void run()
        {
            Random r = new Random();
            while(running)
            {
                try
                {
                    int jitter = r.nextInt(250);
                    //Pause thread for 4 minutes + random jitter, then ping
                    Thread.sleep(240*1000+jitter);
                    ping();
                    //Pause thread for 10 seconds, then check for pong
                    Thread.sleep(10*1000);
                    check_pong();
                }catch (Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
            }
        }

        //shuts down the pinger
        void shutdown()
        {
            running = false;
        }
    }

    //Class used to match names to channel ids and vice versa
    private class Name_wrapper
    {
        private String name;
        private String number;

        Name_wrapper(String name,String number)
        {
            this.name = name;
            this.number = number;
        }

        boolean matches_name(String name){return this.name.equals(name);}

        boolean matches_number(String number){return this.number.equals(number);}

        String get_number(){return number;}
        String get_name(){return name;}
    }
}
