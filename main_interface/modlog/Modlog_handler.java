package main_interface.modlog;

import channel_logic.pubsub.Pub_sub_connection;
import main_interface.login.Login_information;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Leander on 15.10.2016.
 * Class which handles all of the Modlogs,
 */
public class Modlog_handler {

    private Pub_sub_connection psc = null;
    private Map<String,Modlog> modlogs_map = new HashMap<>();
    private Login_information login;

    public Modlog_handler(Login_information login)
    {
        this.login = login;
    }

    //Adds a channel to the modlogs, first checks if channel already exists
    public void add_channel(String channel)
    {
        if(modlogs_map.containsKey(channel))return;
        Modlog modlog = new Modlog(channel);
        modlogs_map.put(channel,modlog);
        if(psc!=null)
        {
            psc.listen_channel(channel);
        }
        else
        {
            psc = new Pub_sub_connection(login,channel,this);
            psc.start();
        }
    }

    //displays the modlogs for a specific channel
    public void display_modlog(String channel)
    {
        if(!modlogs_map.containsKey(channel))return;
        modlogs_map.get(channel).show_modlog();
    }

    //Called from Pub_sub_connection, handles a received line
    //Format: [CHANNEL,TYPE,USER,TARGET,TIME,COMMENT]
    public void handle_line(String[] line)
    {
        if(line.length!=6||line[0].equals("NULL"))return;
        //Check in case of ban or timeout if a ban or timeout has happened in the last 5 seconds
        if(line[1].equals("ban")||line[1].equals("timeout"))
        {
            if(check_if_ban_expired(line[3],line[0]))return;
            ban_wrappers.add(new Ban_wrapper(line[3],line[0]));
        }

        String displayed_text = generate_displayed_text(line);
        System.out.println(displayed_text);
        modlogs_map.get(line[0]).add_line(displayed_text);
    }


    private ArrayList<Ban_wrapper> ban_wrappers = new ArrayList<>();
    //Checks in case of timeout or ban if a message regarding that person has been added in the last 5 seconds
    private boolean check_if_ban_expired(String name, String channel)
    {
        update_ban_wrappers();
        for(Ban_wrapper b:ban_wrappers)
        {
            if(b.check_values(name,channel))return true;
        }
        return false;
    }

    //updates the banwrapper list, removes all entries older than 5 seconds
    private void update_ban_wrappers()
    {
        for(int i = 0;i<ban_wrappers.size();i++)
        {
            if(ban_wrappers.get(i).check_expired_time(5))
            {
                ban_wrappers.remove(i);
                --i;
            }
        }
    }

    //Generates the text to be displayed for each specific type of action
    //Format: [CHANNEL,TYPE,USER,TARGET,TIME,COMMENT]
    private String generate_displayed_text(String[] line)
    {
        String t = "";
        switch (line[1])
        {
            case "timeout":
                t = line[2]+ " timed out "+ line[3] + " for "+ line[4]+" seconds.";
                if(!line[5].equals("NULL")) t = t+"\n"+"Reason: "+line[5];
                return t;

            case "untimeout":
                t = line[2] + " untimeouted "+line[3]+".";
                return t;

            case "ban":
                t = line[2] + " banned "+line[3]+".";
                if(!line[4].equals("NULL")) t = t+"\n"+"Reason: "+line[4];
                return t;

            case "unban":
                t = line[2] + " unbanned "+line[3]+".";
                return t;

            case "slow":
                t = line[2] + " set slow to "+line[3]+".";
                return t;
            case "slowoff":
                t = line[2] + " turned slow off.";
                return t;

            case "emoteonly":
                t = line[2] + " enabled emoteonly.";
                return t;

            case "emoteonlyoff":
                t = line[2] + " turned off emoteonly.";
                return t;

            case "r9kbeta":
                t = line[2] + " turned on r9k.";
                return t;

            case "r9kbetaoff":
                t = line[2] + " turned off r9k.";
                return t;
            case "clear":
                t = line[2] + " cleared the chat.";
                break;
        }
        return t;
    }

    //Private class which contains the name, channel and time of a timeout, used to prevent ban message spam by bots
    private class Ban_wrapper
    {
        private long timeouttime;
        private String name;
        private String channel;

        Ban_wrapper(String name, String channel)
        {
            this.name = name;
            this.channel = channel;
            timeouttime = System.currentTimeMillis();
        }

        //Checks if the timeout has happened more than time_difference_seconds ago
        boolean check_expired_time(int time_difference_seconds)
        {
            long current_time = System.currentTimeMillis();
            return (current_time-timeouttime)>time_difference_seconds*1000;
        }

        //Checks if both name and channel match the given name and channel
        boolean check_values(String name, String channel)
        {
            return this.name.equals(name)&&this.channel.equals(channel);

        }

    }
}
