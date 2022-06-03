package pt.ulisboa.tecnico.cmov.conversationalist.models;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
    public String senderId;
    public MediaType media;
    public String value;
    public Date timestamp;
    public String chatroom;

    public Message(String senderId, MediaType media, String value, Date timestamp, String chatroom) {
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

    public String getChatroom() {
        return this.chatroom;
    }
}
