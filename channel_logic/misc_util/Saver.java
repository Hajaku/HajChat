package channel_logic.misc_util;

import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Created by Leander on 08.09.2016.
 * Used to write default values for all the options in case the program has never been started before
 * also used by the options window to save new values for options
 * */
public class Saver {
    private final static Logger LOGGER = Logger.getGlobal();


    public static void main(String[] args) {

        Saver saver = new Saver();
        saver.save_state("3","3600,600","banana,BibleThump","Space","00ffff","0");
    }

    public void save_state(String buttoncount, String button_times, String keywords, String key_code, String color_keywords,String compact_chatbox)
    {
        try{
            Preferences savestate = Preferences.userNodeForPackage(Constants.class);
            if(buttoncount!=null)savestate.put("buttoncount",buttoncount);
            if(button_times!=null)savestate.put("button_times",button_times);
            if(keywords != null)savestate.put("keywords",keywords);
            if(key_code != null)savestate.put("key_code",key_code);
            if(color_keywords != null)savestate.put("color_keywords",color_keywords);
            if(compact_chatbox!=null)savestate.put("chatbox_type",compact_chatbox);
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    public void set_default()
    {
        save_state("3","3600,600","banana","Space","00ffff","0");
    }


}
