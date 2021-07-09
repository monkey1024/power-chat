package com.monkey1024.bean;

import java.io.Serializable;
import java.util.List;

/*
    消息
 */
public class Message implements Serializable {
    private static final long serialVersionUID = 1;
    private String name;
    private MessageType type;
    private String msg;
    private List<User> userList;
    private byte[] voiceMsg;
    private String picture;

    public byte[] getVoiceMsg() {
        return voiceMsg;
    }

    public String getPicture() {
        return picture;
    }

    public Message() {
    }

    public List<User> getUserList() {
        return userList;
    }

    public void setUserList(List<User> userList) {
        this.userList = userList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMsg() {

        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }


    public void setPicture(String picture) {
        this.picture = picture;
    }

    public void setVoiceMsg(byte[] voiceMsg) {
        this.voiceMsg = voiceMsg;
    }
}
