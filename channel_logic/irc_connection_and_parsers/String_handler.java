package channel_logic.irc_connection_and_parsers;

import chat_interface.chat_window.Chat_window;

import java.util.logging.Logger;

/**
 * Created by Leander on 23.05.2016.
 * Parses all the chatmessages received by the irc_channel_handler, returns result as an array
 * */
public class String_handler {
    private final static Logger LOGGER = Logger.getGlobal();


    private Chat_window cw;
    private String_emote_handler string_emote_handler = new String_emote_handler();

    void handle_line(String line)
   {
       String[] parsed_input = parse_complicated_input(line);

       if(cw == null)
       {
           System.out.println(line);
           print_messages(parsed_input);
       }
       else
       {
           cw.add_message(parsed_input);
       }
   }

    public String_handler(Chat_window cw)
    {
        this.cw = cw;
    }

    public String_handler(){}

    //Complete parser, returns STRING ARRAY: MSGTYPE-USER-BADGES-COLOR-MSG-TIME
    //MSG AND TIME are used for diff. purposes in ban & slow & normal
    private String[] parse_complicated_input(String input)
    {
        String[] parsed_input = {"NULL", "Null", "NULL", "NULL", "NULL", "NULL"};
        try {

            if (input.length() == 0) {
                throw new RuntimeException("Input length zero!");
            }
            if (input.charAt(0) == ':') {
                //Check if initial subscription message
                if(input.contains("subscribed"))
                {
                    parsed_input[0] = "SUB";
                    parsed_input[5] = input.substring(input.indexOf(" :")+2);
                }



            } else if (input.charAt(0) == '@') {
                if (input.matches("^@badges.*$")) {
                    String[] status = input.split(";");

                    int bit = 0;
                    //If message is subscription message
                    String tags_removed = input.substring(input.indexOf("tmi.twitch.tv"));
                    if(tags_removed.substring(0,tags_removed.indexOf("#")).contains("USERNOTICE")) {

                        parsed_input[0] = "SUB";                                      //MSGTYPE

                        String[] split_name = status[2+bit].split("=");                 //User, check if display name is given
                        parsed_input[1] = (split_name.length>1)?split_name[1]:"NULL";
                        if(parsed_input[1].equals("NULL")) {
                            String input_tags_removed = input.substring(input.indexOf("login"));       //User, if display name is empty
                            parsed_input[1] = input_tags_removed.substring(input_tags_removed.indexOf("=") + 1, input_tags_removed.indexOf(";"));
                        }
                        String[] badges = status[0].split("=");
                        parsed_input[2] = (badges.length > 1) ? badges[1] : "NULL";   //BADGES
                        String[] color = status[1].split("=");
                        parsed_input[3] = (color.length > 1) ? color[1] : "NULL";     //COLOR

                        parsed_input[5] = status[11].split("=")[1].replaceAll("\\\\s", " "); //TIME (in this case used for resub noti.)

                        String message_uncleaned = tags_removed.substring(tags_removed.indexOf("USERNOTICE"));
                        parsed_input[4] = message_uncleaned.contains(":")?message_uncleaned.substring(message_uncleaned.indexOf(":")+1):"NULL";

                    }
                    else //If message is normal message
                    {
                        if (status[1].contains("bits")) {
                            parsed_input[0] = "CHEERMSG";
                            bit = 1;
                        } else {
                            parsed_input[0] = "PRVMSG";                             //MSGTYPE
                        }

                        String[] split_name = status[2+bit].split("=");                 //User, check if display name is given
                        parsed_input[1] = (split_name.length>1)?split_name[1]:"NULL";
                        if(parsed_input[1].equals("NULL")) {
                            String input_tags_removed = input.substring(input.indexOf("user-type"));       //User, if display name is empty
                            parsed_input[1] = input_tags_removed.substring(input_tags_removed.indexOf(":") + 1, input_tags_removed.indexOf("!"));
                        }
                        String[] badges = status[0].split("=");
                        parsed_input[2] = (badges.length > 1) ? badges[1] : "NULL";   //BADGES
                        String[] color = status[1 + bit].split("=");
                        parsed_input[3] = (color.length > 1) ? color[1] : "NULL";     //COLOR

                        String message = input.substring(input.indexOf("PRIVMSG")); //MESSAGE
                        message = message.substring(message.indexOf(":") + 1);
                        parsed_input[4] = message;
                        //Special logic for emotes
                        String[] emote_split = status[3 + bit].split("=");
                        String emotecodes = (emote_split.length > 1) ? emote_split[1] : "";
                        if (!emotecodes.equals("")) {
                            parsed_input[4] = string_emote_handler.parse_emotes(parsed_input[4], emotecodes);
                        }

                        //END OF EMOTE LOGIC
                    }

                } else if (input.matches("^@slow.*$")) {

                    String[] splitted_input = input.split(":");

                    parsed_input[0] = "SLOW";                           //MSGTYPE
                    parsed_input[5] = splitted_input[0].split("=")[1];  //DURATION
                } else if (input.matches("^@ban.*$")) {
                    String[] splitted_input = input.split(":");
                    parsed_input[0] = "BAN";                            //MSGTYPE
                    parsed_input[1] = splitted_input[2];                //USER
                    if (splitted_input[0].contains(";")) {
                        String[] status = splitted_input[0].split(";");
                        parsed_input[4] = status[1].split("=")[1];      //MSG (BAN REASON)
                        parsed_input[5] = status[0].split("=")[1];      //TIME
                    } else {
                        parsed_input[4] = splitted_input[0].split("=")[1];//MSG (BAN REASON)
                    }

                }
                //check for subscribersonly mode
                else if (input.matches("^@subs-only.*$")) {
                    parsed_input[0] = "STATUSUPDATE";
                    parsed_input[4] = "sub=" + input.split("=")[1].charAt(0);
                }
                //Check for emoteonly
                else if (input.matches("^@emote-only.*$")) {
                    parsed_input[0] = "STATUSUPDATE";
                    parsed_input[4] = "emote=" + input.split("=")[1].charAt(0);
                } else if (input.matches("^@r9k.*$")) {
                    parsed_input[0] = "STATUSUPDATE";
                    parsed_input[4] = "r9k=" + input.split("=")[1].charAt(0);
                }
                //match the initial chatstate message.
                else if (input.matches("^@broadcaster-lang.*$")) {
                    String[] splitted_input = input.split(";");
                    if(splitted_input.length>=4) {
                        parsed_input[0] = "CHATSTATE";
                        parsed_input[4] = "";
                        parsed_input[4] += splitted_input[4].split("=")[1].charAt(0); //subonly
                        parsed_input[4] += splitted_input[1].split("=")[1]; //emoteonly
                        parsed_input[4] += splitted_input[2].split("=")[1]; //r9k
                        parsed_input[4] += splitted_input[3].split("=")[1]; //slow
                    }
                }

            }
        }catch(Exception e){e.printStackTrace();System.out.println(input);LOGGER.info(e.getMessage());}

        return parsed_input;

    }


    //Outprints an array of strings seperated by ~~
    public void print_stringarray(String[] input){for(String s:input){System.out.print("~~"+s);}System.out.println();}

    //Uses the returned String array to print the specific message
    private void print_messages(String[] input)
    {
        switch(input[0])
        {
            case "PRVMSG":
            System.out.println(input[1]+": "+input[4]);
            break;
            case "SLOW":
                System.out.println("Slowmode: "+input[5]);
            break;
            case "BAN":
                if(input[5].equals("NULL"))
                {
                    System.out.println(input[1]+" has been banned.");
                }
                else
                {
                    System.out.println(input[1]+" has been timed out for "+input[5]+" seconds");
                    System.out.println("Reason: "+input[4]);
                }
            break;
        }
    }
}
