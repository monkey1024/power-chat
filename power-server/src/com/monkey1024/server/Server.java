package com.monkey1024.server;

import com.monkey1024.bean.User;
import com.monkey1024.constant.Constant;

import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Server {

    public static final HashMap<String, User> names = new HashMap<>();
    public static HashSet<ObjectOutputStream> writers = new HashSet<>();

    public static void main(String[] args) {

        try(ServerSocket listener = new ServerSocket(Constant.PORT)) {
            while (true) {
                new Thread(new Handler(listener.accept())).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
