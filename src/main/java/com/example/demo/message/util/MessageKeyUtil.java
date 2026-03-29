package com.example.demo.message.util;

public class MessageKeyUtil {

    public static String build(String messageId, String lang, String action) {
        return messageId + "|" + lang + "|" + action;
    }
}