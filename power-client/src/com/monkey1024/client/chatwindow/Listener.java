package com.monkey1024.client.chatwindow;

import com.monkey1024.bean.Message;
import com.monkey1024.bean.MessageType;
import com.monkey1024.client.login.LoginController;

import java.io.*;
import java.net.Socket;


public class Listener implements Runnable {

    private static String picture;
    private Socket socket;
    public String hostname;
    public int port;
    public static String username;
    public ChatController chatController;
    private static ObjectOutputStream oos;
    private InputStream inputStream;
    private ObjectInputStream ois;
    private OutputStream outputStream;

    public Listener(String hostname, int port, String username, String picture, ChatController chatController) {
        this.hostname = hostname;
        this.port = port;
        Listener.username = username;
        Listener.picture = picture;
        this.chatController = chatController;
    }

    public void run() {
        try {
            //获取io对象
            socket = new Socket(hostname, port);
            LoginController.getInstance().showScene();
            outputStream = socket.getOutputStream();
            oos = new ObjectOutputStream(outputStream);
            inputStream = socket.getInputStream();
            ois = new ObjectInputStream(inputStream);

            connect();
            while (socket.isConnected()) {
                Message message = (Message) ois.readObject();

                if (message != null) {
                    switch (message.getType()) {
                        case TEXT:
                            chatController.showMsg(message);
                            break;
                        case NOTIFICATION:
                            chatController.newUserNotification(message);
                            break;
                        case JOINED:
                        case DISCONNECTED:
                            chatController.setUserList(message);
                            break;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送文字消息
     *
     * @param msg
     * @throws IOException
     */
    public static void send(String msg) throws IOException {
        Message newMsg = new Message();
        newMsg.setName(username);
        newMsg.setType(MessageType.TEXT);
        newMsg.setMsg(msg);
        newMsg.setPicture(picture);
        oos.writeObject(newMsg);
        oos.flush();
    }

    /**
     *  连接
     * @throws IOException
     */
    public static void connect() throws IOException {
        Message newMsg = new Message();
        newMsg.setName(username);
        newMsg.setType(MessageType.JOINED);
        newMsg.setMsg("已连接");
        newMsg.setPicture(picture);
        oos.writeObject(newMsg);
    }

}
