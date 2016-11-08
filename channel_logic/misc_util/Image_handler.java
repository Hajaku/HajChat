package channel_logic.misc_util;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by Leander on 21.08.2016.
 * Loads all of the badges from the local memory, instantiated for each channel, loads subbadge from the net
 * Also handles emotes inside of messages, these are fetched from the twitch kraken api
 */
public class Image_handler {
    private final static Logger LOGGER = Logger.getGlobal();

    private String channel;
    private String channel_id;

    private static Image staff;
    private static Image admin;
    private static Image globalmod;
    private static Image broadcaster;
    private static Image mod;
    private static Image turbo;
    private static Image premium;
    private static Image[] cheer = new Image[6];
    private static Image[] cheer_animated = new Image[6];

    private HashMap<Integer,Image> sub = new HashMap<>();

    private static HashMap<String,Image> image_cache = new HashMap<>();

    private static boolean static_images_initialized = false;

    Image_handler(String channel,String channel_id)
    {
        this.channel = channel;
        this.channel_id = channel_id;
    }


    //As javafx needs to have graphics initialized before you can actually load images
    //initialize is called at the start of the application method.
    public void initialize()
    {
        initialize_subbadge();
        synchronized (this) {
            if(static_images_initialized)return;
            load_badges();
            load_cheerbadges();
            static_images_initialized = true;
        }
    }

    //Loads the badges
    private void load_badges()
    {
        try {
                staff = new Image("https://static-cdn.jtvnw.net/chat-badges/staff.png");
                admin = new Image("https://static-cdn.jtvnw.net/chat-badges/admin.png");
                globalmod = new Image("https://static-cdn.jtvnw.net/chat-badges/globalmod.png");
                broadcaster = new Image("https://static-cdn.jtvnw.net/chat-badges/broadcaster.png");
                mod = new Image("https://static-cdn.jtvnw.net/chat-badges/mod.png");
                turbo = new Image("https://static-cdn.jtvnw.net/chat-badges/turbo.png");
                premium = new Image("https://static-cdn.jtvnw.net/badges/v1/a1dd5073-19c3-4911-8cb4-c464a7bc1510/1");
        }catch (Exception e){
            e.printStackTrace();
            LOGGER.info(e.getMessage());
        }
    }

    //Loads the cheerbadges
    private void load_cheerbadges()
    {
        try {
            cheer[0] = new Image("http://static-cdn.jtvnw.net/bits/light/static/gray/1");
            cheer[1] = new Image("http://static-cdn.jtvnw.net/bits/light/static/purple/1");
            cheer[2] = new Image("http://static-cdn.jtvnw.net/bits/light/static/green/1");
            cheer[3] = new Image("http://static-cdn.jtvnw.net/bits/light/static/blue/1");
            cheer[4] = new Image("http://static-cdn.jtvnw.net/bits/light/static/red/1");
            cheer[5] = new Image("http://static-cdn.jtvnw.net/bits/light/static/gold/1");
            cheer_animated[0] = new Image("http://static-cdn.jtvnw.net/bits/light/animated/gray/1");
            cheer_animated[1] = new Image("http://static-cdn.jtvnw.net/bits/light/animated/purple/1");
            cheer_animated[2] = new Image("http://static-cdn.jtvnw.net/bits/light/animated/green/1");
            cheer_animated[3] = new Image("http://static-cdn.jtvnw.net/bits/light/animated/blue/1");
            cheer_animated[4] = new Image("http://static-cdn.jtvnw.net/bits/light/animated/red/1");
            cheer_animated[5] = new Image("http://static-cdn.jtvnw.net/bits/light/animated/gold/1");
        }catch(Exception e){
            e.printStackTrace();
            LOGGER.info(e.getMessage());
        }
    }


