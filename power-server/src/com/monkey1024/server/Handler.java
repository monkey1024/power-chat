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
    private ObjectInputStream inputStream;
    private OutputStream os;
    private ObjectOutputStream outputStream;
    private InputStream is;

    public Handler(Socket socket) throws IOException {
        this.socket = socket;
    }
    
    @Override
    public void run() {
        try {
            //获取输入流
            is = socket.getInputStream();
            inputStream = new ObjectInputStream(is);

            //获取输出流
            os = socket.getOutputStream();
            outputStream = new ObjectOutputStream(os);

            //读取客户端传入的数据
            Message firstMessage = (Message) inputStream.readObject();
            checkDuplicateUsername(firstMessage);

            sendNotification(firstMessage);
            addToList();

            while (socket.isConnected()) {
                Message message = (Message) inputStream.readObject();
                if (message != null) {
                    System.out.println(message.getType() +  "-"  + message.getName() + ":" + message.getMsg());
                    switch (message.getType()) {
                        case USER:
                        case VOICE:
                            write(message,outputStream);
                            break;
                        case CONNECTED:
                            addToList();
                            break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (DuplicateUsernameException e){
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            closeConnections();
        }
    }

    /*
        检查用户名是否重复
     */
    private synchronized void checkDuplicateUsername(Message message) throws DuplicateUsernameException {
        if (!Server.names.containsKey(message.getName())) {
            this.name = message.getName();
            user = new User(message.getName(),message.getPicture());
            //将user对象加入到集合中
            Server.names.put(name, user);
            System.out.println(name + "已加入群聊");
        } else {
            throw new DuplicateUsernameException(message.getName() + " 用户名重复");
        }
    }

    /*
        发送通知消息
     */
    private Message sendNotification(Message message) throws IOException {
        Message msg = new Message();
        msg.setMsg("开始聊天啦");
        msg.setType(MessageType.NOTIFICATION);
        msg.setName(message.getName());
        write(msg,outputStream);
        return msg;
    }

    /*
        从列表中移除退出的用户
     */
    private Message removeFromList() throws IOException {
        Message msg = new Message();
        msg.setMsg("has left the chat.");
        msg.setType(MessageType.DISCONNECTED);
        msg.setName("SERVER");
        msg.setUserList(new ArrayList<>(Server.names.values()));
        write(msg,outputStream);
        return msg;
    }

    /*
        新登录的用户进行连接
     */
    private Message addToList() throws IOException {
        Message msg = new Message();
        msg.setMsg("欢迎来到聊天室");
        msg.setType(MessageType.CONNECTED);
        msg.setName("SERVER");
        write(msg,outputStream);
        return msg;
    }

    private void write(Message msg,ObjectOutputStream writer) throws IOException {
            msg.setUserList(new ArrayList<>(Server.names.values()));
            writer.writeObject(msg);
            writer.reset();
    }

    /*
        退出
     */
    private synchronized void closeConnections()  {
        if (name != null) {
            Server.names.remove(name);
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
        if (inputStream != null){
            try {
                inputStream.close();
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
