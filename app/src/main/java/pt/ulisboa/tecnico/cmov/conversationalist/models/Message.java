package pt.ulisboa.tecnico.cmov.conversationalist.models;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Message implements Serializable {
    public String id;
    public String senderId;
    public String media;
    public String value;
    public Date timestamp;
    public String chatroom;
    public Map<String, String> translations;

    public Message(String id, String senderId, String media, String value, Date timestamp, String chatroom, HashMap<String, String> translations) {
        this.id = id;
        this.senderId = senderId;
        this.media = media;
        this.value = value;
        this.timestamp = timestamp;
        this.chatroom = chatroom;
        this.translations = translations;
    }

    public Message() {
        this.id = "";
        this.senderId = "";
        this.media = null;
        this.value = "";
        this.timestamp = new Date();
        this.chatroom = "";
        this.translations = new HashMap<>();
    }
}
