package com.monkey1024.server;

import com.monkey1024.bean.User;
import com.monkey1024.constant.Constant;

import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {

    public static final HashMap<String, User> userMap = new HashMap<>();
    public static HashSet<ObjectOutputStream> writers = new HashSet<>();
    public static ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(16, 32, 1, TimeUnit.MINUTES, new ArrayBlockingQueue<>(20));

    public static void main(String[] args) {

        try(ServerSocket listener = new ServerSocket(Constant.PORT)) {
            while (true) {
                poolExecutor.execute(new Handler(listener.accept()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
