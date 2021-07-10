package com.monkey1024.client.chatwindow;

import com.monkey1024.bean.Message;
import com.monkey1024.bean.MessageType;
import com.monkey1024.bean.User;
import com.monkey1024.bean.bubble.BubbleSpec;
import com.monkey1024.bean.bubble.BubbledLabel;
import com.monkey1024.client.login.MainLauncher;
import com.monkey1024.client.util.VoicePlayback;
import com.monkey1024.client.util.VoiceRecorder;
import com.monkey1024.client.util.VoiceUtil;
import com.monkey1024.traynotifications.animations.AnimationType;
import com.monkey1024.traynotifications.notification.TrayNotification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;


public class ChatController implements Initializable {

    @FXML private TextArea messageBox;
    @FXML private Label usernameLabel;
    @FXML private Label onlineCountLabel;
    @FXML private ListView userList;
    @FXML private ImageView userImageView;
    @FXML ListView chatPane;
    @FXML BorderPane borderPane;
    @FXML ImageView microphoneImageView;

    Image microphoneActiveImage = new Image(getClass().getClassLoader().getResource("images/microphone-active.png").toString());
    Image microphoneInactiveImage = new Image(getClass().getClassLoader().getResource("images/microphone.png").toString());

    private double xOffset;
    private double yOffset;

    /*
        发送消息
     */
    public void sendButtonAction() throws IOException {
        String msg = messageBox.getText();
        if (!messageBox.getText().isEmpty()) {
            Listener.send(msg);
            messageBox.clear();
        }
    }

    /*
        录制语音消息
     */
    public void recordVoiceMessage()  {
        if (VoiceUtil.isRecording()) {
            Platform.runLater(() -> microphoneImageView.setImage(microphoneInactiveImage));
            VoiceUtil.setRecording(false);
        } else {
            Platform.runLater(() -> microphoneImageView.setImage(microphoneActiveImage));
            VoiceRecorder.captureAudio();
        }
    }

    /**
     *  根据消息发送者的不同从而改变展示的效果
     * @param msg  发送的消息
     * @param flag  true表示自己发送的，false表示别人发送
     * @return
     */
    private HBox showMessage(Message msg, boolean flag) {
        Label label = new Label(msg.getName());
        BubbledLabel bl6 = new BubbledLabel();
        //判断是语音消息还是文字消息
        if (msg.getType() == MessageType.VOICE){
            bl6.setGraphic(new ImageView(new Image(getClass().getClassLoader().getResource("images/sound.png").toString())));
            bl6.setText("语音消息");
            VoicePlayback.playAudio(msg.getVoiceMsg());
        }else {
            bl6.setText(msg.getMsg());
        }
        HBox hBox = new HBox();

        //判断是自己发送的消息还是别人发送的消息
        if (flag) {
            bl6.setBackground(new Background(new BackgroundFill(Color.LIGHTGREEN,
                    null, null)));
            hBox.setMaxWidth(chatPane.getWidth() - 20);
            hBox.setAlignment(Pos.TOP_RIGHT);
            bl6.setBubbleSpec(BubbleSpec.FACE_RIGHT_CENTER);
            hBox.getChildren().addAll(bl6, label);

            setOnlineLabel(Integer.toString(msg.getUserList().size()));
        }else {
            bl6.setBackground(new Background(new BackgroundFill(Color.WHITE,null, null)));

            bl6.setBubbleSpec(BubbleSpec.FACE_LEFT_CENTER);
            hBox.getChildren().addAll(label, bl6);
            setOnlineLabel(Integer.toString(msg.getUserList().size()));
        }

        return hBox;
    }

