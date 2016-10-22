package channel_logic.misc_util;
import javafx.scene.input.KeyCode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Created by Leander on 23.05.2016.
 * Constants class, contains all the default values, created for each individual chat_window, also initializes
 * an image handler which loads all the badges
 */
public class Constants {
    private final static Logger LOGGER = Logger.getGlobal();

    private String user;
    private String pw;

    private int buttoncount = 3;
    private int[] timeoutduration = {3600,600};//Always one less as buttoncount, as button 0 is permaban
    private String[] keywords = {"banana","BibleThump"};
    private KeyCode keyCode = KeyCode.SPACE;
    private int chatboxcount = 100;
    private String keyword_color = "00ffff";
    private boolean compact_chatbox = true;

    private Image_handler imh;

    private String channel;
    private String channel_id = "";

    public Constants(String channel,String user, String pw){
        this.channel = channel;
        fetch_user_id();
        imh = new Image_handler(channel,channel_id);
        this.user = user;
        this.pw = pw;

        try {
            Preferences savestate = Preferences.userNodeForPackage(Constants.class);

            buttoncount = Integer.parseInt(savestate.get("buttoncount","3"));

            String[] timeoutduration_s = savestate.get("button_times","3600,600").split(",");
            int[] timeoutdurations = new int[timeoutduration_s.length];
            for (int i = 0; i < timeoutduration_s.length; i++)
            {
                timeoutdurations[i] = Integer.parseInt(timeoutduration_s[i]);
            }
            timeoutduration = timeoutdurations;

            keywords = savestate.get("keywords","Banana").split(",");
            keyCode = KeyCode.getKeyCode(savestate.get("key_code","SPACE"));
            keyword_color = savestate.get("color_keywords","00ffff");
            compact_chatbox = savestate.get("chatbox_type","").equals("1");
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}

    }

    //Fetches the user id from the net
    private void fetch_user_id() {
        if(channel==null||channel.equals(""))return;
        try {
            URL url = new URL("https://api.twitch.tv/kraken/users/" + channel + "/?client_id=ib3fwre0zolczpxga19t74a6qc3qjxo");
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String s = br.readLine();
            channel_id= s.substring(s.indexOf("_id\":") + 5, s.indexOf(",\"name\""));
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(e.getMessage());
        }
    }


    public String get_user()
    {
        return user;
    }
    public String get_pw()
    {
        return pw;
    }

    public int get_buttoncount(){return buttoncount;}
    public int[] get_timeoutduration(){return timeoutduration;}
    public String get_channel(){return channel;}
    public Image_handler get_imagehandler(){return imh;}
    public String[] get_keywords(){return keywords;}
    public KeyCode get_keycode(){return keyCode;}
    public int get_chatboxcount(){return chatboxcount;}
    public String get_keyword_color(){return keyword_color;}
    public boolean get_chatbox_type(){return compact_chatbox;}
}
