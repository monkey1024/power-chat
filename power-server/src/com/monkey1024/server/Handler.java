package com.monkey1024.server;

import com.monkey1024.bean.Message;
import com.monkey1024.bean.MessageType;
import com.monkey1024.bean.User;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Handler implements Runnable {
    private Socket socket;
    private User user;
    private ObjectInputStream objectInputStream;
    private OutputStream outputStream;
    private ObjectOutputStream objectOutputStream;
    private InputStream inputStream;

    public Handler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try {
            inputStream = socket.getInputStream();
            objectInputStream = new ObjectInputStream(inputStream);
            outputStream = socket.getOutputStream();
            objectOutputStream = new ObjectOutputStream(outputStream);

            //获取信息
            Message firstMessage = (Message) objectInputStream.readObject();

            //判断用户名是否重复
            if (!checkDuplicateUsername(firstMessage)){
                return;
            }

            //将新加入聊天的输出流放入到set中
            Server.writers.add(objectOutputStream);
            //发送新用户加入聊天的通知
            sendNotification(firstMessage);

            showOnlineUser();

            while (socket.isConnected()) {
                Message message = (Message) objectInputStream.readObject();
                if (message != null) {
                    switch (message.getType()) {
                        case TEXT:
                            write(message);
                            break;
                        case JOINED:
                            showOnlineUser();
                            break;
                    }
                }
            }
        }catch (SocketException e) {
            e.printStackTrace();
        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnections();
        }
    }

    /**
     * 检查用户名是否重复
     * @param message
     * @throws Exception
     */
    private synchronized boolean checkDuplicateUsername(Message message) throws Exception {
        if (!Server.userMap.containsKey(message.getName())) {
            user = new User();
            user.setName(message.getName());
            user.setPicture(message.getPicture());
            Server.userMap.put(message.getName(), user);
            return true;
        } else {
            Message msg = new Message();
            msg.setMsg("用户名重复");
            msg.setType(MessageType.ERROR);
            msg.setName(message.getName());
            msg.setPicture(message.getPicture());
            write(msg);
            return false;
        }
    }

    /**
     *  客户端显示加入群聊的消息
     * @param message
     * @throws IOException
     */
    private void sendNotification(Message message) throws IOException {
        message.setMsg("加入群聊");
        message.setType(MessageType.NOTIFICATION);
        write(message);
    }

    /**
     *  退出聊天
     * @throws IOException
     */
    private void removeFromList() throws IOException {
        Message msg = new Message();
        msg.setMsg("离开了聊天");
        msg.setType(MessageType.DISCONNECTED);
        msg.setName("SERVER");
        write(msg);
    }

    /**
     *  向客户端显示当前在线用户
     * @return
     * @throws IOException
     */
    private void showOnlineUser() throws IOException {
        Message msg = new Message();
        msg.setMsg("欢迎加入聊天");
        msg.setType(MessageType.JOINED);
        msg.setName("SERVER");
        write(msg);
    }

    /**
     * 向客户端发送消息
     * @param msg
     * @throws IOException
     */
    private void write(Message msg) throws IOException {
        //设置在线用户
        msg.setOnlineUsers(new ArrayList<>(Server.userMap.values()));
        //将消息发送到客户端
        for (ObjectOutputStream writer : Server.writers) {
            writer.writeObject(msg);
        }
    }

    /**
     *  关闭链接
     */
    private synchronized void closeConnections()  {
        if (user.getName() != null) {
            Server.userMap.remove(user.getName());
        }

        if (objectOutputStream != null){
            Server.writers.remove(objectOutputStream);
        }
        if (inputStream != null){
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (outputStream != null){
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (objectInputStream != null){
            try {
                objectInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            removeFromList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

