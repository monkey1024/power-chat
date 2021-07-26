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

            Message firstMessage = (Message) objectInputStream.readObject();
            if (!checkDuplicateUsername(firstMessage)){
                return;
            }
            Server.writers.add(objectOutputStream);
            sendNotification(firstMessage);
            addToList();

            while (socket.isConnected()) {
                Message message = (Message) objectInputStream.readObject();
                if (message != null) {
                    switch (message.getType()) {
                        case TEXT:
                            write(message);
                            break;
                        case JOINED:
                            addToList();
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
        if (!Server.names.containsKey(message.getName())) {
            user = new User();
            user.setName(message.getName());
            user.setPicture(message.getPicture());
            Server.names.put(message.getName(), user);
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

    private void sendNotification(Message message) throws IOException {
        Message msg = new Message();
        msg.setMsg("加入群聊");
        msg.setType(MessageType.NOTIFICATION);
        msg.setName(message.getName());
        msg.setPicture(message.getPicture());
        write(msg);
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
        msg.setOnlineUsers(new ArrayList<>(Server.names.values()));
        write(msg);
    }

    /**
     *  新用户加入
     * @return
     * @throws IOException
     */
    private void addToList() throws IOException {
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
        for (ObjectOutputStream writer : Server.writers) {
            msg.setOnlineUsers(new ArrayList<>(Server.names.values()));
            writer.writeObject(msg);
            writer.reset();
        }
    }

    /**
     *  关闭链接
     */
    private synchronized void closeConnections()  {
        if (user.getName() != null) {
            Server.names.remove(user.getName());
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

