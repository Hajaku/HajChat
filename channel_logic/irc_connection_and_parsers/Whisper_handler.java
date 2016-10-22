package channel_logic.irc_connection_and_parsers;

import main_interface.Main_window_controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by Leander on 15.09.2016.
 * Connects to group chat server and parses whisper messages received by the server, passes these to the main interface
 */
public class Whisper_handler extends Thread {
    private final static Logger LOGGER = Logger.getGlobal();

    private Main_window_controller mwc;
    private String user;
    private String pw;
    private String_emote_handler string_emote_handler = new String_emote_handler();
    private BufferedWriter output_writer;

    public Whisper_handler(String user, String pw, Main_window_controller mwc)
    {
        this.user = user;
        this.pw = pw;
        this.mwc = mwc;
    }

    //Used to start the actual whisper thread, starts reading whispers and passing them to the user_interface
    public void run()
    {
        read_whispers();
    }


    //read whispers
    private void read_whispers()
    {
        try {
            String server = "irc.chat.twitch.tv";

            Socket socket = new Socket(server, 6667);

            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(socket.getOutputStream( ),"UTF8"));
            output_writer = writer;
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(socket.getInputStream( ),"UTF8"));
            //Login
            writer.write("PASS " + pw + "\r\n");
            writer.write("NICK " + user + "\r\n");
            writer.flush();
            //Request caps
            writer.write("CAP REQ :twitch.tv/commands\r\n");
            writer.write("CAP REQ :twitch.tv/membership\r\n");
            writer.write("CAP REQ :twitch.tv/tags\r\n");
            writer.flush();
            //Wait for login
            String line = null;
            while ((line = reader.readLine( )) != null) {
                if (line.contains("004")) {
                    // We are now logged in.
                    break;
                }
                else if (line.contains("433")) {
                    throw new RuntimeException("Whisper authentification failed!");
                }
            }

            //join the channel of the user to be able to send whispers

            while (((line = reader.readLine( )) != null)) {
                if (line.matches("PING :tmi.twitch.tv")) {
                    // response to pings
                    writer.write("PONG :tmi.twitch.tv\r\n");
                    writer.flush();
                }
                else {
                    mwc.add_whisper(parse_string(line));
                }
            }

        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    //parses the raw line received from a whisper
    private String[] parse_string(String input)
    {
        //String[] format: USER-COLOR-MESSAGE
        String[] parsed_message = {"NULL","NULL","NULL"};
        if(input !=null&&!input.contains("WHISPER"))return parsed_message;
        try {
            if (input != null && input.length() > 0 && input.matches("^@badges.*$")) {
                String[] split_input = input.split(";");
                //determine color of the user
                String[] split_color = split_input[1].split("=");
                parsed_message[1] = (split_color.length > 1) ? split_color[1] : "NULL";
                //determine actual message
                String input_tags_removed = input.substring(input.indexOf("user-type="));
                parsed_message[0] = input_tags_removed.substring(input_tags_removed.indexOf(":")+1, input_tags_removed.indexOf("!"));
                parsed_message[2] = input_tags_removed.substring(input_tags_removed.indexOf("WHISPER"));
                parsed_message[2] = parsed_message[2].substring(parsed_message[2].indexOf(":")+1);
                //add emotecodes to the parsed message
                String[] emote_split = split_input[3].split("=");
                String emotecodes = (emote_split.length > 1) ? emote_split[1] : "";
                parsed_message[2] = string_emote_handler.parse_emotes(parsed_message[2],emotecodes);
            }
        }catch (Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
        return parsed_message;
    }

    //write a whisper to the corresponding channel
    public void write_whisper(String user,String message)
    {
        try {
            output_writer.write("PRIVMSG #jtv :.w "+user+" "+message+"\r\n");
            output_writer.flush();

        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }


}
