package main_interface;

import channel_logic.misc_util.Constants;
import channel_logic.misc_util.Saver;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import main_interface.login.Login_controller;
import main_interface.login.Login_information;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * Created by Leander on 06.09.2016.
 * Main class of the application, launched by the jar, starts the login process
 *
 *
 * TODO: TESTING
 *
 * TODO: Important:
 * selectable text
 * colors disappearing -_- (probably a caching issue or something similar)
 *
 * TODO: Not urgent:
 *
 * TODO: possible future features
 * zoom/size selection
 *
 * TODO: possible performance improves
 *
 */


public class Main_window extends Application {
    private final static Logger LOGGER = Logger.getGlobal();

    public static void main(String[] args) {
        Application.launch(args);
    }

    private Stage primaryStage;
    //called automatically at start of main application
    public void start(Stage primaryStage)
    {
        try {
            //initialize logger
            LOGGER.addHandler(new FileHandler("HajChat_log.log"));

            this.primaryStage = primaryStage;

            //Check if first start of application, if that's the case initialize options with default values
            check_first_start();

            //actual start of the program/login process
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/Data/icons/main_icon_cut.png")));
            primaryStage.setTitle("HajChat");
            start_login();
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    //Check if the application has been started before, if not initializes default values
    private void check_first_start()
    {
        Preferences pref = Preferences.userNodeForPackage(Constants.class);
        String init_test = pref.get("buttoncount","");
        if(init_test.equals(""))
        {
            new Saver().set_default();
        }
    }

    //Starts the login process, opens the login window
    private void start_login()
    {
        try{
            //start of login process
            Stage login_stage = new Stage();
            login_stage.getIcons().add(new Image(getClass().getResourceAsStream("/Data/icons/main_icon_cut.png")));
            login_stage.setTitle("Login");
            Pane login_pane = new Pane();
            Scene login_scene = new Scene(login_pane, 300, 350);
            FXMLLoader login_loader = new FXMLLoader(getClass().getResource("layout_elements/login_interface.fxml"));
            VBox login_vbox = login_loader.load();
            Login_controller log_con = login_loader.getController();
            log_con.set_main_class(this);
            login_pane.getChildren().add(login_vbox);
            login_stage.setScene(login_scene);
            login_stage.setResizable(false);
            login_stage.show();
            //end of login creation, login controller starts main application

        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}

    }

    //Starts the main application window
    public void start_main_application(Login_information login, boolean read_only)
    {
        try {
            //Load interface
            Pane pane = new Pane();
            Scene scene = new Scene(pane, 1400, 860);
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("layout_elements/main_interface.fxml"));
            HBox mainwindow = fxmlLoader.load();
            ((Main_window_controller) fxmlLoader.getController()).set_login(login,read_only);
            scene.getStylesheets().addAll("chat_interface/user_interface.css");
            pane.getChildren().add(mainwindow);
            mainwindow.prefHeightProperty().bind(pane.heightProperty());
            mainwindow.prefWidthProperty().bind(pane.widthProperty());

            define_close_operation(primaryStage);
            primaryStage.setScene(scene);
            primaryStage.show();
        }catch(Exception e){e.printStackTrace();LOGGER.info(e.getMessage());}
    }

    //defines the close operation, closes the interface and then exits/closes the java application
    private void define_close_operation(Stage primaryStage)
    {
        primaryStage.setOnCloseRequest(event ->
        {
            System.out.println("Closing the application.");
            LOGGER.info("Closing the application.");
            Platform.exit();
            System.exit(0);
        });
    }

}
