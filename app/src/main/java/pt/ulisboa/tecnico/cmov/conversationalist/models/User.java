package pt.ulisboa.tecnico.cmov.conversationalist.models;

import com.google.firebase.firestore.DocumentId;

import java.util.ArrayList;
import java.util.List;

public class User {
    private final List<Chatroom> chatrooms;
    //    Hidden firestore id field
    @DocumentId
    private String username;
    private Long lastAccess;

    public User(Long lastAccess, List<Chatroom> chatrooms) {
        this.lastAccess = lastAccess;
        this.chatrooms = chatrooms;
    }

    public User(Long lastAccess) {
        this.lastAccess = lastAccess;
        this.chatrooms = new ArrayList<>();
    }

    public Long getLastAccess() {
        return lastAccess;
    }

    public void setLastAccess(Long lastAccess) {
        this.lastAccess = lastAccess;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
