package chat_interface;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.util.Random;

/**
 * Created by Leander on 23.08.2016.
 */
public class testclass extends Application{


    public static void main(String[] args) {
        Application.launch(args);
    }


    ObservableList<HBox> testlist;
    ListView<HBox> list;

    @Override
    public void start(Stage primaryStage)
    {
        try {
            Group root = new Group();
            Scene scene = new Scene(root, 500, 200);

            //Media pick = new Media("https://video-edge-c55bbc.fra02.hls.ttvnw.net/hls-826afc/savjz_23457978240_534794530/medium/index-live.m3u8?token=id=3212450881951557492,bid=23457978240,exp=1477084677,node=video-edge-c55bbc.fra02,nname=video-edge-c55bbc.fra02,proto=https,fmt=medium&sig=cb8aad0fe479ebd2194401f325f91a44e04b3970");
            Media pick = new Media("https://video-edge-c2a948.fra02.hls.ttvnw.net/hls-826afc/savjz_23457978240_534794530/high/index-live.m3u8?token=id=2115674731634362687,bid=23457978240,exp=1477085213,node=video-edge-c2a948.fra02,nname=video-edge-c2a948.fra02,proto=https,fmt=high&sig=4de5ac63d5c293849639225a952709ce0755308d");
            //Media pick = new Media("http://0.s3.envato.com/h264-video-previews/80fad324-9db4-11e3-bf3d-0050569255a8/490527.mp4");
            MediaPlayer player = new MediaPlayer(pick);
            player.play();

            //Add a mediaView, to display the media. Its necessary !
            //This mediaView is added to a Pane
            MediaView mediaView = new MediaView(player);
            ((Group)scene.getRoot()).getChildren().add(mediaView);

            //show the stage
            primaryStage.setTitle("Media Player");
            primaryStage.setScene(scene);
            primaryStage.show();


        }catch(Exception e){e.printStackTrace();}
            /*
        }
            Group root = new Group();
            Scene s = new Scene(root,600,800);
            /*
            list = new ListView<>();
            testlist = FXCollections.observableArrayList();
            list.setFixedCellSize(-1);
            list.setCellFactory(param -> new custom_cell());

            list.setItems(testlist);
            list.prefHeightProperty().bind(s.heightProperty());
            list.prefWidthProperty().bind(s.widthProperty());

            root.getChildren().add(list);
            */
            /*
            WebView browser = new WebView();
            WebEngine engine = browser.getEngine();
            //engine.load("http://player.twitch.tv/?channel=silvername");
            engine.load("https://html5test.com/index.html");
            browser.prefWidthProperty().bind(s.widthProperty());
            root.getChildren().add(browser);
            primaryStage.setScene(s);
            */
            /*
            //Hbox_creator hbox_creator = new Hbox_creator(this);
            //hbox_creator.start();
            Media m = new Media("https://video-edge-c55c6c.fra02.hls.ttvnw.net/transcode-x2-1efe72/silvername_23456881312_534725972/mobile/index-live.m3u8?token=id=8430851297536355984,bid=23456881312,exp=1477055756,node=video-edge-c55c6c.fra02,nname=video-edge-c55c6c.fra02,proto=https,fmt=mobile&sig=d7afe736d84bc2785951a31ee8f278c8cafa5a30");
            MediaPlayer mediaPlayer = new MediaPlayer(m);
            mediaPlayer.play();
            MediaView mv = new MediaView(mediaPlayer);
            mediaPlayer.setAutoPlay(true);
            mv.setStyle("-fx-border-color: darkred;-fx-border-width: 2px");

            root.getChildren().add(mv);

            primaryStage.setScene(s);
            primaryStage.show();
            */
        //}catch(Exception e){e.printStackTrace();}

    }


    void add_box(HBox hbox)
    {
        Platform.runLater(()->testlist.add(hbox));
        Platform.runLater(()->list.scrollTo(hbox));
    }


    private class custom_cell extends ListCell<HBox>
    {

        custom_cell(){}
        @Override
        public void updateItem(HBox item, boolean empty)
        {
            super.updateItem(item, empty);
            if(!empty&&item!=null) {
                item.minWidthProperty().bind(widthProperty());
                item.maxWidthProperty().bind(widthProperty());

                minHeightProperty().bind(item.heightProperty());
                prefHeightProperty().bind(item.heightProperty());
                //maxHeightProperty().bind(item.heightProperty());
                setGraphic(item);
            }
            else
            {
                setGraphic(null);
            }
        }

    }

    private class Hbox_creator extends Thread{
        testclass test;

        Hbox_creator(testclass test)
        {
            this.test = test;
        }


        public void run()
        {
            String[] co = {"black","blue","purple","red"};
            Random r = new Random();
            int i =0;

            while(true)
            {
                ++i;
                HBox h = new HBox();
                int height = r.nextInt(50)+20;
                h.setMinHeight(height);
                h.setPrefHeight(height);
                h.setMaxHeight(height);
                h.setMinWidth(10);
                h.setStyle("-fx-border-color:"+co[i%co.length]+";-fx-border-width: 5pt");
                test.add_box(h);
                System.out.println(height);

                try{
                    Thread.sleep(1000);
                }catch(Exception e){e.printStackTrace();}
            }
        }
    }



}
