package com.suslov.cft.chat.common;

import lombok.Getter;

@Getter
public class Message {
    private Type type;
    private String userName;
    private String text;

    public Message() {
    }

    public Message(Type type, String userName) {
        this(type, userName, userName);
    }

    public Message(Type type, String userName, String text) {
        this.type = type;
        this.userName = userName;
        this.text = text;
    }

    public enum Type {
        USER_NAME, WELCOME_USER, PARTING_USER, USER_TEXT, ERROR_NAME
    }
}