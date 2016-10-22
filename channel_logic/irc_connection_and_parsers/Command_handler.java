package channel_logic.irc_connection_and_parsers;

import channel_logic.irc_connection_and_parsers.String_handler;
import chat_interface.chat_window.Chat_window;

/**
 * Created by Leander on 11.10.2016.
 * Class which extends String_handler, used to handle the string received by the logged in irc_handler
 * Is only used in case a user entered command returns a message from the server
 */
public class Command_handler extends String_handler {
    private Chat_window cw;

    public Command_handler(Chat_window cw)
    {
        this.cw = cw;
    }

    //Parser used to parse returned commands,format of STRING ARRAY: MSGTYPE-USER-BADGES-COLOR-MSG-TIME
    void handle_line(String line)
    {
        if(line.matches("^@msg-id=room_mods.*$")) {
            String[] parsed_input = {"NULL", "Null", "NULL", "NULL", "NULL", "NULL"};
            parsed_input[0] = "MODS";
            parsed_input[4] = line.substring(line.indexOf(":The")+1);
            cw.add_message(parsed_input);

        }
    }


}
