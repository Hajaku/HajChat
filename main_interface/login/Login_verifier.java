package main_interface.login;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * Created by Leander on 09.09.2016.
 * Verifies login information by establishing a connection to the chat server and test connecting
 */
class Login_verifier {
    private final static Logger LOGGER = Logger.getGlobal();

    boolean verify_login(String user, String pw)
    {
        try {
            if(user.matches("^justinfan(\\d+)?$")||(user.equals("")&&pw.equals("")))return true;
            if(pw.length()<6)return false;
            if(!pw.substring(0,6).equals("oauth:"))pw = "oauth:"+pw;

            Socket test_connection = new Socket("irc.twitch.tv", 6667);
            BufferedWriter test_writer = new BufferedWriter(new OutputStreamWriter(test_connection.getOutputStream()));
            BufferedReader test_reader = new BufferedReader(new InputStreamReader(test_connection.getInputStream()));
            test_writer.write("PASS " + pw + "\r\n");
            test_writer.write("NICK " + user + "\r\n");
            test_writer.flush();

            String line = null;
            while ((line = test_reader.readLine( )) != null) {
                if (line.contains("004")) {
                    return true;
                }
                else if (line.contains("authentication failed")) {
                    return false;
                }
            }
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
        return false;
    }
}