    /*
        将信息展示到界面中
     */
    public synchronized void addToChat(Message msg) {

        //判断当前是谁发送的信息
        if (msg.getName().equals(usernameLabel.getText())) {
            //自己发送的消息
            Task<HBox> yourMessages = new Task<HBox>() {
                @Override
                public HBox call() {
                    return showMessage(msg,true);
                }
            };
            yourMessages.setOnSucceeded(event -> chatPane.getItems().add(yourMessages.getValue()));

            Thread yourThread = new Thread(yourMessages);
            yourThread.setDaemon(true);
            yourThread.start();
        } else {
            //别人发送的消息
            Task<HBox> othersMessages = new Task<HBox>() {
                @Override
                public HBox call() {
                    return showMessage(msg,false);
                }
            };
            othersMessages.setOnSucceeded(event -> chatPane.getItems().add(othersMessages.getValue()));

            Thread otherThread = new Thread(othersMessages);
            otherThread.setDaemon(true);
            otherThread.start();
        }
    }
    public void setUsernameLabel(String username) {
        this.usernameLabel.setText(username);
    }

    public void setImageLabel() {
        this.userImageView.setImage(new Image(getClass().getClassLoader().getResource("images/default.png").toString()));
    }

    public void setOnlineLabel(String count) {
        Platform.runLater(() -> onlineCountLabel.setText(count));
    }

    public void setUserList(Message msg) {
        Platform.runLater(() -> {
            ObservableList<User> users = FXCollections.observableList(msg.getUserList());
            userList.setItems(users);
            setOnlineLabel(String.valueOf(msg.getUserList().size()));
        });
    }

    /*
        登录之后通知信息
     */
    public void newUserNotification(Message msg) {
        Platform.runLater(() -> {
            Image profileImg = new Image(getClass().getClassLoader().getResource("images/line.png").toString(),50,50,false,false);
            TrayNotification tray = new TrayNotification();
            tray.setTitle("新朋友来了!");
            tray.setMessage(msg.getName() + " 登录聊天室!");
            tray.setRectangleFill(Paint.valueOf("#2C3E50"));
            tray.setAnimationType(AnimationType.POPUP);
            tray.setImage(profileImg);
            tray.showAndDismiss(Duration.seconds(5));
            try {
                Media hit = new Media(getClass().getClassLoader().getResource("sounds/notification.wav").toString());
                MediaPlayer mediaPlayer = new MediaPlayer(hit);
                mediaPlayer.play();
            } catch (Exception e) {
                e.printStackTrace();
            }

        });
    }

    public void sendMethod(KeyEvent event) throws IOException {
        if (event.getCode() == KeyCode.ENTER) {
            sendButtonAction();
        }
    }

    /**
     *  关闭应用
     */
    @FXML
    public void closeApplication() {
        Platform.exit();
        System.exit(0);
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setImageLabel();

                /* Drag and Drop */
        borderPane.setOnMousePressed(event -> {
            xOffset = MainLauncher.getPrimaryStage().getX() - event.getScreenX();
            yOffset = MainLauncher.getPrimaryStage().getY() - event.getScreenY();
            borderPane.setCursor(Cursor.CLOSED_HAND);
        });

        borderPane.setOnMouseDragged(event -> {
            MainLauncher.getPrimaryStage().setX(event.getScreenX() + xOffset);
            MainLauncher.getPrimaryStage().setY(event.getScreenY() + yOffset);

        });

        borderPane.setOnMouseReleased(event -> {
            borderPane.setCursor(Cursor.DEFAULT);
        });

        /* Added to prevent the enter from adding a new line to inputMessageBox */
        messageBox.addEventFilter(KeyEvent.KEY_PRESSED, ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                try {
                    sendButtonAction();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ke.consume();
            }
        });

    }

    public void logoutScene() {
        Platform.runLater(() -> {
            FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Parent window = null;
            try {
                window = (Pane) fmxlLoader.load();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Stage stage = MainLauncher.getPrimaryStage();
            Scene scene = new Scene(window);
            stage.setMaxWidth(350);
            stage.setMaxHeight(420);
            stage.setResizable(false);
            stage.setScene(scene);
            stage.centerOnScreen();
        });
    }
}