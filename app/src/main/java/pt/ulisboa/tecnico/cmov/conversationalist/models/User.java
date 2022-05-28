package pt.ulisboa.tecnico.cmov.conversationalist.models;

import com.google.firebase.firestore.DocumentId;

public class User {

    //    Hidden firestore id field
    @DocumentId
    private String username;
    private Long lastAccess;
    private Chatroom[] chatrooms;

    public User(Long lastAccess, Chatroom[] chatrooms) {
        this.lastAccess = lastAccess;
        this.chatrooms = chatrooms;
    }

    public User(Long lastAccess) {
        this.lastAccess = lastAccess;
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
