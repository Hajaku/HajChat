package main_interface.logviewer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main_interface.Main_window;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Created by Leander on 27.09.2016.
 * Class which is instantiated on log lookup, creates the logviewer window and displays logs
 * */
public class Logviewer {
    private final static Logger LOGGER = Logger.getGlobal();

    private String user;
    private String channel;

    public Logviewer(String username, String channel)
    {
        this.user = username;
        this.channel = channel.trim();
    }


    //Creates the logviewer window and sets the values
    public void show_logs(int lines)
    {
        try {
            Stage logviewer_stage = new Stage();
            logviewer_stage.getIcons().add(new Image(getClass().getResourceAsStream("/Data/icons/main_icon_cut.png")));
            logviewer_stage.setTitle("Logviewer");
            Pane logviewer_pane = new Pane();
            Scene logviewer_scene = new Scene(logviewer_pane, 490, 590);
            FXMLLoader fxmlLoader = new FXMLLoader(Main_window.class.getResource("layout_elements/logviewer_display.fxml"));
            VBox logviewer_window = fxmlLoader.load();
            Logviewer_controller controller = fxmlLoader.getController();

            controller.set_content(user,channel,lookup_logs(lines));

            logviewer_pane.getChildren().add(logviewer_window);
            logviewer_stage.setScene(logviewer_scene);
            logviewer_stage.setResizable(false);
            logviewer_stage.show();
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    //Lookup logs of a specific user and channel by connecting to cbennis public logviewer api
    private String lookup_logs(int lines)
    {
        try{

            URL url = new URL("https://cbenni.com/api/slack/?text="+user+"+"+channel+"+"+lines);
            BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
            String output = "";
            String line;
            while((line = br.readLine())!=null)
            {
                output+="\n"+line;
            }
            return output.substring(1);//remove first linebreak
        } catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
        return "Lookup failed :(";
    }


}
