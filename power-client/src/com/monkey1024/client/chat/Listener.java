package com.monkey1024.client.chat;

import com.monkey1024.bean.Message;
import com.monkey1024.bean.MessageType;
import com.monkey1024.client.login.LoginController;

import java.io.*;
import java.net.Socket;


public class Listener implements Runnable {

    private String picture;
    private Socket socket;
    private String hostname;
    private int port;
    private String username;
    private ChatController chatController;
    private ObjectOutputStream oos;
    private InputStream inputStream;
    private ObjectInputStream ois;
    private OutputStream outputStream;

    public static Listener instance;

    public Listener(String hostname, int port, String username, String picture, ChatController chatController) {
        this.hostname = hostname;
        this.port = port;
        this.username = username;
        this.picture = picture;
        this.chatController = chatController;
        instance = this;
    }

    public void run() {
        try {
            //获取io对象
            socket = new Socket(hostname, port);
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
                            LoginController.getInstance().showScene();
                            chatController.notify(message.getName() + "加入聊天",message.getPicture(),"新朋友加入","sounds/Global.wav");
                            break;
                        case ERROR:
                            chatController.notify(message.getMsg(),message.getPicture(),"出问题了","sounds/system.wav");
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
    public void send(String msg) throws IOException {
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
    public void connect() throws IOException {
        Message newMsg = new Message();
        newMsg.setName(username);
        newMsg.setType(MessageType.JOINED);
        newMsg.setMsg("已连接");
        newMsg.setPicture(picture);
        oos.writeObject(newMsg);
    }

}
