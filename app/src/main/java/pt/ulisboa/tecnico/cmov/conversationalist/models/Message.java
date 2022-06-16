package pt.ulisboa.tecnico.cmov.conversationalist.models;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    public String senderId;
    public String media;
    public String value;
    public Date timestamp;
    public String chatroom;

    public Message(String senderId, String media, String value, Date timestamp, String chatroom) {
        this.senderId = senderId;
        this.media = media;
        this.value = value;
        this.timestamp = timestamp;
        this.chatroom = chatroom;
    }

    public Message() {
        this.senderId = "";
        this.media = null;
        this.value = "";
        this.timestamp = new Date();
        this.chatroom = "";
    }
}
