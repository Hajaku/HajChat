package main_interface.logviewer;

import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

import java.awt.*;
import java.net.URI;
import java.util.logging.Logger;

public class Logviewer_controller {
    private final static Logger LOGGER = Logger.getGlobal();

    @FXML
    private Text logs_title;

    @FXML
    private TextArea logviewer_textarea;

    @FXML
    private Hyperlink link_cbenni;

    @FXML
    private Hyperlink link_user;

    private String user;
    private String channel;

    @FXML
    public void open_user_link()
    {
        open_link("http://cbenni.com/"+channel+"/?user="+user);
    }
    @FXML
    public void open_cbenni_link()
    {
        open_link("http://cbenni.com/");
    }

    void set_content(String user,String channel, String logs)
    {
        logs = logs.replaceAll("```","");
        if(logs.contains("See http://cbenni.com/"+channel+"/?user="+user))logs = logs.substring(0,logs.indexOf("See http://cbenni.com/"+channel+"/?user="+user));
        this.user = user;
        this.channel = channel;
        logs_title.setText(user+ " in "+channel);
        logviewer_textarea.setText(logs);
        link_user.setText("Check http://cbenni.com/"+channel+"/?user="+user);
    }

    //Trys to open a generic weblink
    private void open_link(String link)
    {
        Desktop desktop = Desktop.isDesktopSupported()?Desktop.getDesktop():null;
        if(desktop!=null&&desktop.isSupported(Desktop.Action.BROWSE))
        {
            try
            {
                desktop.browse(new URI(link));
            }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
        }
    }



}
