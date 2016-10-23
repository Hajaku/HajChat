package channel_logic.misc_util;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import javafx.scene.image.Image;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
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

    private ArrayList<image_wrapper> image_cache = new ArrayList<>();

    Image_handler(String channel,String channel_id)
    {
        this.channel = channel;
        this.channel_id = channel_id;
    }


    //As javafx needs to have graphics initialized before you can actually load images
    //initialize is called at the start of the application method.
    public void initialize()
    {
        load_badges();
        load_cheerbadges();
        initialize_subbadge();
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
    private Image request_subbadge(String badge)
    {
        try{
            int month = Integer.parseInt(badge.substring(badge.indexOf("/")+1));
            return sub.get(month);
        }catch (Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
        return null;
    }

    //Returns the requested badge, default return is "Kappa" emote.
    public Image request_badge(String badge)
    {
        if(badge.contains("bits"))
        {
            return request_cheerbadge(badge,true);
        }
        if(badge.contains("subscriber"))
        {
            return request_subbadge(badge);
        }
        if(badge.contains("turbo"))
        {
            return turbo;
        }
        if(badge.contains("premium"))
        {
            return premium;
        }
        if(badge.contains("broadcaster"))
        {
            return broadcaster;
        }
        if(badge.contains("moderator"))
        {
            return mod;
        }
        if(badge.contains("admin"))
        {
            return admin;
        }
        else if(badge.contains("staff"))
        {
            return staff;
        }
        else if(badge.contains("global_mod"))
        {
            return globalmod;
        }

        return new Image("http://static-cdn.jtvnw.net/emoticons/v1/25/1.0");//Kappa, returns as placeholder when invalid badge is requested
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
    public Image request_emote(String emote)
    {
        for(int i=0;i<image_cache.size();i++)
        {
            image_wrapper imw = image_cache.get(i);
            //If element is found in cache its wrapper is put at position 0 and the corresponding image is returned
            if(imw.check_name(emote))
            {
                Image return_image = imw.get_image();
                image_cache.remove(i);
                image_cache.add(0,imw);
                return return_image;
            }
        }
        //If not in cache, image is loaded from net
        return load_emote_from_net(emote);
    }

    //Loads an emote from the twitch api
    private Image load_emote_from_net(String name)
    {
        if(name.equals(""))return null;
        String emotecode = name.substring(1,name.indexOf(")"));
        String url = "http://static-cdn.jtvnw.net/emoticons/v1/"+emotecode+"/1.0";
        Image img = new Image(url);

        if(image_cache.size()==50)
        {
            image_wrapper wrap = image_cache.get(49);
            image_cache.remove(49);
            wrap.set_name(name);
            wrap.set_image(img);
            image_cache.add(0,wrap);
        }
        else
        {
            image_wrapper wrap = new image_wrapper(name,img);
            image_cache.add(0,wrap);
        }
        return new Image(url);
    }

    //Private class used to cache images
    private class image_wrapper
    {
        private String name;
        private Image image;

        image_wrapper(String name,Image img)
        {
            this.image = img;
            this.name = name;
        }

        void set_name(String name){this.name = name;}
        void set_image(Image image){this.image = image;}
        Image get_image(){return image;}
        String get_name(){return name;}
        boolean check_name(String name){return this.name.equals(name);}
    }

}

