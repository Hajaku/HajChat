package channel_logic;

import channel_logic.misc_util.Constants;
import channel_logic.irc_connection_and_parsers.Command_handler;
import channel_logic.irc_connection_and_parsers.IRC_channel_handler;
import channel_logic.irc_connection_and_parsers.String_handler;
import chat_interface.chat_window.Chat_window;

import java.text.SimpleDateFormat;

/**
 * Created by Leander on 07.08.2016.
 * Created to handle an individual channel, class creates the irc handler which connects to the servers and
 * the string handler which parses the received messages
 */
public class Channel_handler extends Thread{
    private String channel = "";
    private IRC_channel_handler irc_reader;
    private IRC_channel_handler irc_writer;
    private boolean irc_is_started = false;
    private String_handler string_handler;
    private Chat_window cw;
    private Constants con;

    Channel_handler(Constants c, Chat_window cw)
    {
        this.con = c;
        this.channel += ("#"+c.get_channel());
        string_handler = new String_handler(cw);
        this.cw = cw;
    }

    //shuts down the irc_writer and irc_reader
    void shutdown_irc_handlers()
    {
        irc_writer.shutdown();
        irc_reader.shutdown();
    }


    public void run()
    {
        irc_reader = new IRC_channel_handler(string_handler,this.channel,con,false);
        irc_reader.start();
        irc_writer = new IRC_channel_handler(new Command_handler(cw),this.channel,con,true);
        irc_writer.start();
        irc_is_started = true;
    }

    //Sends a timeout command to the server.
    public void timeout_user(String user, int time)
    {
        if(time <0 ) {time = 1;}//Basically a purge
        if(time > 1209600) {time = 1209600;}//Check if timeout duration exceeds max duration
        irc_writer.write_string(".timeout "+user+" "+time+" Click on ban-button");
    }

    public IRC_channel_handler get_irc_handler()
    {
        while(!irc_is_started){}
        return irc_writer;
    }

    //Permabans a user
    public void ban_user(String user)
    {
        irc_writer.write_string(".ban "+user+" Click on ban-button");
    }

    //set a slowmode
    public void set_slow(int slowduration)
    {
        irc_writer.write_string(".slow "+slowduration);
    }

    //Posts a timestamp every 5 second into the chat
    private void test_messages()
    {
       while(true)
       {
           try
           {
               Thread.sleep(5000);
               irc_writer.write_string("TEST+"+ new SimpleDateFormat("HH.mm.ss").format(new java.util.Date()));
           }catch (Exception e){e.printStackTrace();}
       }
    }

}
