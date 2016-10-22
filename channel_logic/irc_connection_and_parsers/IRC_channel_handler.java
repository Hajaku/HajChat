package channel_logic.irc_connection_and_parsers;

import channel_logic.misc_util.Constants;

import java.io.*;
import java.net.*;
import java.util.logging.Logger;

/**
 * Created by Leander on 06.09.2016.
 * Connects to the irc servers and passes received lines to the string handler
 */

public class IRC_channel_handler extends Thread{
    private final static Logger LOGGER = Logger.getGlobal();

    private String_handler string_handler;
    private String channel;
    private boolean running = true;
    private boolean show_information = true;
    private BufferedWriter output_writer;
    private Constants c;
    private boolean login;


    public IRC_channel_handler(String_handler string_handler, String channel,Constants c, boolean login)
    {
        this.c = c;
        this.string_handler = string_handler;
        this.channel = channel.toLowerCase();
        this.login = login;
    }

    public void run()
    {
        try{read_lines();}catch (Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }


    private void read_lines() throws Exception {

        // The server to connect to and our details.
        String server = "irc.chat.twitch.tv";

        // Connect directly to the IRC server.
        Socket socket1 = new Socket(server,6667);

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(socket1.getOutputStream( ),"UTF8"));
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket1.getInputStream( ),"UTF8"));


        output_writer = writer;
        // Log on to the server.
        if(login) {
            writer.write("PASS " + c.get_pw() + "\r\n");
            writer.write("NICK " + c.get_user() + "\r\n");
        }
        else
        {
            writer.write("PASS password\r\n");
            writer.write("NICK justinfan1244534523423423442\r\n");
        }

        writer.write("CAP REQ :twitch.tv/commands\r\n");
        if(show_information) {
            writer.write("CAP REQ :twitch.tv/membership\r\n");
            writer.write("CAP REQ :twitch.tv/tags\r\n");
        }
        writer.flush();

        // Read lines from the server until it tells us we have connected.
        String line = null;
        while ((line = reader.readLine( )) != null) {
            if (line.contains("004")) {
                // We are now logged in.
                break;
            }
            else if (line.contains("433")) {
                string_handler.handle_line("Nickname already in use!");
                return;
            }
        }

        // Joining the channel
        writer.write("JOIN " + channel + "\r\n");
        writer.flush( );




        // Perpetual reading of lines from the server
        while (((line = reader.readLine( )) != null)&&running) {
            if (line.matches("PING :tmi.twitch.tv")) {
                // response to ping so that the server does not close the connection
                output_writer.write("PONG :tmi.twitch.tv\r\n");
                output_writer.flush();
            }
            else {
                // Forward the line to the string handler
                if(string_handler!=null){
                    string_handler.handle_line(line);
                }
            }
        }
    }

    public void shutdown()
    {
        running = false;
    }


    public void write_string(String output)
    {
        if(output_writer==null)throw new RuntimeException("Writer not existent!");
        try{
            System.out.print("PRIVMSG "+channel+" :"+output+"\r\n");
            output_writer.write("PRIVMSG "+channel+" :"+output+"\r\n");
            output_writer.flush();
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}

    }


}
