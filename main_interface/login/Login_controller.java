package main_interface.login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import main_interface.Main_window;

import java.awt.*;
import java.net.URI;
import java.util.Random;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Created by Leander
 * Controller of the login interface, fetches login information and passes it to login verifier to verify, if corrects
 * starts main application
 */

public class Login_controller {
    private final static Logger LOGGER = Logger.getGlobal();


    @FXML
    private Hyperlink oauth_hyperlink;

    @FXML
    private Button login_button;

    @FXML
    private TextField username_field;

    @FXML
    private PasswordField oauth_field;

    @FXML
    private Text wrong_login;

    private Login_information login;
    private Main_window mw;

    //called after fields are injected
    @FXML
    public void initialize()
    {
        oauth_field.setOnKeyPressed(event -> wrong_login.setOpacity(0));//ensure that wrong login display disappears after modifying text
        oauth_hyperlink.setOnAction(event ->open_oauth_link());
        try
        {
            Preferences login_save = Preferences.userNodeForPackage(Login_controller.class);
            username_field.setText(login_save.get("user",""));
            oauth_field.setText(de_obfuscate(login_save.get("pw","")));
        }catch (Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}

        //installs the login tooltip
        Tooltip logintip = new Tooltip("For read only access use User: justinfan1234 with no/random password.");
        Tooltip.install(username_field,logintip);
        Tooltip.install(oauth_field,logintip);
    }


    @FXML
    void login_pressed(ActionEvent event) {

        String user = username_field.getText();
        String oauth = oauth_field.getText();
        login = new Login_information(user, oauth);
        Login_verifier verifier = new Login_verifier();

        if(verifier.verify_login(user,oauth))
        {
            if(!user.matches("^justinfan(\\d+)?$"))
            {
                try {
                    Preferences login_save = Preferences.userNodeForPackage(Login_controller.class);
                    login_save.put("user", user);
                    login_save.put("pw", obfuscate(oauth));
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.info(e.getMessage());
                }
            }
            //close current stage
            Node n = (Node) event.getSource();
            Stage stage = (Stage) n.getScene().getWindow();
            stage.close();
            //start actual application
            mw.start_main_application(login,user.matches("^justinfan(\\d+)?$"));
        }
        else
        {
            //show message that login was false
            wrong_login.setOpacity(1);
        }
    }

    //sets the mainwindow field of this instance to the given object
    public void set_main_class(Main_window mw)
    {
        this.mw = mw;
    }


    //Obfuscates a string, used to store the pw
    //WARNING: DOES NOT PROVIDE ACTUAL SECURITY, OBFUSCATION != ENCRYPTION
    private String obfuscate(String input)
    {
        //Randomly chosen long constant
        Random r = new Random(4639483783L);
        String result = "";
        //Shift each character by the value returned by nextint
        for(char c:input.toCharArray())
        {
            int offset = r.nextInt(100)+50;
            char shifted_char = (char)(c+offset);
            result = result+shifted_char;
        }
        return result;
    }

    //de-obfuscates a string obfuscated with the obfuscate method
    private String de_obfuscate(String input)
    {
        Random r = new Random(4639483783L);
        String result = "";
        //Shift each character back to the original value
        for(char c:input.toCharArray())
        {
            int offset = r.nextInt(100)+50;
            char shifted_char = (char)(c-offset);
            result = result+shifted_char;
        }
        return result;
    }

    //opens a weblink to a page to fetch an oauth token
    private void open_oauth_link()
    {
        Desktop desktop = Desktop.isDesktopSupported()?Desktop.getDesktop():null;
        if(desktop!=null&&desktop.isSupported(Desktop.Action.BROWSE))
        {
            try
            {
                desktop.browse(new URI("http://www.twitchapps.com/tmi/"));
            }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
        }
    }

}
