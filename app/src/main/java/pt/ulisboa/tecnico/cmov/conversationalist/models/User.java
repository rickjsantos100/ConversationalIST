package pt.ulisboa.tecnico.cmov.conversationalist.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    //    Hidden firestore id field
    @DocumentId
    private String username;
    private Long lastAccess;

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