    //Loads the subscriber badges from the twitch api
    private void initialize_subbadge()
    {
        try {
            //loading the badges json from the api
            URL suburl = new URL("https://badges.twitch.tv/v1/badges/channels/"+channel_id+"/display");
            BufferedReader br = new BufferedReader(new InputStreamReader(suburl.openStream()));
            String raw_json = "";
            String line = "";
            while((line = br.readLine())!=null){raw_json = raw_json+line;}
            br.close();
            //extracting the information from the json
            Gson g = new Gson();
            Map base_map = g.fromJson(raw_json,Map.class);
            if(base_map.containsKey("badge_sets"))
            {
                LinkedTreeMap badge_map = (LinkedTreeMap) base_map.get("badge_sets");
                if(badge_map.containsKey("subscriber")) {
                    LinkedTreeMap subscribers = (LinkedTreeMap)badge_map.get("subscriber");
                    LinkedTreeMap versions_map = (LinkedTreeMap) subscribers.get("versions");
                    //Extract the images of the different amount of months and put them into sub image hashmap
                    int[] months = {0, 1, 3, 6, 12, 24};
                    for (int i : months) {
                        if (versions_map.containsKey("" + i)) {
                            LinkedTreeMap image_map = (LinkedTreeMap)versions_map.get(""+i);
                            sub.put(i, new Image((String) image_map.get("image_url_1x")));
                        }
                    }
                }
            }
        }catch (Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    //Extracts the number of months from the badge string and returns the appropriate badge
    private ImageView request_subbadge(String badge)
    {
        try{
            int month = Integer.parseInt(badge.substring(badge.indexOf("/")+1));
            ImageView subbadge = new ImageView(sub.get(month));
            Tooltip tooltip = new Tooltip("Subscriber for atleast " + month + " months.");
            Tooltip.install(subbadge,change_tooltip_delay(tooltip));
            return subbadge;
        }catch (Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
        return null;
    }

    //Returns the requested badge, default return is "Kappa" emote.
    public ImageView request_badge(String badge)
    {
        if(badge.contains("bits"))
        {
            return new ImageView(request_cheerbadge(badge,true));
        }
        if(badge.contains("subscriber"))
        {
            return request_subbadge(badge);
        }
        if(badge.contains("turbo"))
        {
            return new ImageView(turbo);
        }
        if(badge.contains("premium"))
        {
            return new ImageView(premium);
        }
        if(badge.contains("broadcaster"))
        {
            return new ImageView(broadcaster);
        }
        if(badge.contains("moderator"))
        {
            return new ImageView(mod);
        }
        if(badge.contains("admin"))
        {
            return new ImageView(admin);
        }
        else if(badge.contains("staff"))
        {
            return new ImageView(staff);
        }
        else if(badge.contains("global_mod"))
        {
            return new ImageView(globalmod);
        }

        return new ImageView(new Image("http://static-cdn.jtvnw.net/emoticons/v1/25/1.0"));//Kappa, returns as placeholder when invalid badge is requested
    }




    //Returns a cheerbadge
    private Image request_cheerbadge(String cheer_badge,boolean animated)
    {
        int bits = Integer.parseInt(cheer_badge.split("/")[1]);
        if(bits>=100000){if(animated){return cheer_animated[5];}else{return cheer[5];}}
        if(bits>=10000){if(animated){return cheer_animated[4];}else{return cheer[4];}}
        if(bits>=5000){if(animated){return cheer_animated[3];}else{return cheer[3];}}
        if(bits>=1000){if(animated){return cheer_animated[2];}else{return cheer[2];}}
        if(bits>=100){if(animated){return cheer_animated[1];}else{return cheer[1];}}
        if(animated){return cheer_animated[0];}else{return cheer[0];}
    }

    //returns a cheeremote, forwards request to request_cheerbadge(...)
    public Image request_cheeremote(int bits,boolean animated)
    {
        return request_cheerbadge("cheer/"+bits,animated);
    }

    //Returns the requested emote, first checks the image_cache, if not found loads from internet
    public ImageView request_emote(String emote)
    {
        String tooltip = emote.substring(emote.indexOf(")")+1);
        Tooltip t = change_tooltip_delay(new Tooltip(tooltip));
        ImageView return_imageview = null;
        if(image_cache.containsKey(emote))return_imageview = new ImageView(image_cache.get(emote));
        //If not in cache, image is loaded from net
        if(return_imageview==null)return_imageview = load_emote_from_net(emote);
        Tooltip.install(return_imageview,t);
        return return_imageview;
    }

    //Loads an emote from the twitch api
    private ImageView load_emote_from_net(String name)
    {
        if(name.equals(""))return null;
        String emotecode = name.substring(1,name.indexOf(")"));
        String url = "http://static-cdn.jtvnw.net/emoticons/v1/"+emotecode+"/1.0";
        Image img = new Image(url);

        image_cache.put(name,img);
        return new ImageView(img);
    }


    //As java 8 does not support changing the delay of tooltips yet (introduced in java 9) changed using reflection
    private static Tooltip change_tooltip_delay(Tooltip tooltip) {

        if(tooltip==null)return tooltip;
        try {
            Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
            fieldBehavior.setAccessible(true);
            Object objBehavior = fieldBehavior.get(tooltip);

            Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
            fieldTimer.setAccessible(true);
            Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

            objTimer.getKeyFrames().clear();
            objTimer.getKeyFrames().add(new KeyFrame(new javafx.util.Duration(100)));
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.info(e.getMessage());
        }
        return tooltip;

    }


}

