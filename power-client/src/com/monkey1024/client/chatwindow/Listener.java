package com.monkey1024.client.chatwindow;

import com.monkey1024.bean.Message;
import com.monkey1024.bean.MessageType;
import com.monkey1024.client.login.LoginController;

import java.io.*;
import java.net.Socket;

/*
    监听器
 */
public class Listener implements Runnable{

    private Socket socket;
    public String hostname;
    public int port;
    public static String username;
    public ChatController controller;
    private static ObjectOutputStream oos;
    private InputStream is;
    private ObjectInputStream input;
    private OutputStream outputStream;

    public Listener(String hostname, int port, String username, ChatController controller) {
        this.hostname = hostname;
        this.port = port;
        Listener.username = username;
        this.controller = controller;
    }

    public void run() {
        try {
            socket = new Socket(hostname, port);
            LoginController.getInstance().showScene();
            outputStream = socket.getOutputStream();
            oos = new ObjectOutputStream(outputStream);
            is = socket.getInputStream();
            input = new ObjectInputStream(is);
        } catch (IOException e) {
            LoginController.getInstance().showErrorDialog("无法连接");
        }
        System.out.println("开始连接"+ socket.getInetAddress() + ":" + socket.getPort());

        try {
            connect();
            while (socket.isConnected()) {
                Message message = null;
                message = (Message) input.readObject();

                if (message != null) {
                    System.out.println("接收消息:" + message.getMsg() + "， 消息类型:" + message.getType());
                    switch (message.getType()) {
                        case USER:
                        case VOICE:
                            controller.addToChat(message);
                            break;
                        case NOTIFICATION:
                            controller.newUserNotification(message);
                            break;
                        case SERVER:
                            //controller.addAsServer(message);
                            break;
                        case CONNECTED:
                        case DISCONNECTED:
                            controller.setUserList(message);
                            break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            controller.logoutScene();
        }
    }

    /*
        发送消息
     */
    public static void send(String msg) throws IOException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(MessageType.USER);

        createMessage.setMsg(msg);
        oos.writeObject(createMessage);
        oos.flush();
    }

    /*
        发送语音消息
     */
    public static void sendVoiceMessage(byte[] audio) throws IOException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(MessageType.VOICE);

        createMessage.setVoiceMsg(audio);
        oos.writeObject(createMessage);
        oos.flush();
    }


    /*
        发送连接消息
     */
    public static void connect() throws IOException {
        Message createMessage = new Message();
        createMessage.setName(username);
        createMessage.setType(MessageType.CONNECTED);
        createMessage.setMsg("已连接");
        oos.writeObject(createMessage);
    }

}
