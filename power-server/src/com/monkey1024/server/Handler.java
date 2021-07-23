package com.monkey1024.server;

import com.monkey1024.bean.Message;
import com.monkey1024.bean.MessageType;
import com.monkey1024.bean.User;
import com.monkey1024.exception.DuplicateUsernameException;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class Handler implements Runnable {
    private String name;
    private Socket socket;
    private User user;
    private ObjectInputStream input;
    private OutputStream os;
    private ObjectOutputStream output;
    private InputStream is;

    public Handler(Socket socket) throws IOException {
        this.socket = socket;
    }

    public void run() {
        try {
            is = socket.getInputStream();
            input = new ObjectInputStream(is);
            os = socket.getOutputStream();
            output = new ObjectOutputStream(os);

            Message firstMessage = (Message) input.readObject();
            checkDuplicateUsername(firstMessage);
            Server.writers.add(output);
            sendNotification(firstMessage);
            addToList();

            while (socket.isConnected()) {
                Message inputmsg = (Message) input.readObject();
                if (inputmsg != null) {
                    switch (inputmsg.getType()) {
                        case USER:
                            write(inputmsg);
                            break;
                        case CONNECTED:
                            addToList();
                            break;
                    }
                }
            }
        } catch (SocketException socketException) {
        } catch (DuplicateUsernameException duplicateException){
        } catch (Exception e){
        } finally {
            closeConnections();
        }
    }

    private synchronized void checkDuplicateUsername(Message firstMessage) throws DuplicateUsernameException {
        if (!Server.names.containsKey(firstMessage.getName())) {
            this.name = firstMessage.getName();
            user = new User();
            user.setName(firstMessage.getName());
            user.setPicture(firstMessage.getPicture());

            //users.add(user);
            Server.names.put(name, user);

        } else {
            throw new DuplicateUsernameException(firstMessage.getName() + " is already connected");
        }
    }

    private Message sendNotification(Message firstMessage) throws IOException {
        Message msg = new Message();
        msg.setMsg("加入群聊");
        msg.setType(MessageType.NOTIFICATION);
        msg.setName(firstMessage.getName());
        msg.setPicture(firstMessage.getPicture());
        write(msg);
        return msg;
    }


    private Message removeFromList() throws IOException {
        Message msg = new Message();
        msg.setMsg("离开了聊天");
        msg.setType(MessageType.DISCONNECTED);
        msg.setName("SERVER");
        msg.setOnlineUsers(new ArrayList<>(Server.names.values()));
        write(msg);
        return msg;
    }

    /*
     * For displaying that a user has joined the server
     */
    private Message addToList() throws IOException {
        Message msg = new Message();
        msg.setMsg("欢迎加入聊天");
        msg.setType(MessageType.CONNECTED);
        msg.setName("SERVER");
        write(msg);
        return msg;
    }

    /**
     * 向监听器发送消息
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

    /*
     * Once a user has been disconnected, we close the open connections and remove the writers
     */
    private synchronized void closeConnections()  {
        if (name != null) {
            Server.names.remove(name);
        }
        if (user != null){
            Server.users.remove(user);
        }
        if (output != null){
            Server.writers.remove(output);
        }
        if (is != null){
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (os != null){
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (input != null){
            try {
                input.close();
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

