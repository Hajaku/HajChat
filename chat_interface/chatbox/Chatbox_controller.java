package chat_interface.chatbox;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

import java.util.Random;

/**
 * Created by Leander
 * Controller for a chatbox, handles selecting the color for the user and displaying all of the given content inside of
 * the chatbox,
 */

public class Chatbox_controller {

    @FXML
    private FlowPane textbox;

    @FXML
    private HBox chatheader;

    @FXML
    private HBox boundingbox;

    //Contains a variety of the standard twitch colors, some are omitted, used in "random"(hash based) color selection
    private String[] colorselection = {"#0000FF","#FF7F50","#1E90FF","#00FF7F","#FFFF00","#FF0000","#DAA520","FF69B4",
                                        "D2691E","B22222","#8A2BE2"};
    private String color;

    private long id; //TODO remove this
    private static long counter = 0;

    @FXML
    public void initialize()
    {
        id = counter;
        ++counter;
        textbox.prefWrapLengthProperty().setValue(Integer.MAX_VALUE);
    }


    //Resets the chatbox to default state
    void reset_chatbox()
    {
        color = "NULL";
        boundingbox.setStyle("-fx-opacity: 100%");
        textbox.getChildren().clear();
        textbox.setStyle("");
        chatheader.getChildren().clear();

        //System.out.println("RESET: "+id);
    }

    //Called on user ban, sets opacity to 35%
    void user_ban()
    {
        Platform.runLater(() ->boundingbox.setStyle("-fx-opacity: 35%"));
    }

    //Adds a textblock to the chatbox
    void add_chatbox(Text t)
    {
        textbox.getChildren().add(t);
    }

    //Adds a colored textblock to the chatbox
    void add_chatbox_colored(Text t)
    {
        t.setStyle("-fx-fill: "+color);
        add_chatbox(t);
    }

    //Adds an image to the chatbox
    void add_chatbox(Image img)
    {
        ImageView imv = new ImageView(img);
        textbox.getChildren().add(imv);
    }
    //Overloaded add_chatbox, displays tooltip
    void add_chatbox(Image img,String tooltip)
    {
        Tooltip t = new Tooltip(tooltip);
        ImageView imv = new ImageView(img);
        Tooltip.install(imv,t);
        textbox.getChildren().addAll(imv);
    }


    //Adds a name to the header, if no color is given it uses a random one from the colorselection
    void add_header(Text t, String color, boolean compact)
    {
        if(color.equals("NULL")){color = hash_name(t);}
        this.color = color;
        set_username_style(t);


        if(compact)
        {
            add_chatbox(t);
        }
        else
        {
            chatheader.getChildren().add(t);
        }

    }

    //sets the style of the username, if the username is too bright it adds a border for better readability
    private void set_username_style(Text t)
    {
        double percent = 0.5d;

        if(color.matches("^#.*$")) {
            String hexcolor = color;
            hexcolor = hexcolor.substring(1);
            int hex = Integer.parseInt(hexcolor, 16);
            int r = ((hex & 0xFF0000) >> 16);
            int g = ((hex & 0xFF00) >> 8);
            int b = ((hex & 0xFF));
            if (r + g + b > 700)//0 <= r,g,b <= 255, max 255*3 = 765, everything over 700 is seen as too bright == too close to white
            {
                String darkened_color = String.format("#%02x%02x%02x", (int) (r * percent), (int) (g * percent), (int) (b * percent));
                t.setStyle("-fx-font-weight: bold;-fx-fill:" + color + ";-fx-stroke:" + darkened_color + ";-fx-stroke-width: 1px");
                return;
            }
        }

        ///System.out.println("COLOR: "+color+ " in "+id);//TODO remove this

        t.setStyle("-fx-font-weight: bold;-fx-fill: "+color);

    }



    //Select a color for user who has no color preselected, uses hash function to generate randomness, ensures users
    //always have the same color
    private String hash_name(Text t)
    {
        int[] hash = {34,91,85,11};
        String user = t.getText();
        int hashvalue = 0;
        for(int i=0;i<user.length()&&i<4;i++)
        {
            hashvalue += (user.charAt(i)*hash[i]);
        }
        return colorselection[hashvalue%colorselection.length];
    }

    //Adds an image to the header, mainly used in badge
    void add_header(Image img)
    {
        ImageView imv = new ImageView(img);
        chatheader.getChildren().add(imv);
    }
    //Adds a button to the chatbox, no eventhandling is done here. Used for the banbuttons
    void add_header(Button b)
    {
        chatheader.getChildren().add(b);
    }

    //Slightly colors the background of the chatbox, used to mark messages containing keywords
    void mark_keywords(String color)
    {
        Platform.runLater(() -> boundingbox.setStyle("-fx-background-color: #"+color));
    }

}
