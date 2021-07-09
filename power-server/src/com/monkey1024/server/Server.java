package com.monkey1024.server;

import com.monkey1024.constant.Constant;
import com.monkey1024.bean.User;



import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/*
    服务端代码
 */
public class Server {

    public static final HashMap<String, User> names = new HashMap<>();

    public static ThreadPoolExecutor pool = new ThreadPoolExecutor(16, 16, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));

    public static void main(String[] args) throws Exception {
        ServerSocket listener = new ServerSocket(Constant.PORT);

        try {
            while (true) {
                pool.execute(new Handler(listener.accept()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            listener.close();
        }
    }

}
