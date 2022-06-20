package pt.ulisboa.tecnico.cmov.conversationalist.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

public class Chatroom implements Serializable {


    @DocumentId
    public String name;
    public boolean isPrivate;
    public String adminRef;
    public String region;
    public Long radius;

    public Chatroom() {
    }

    public Chatroom(String name, String region) {
        this.name = name;
        this.region = region;
    }

    public Chatroom(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Chatroom))
            return false;
        Chatroom p = (Chatroom) other;
        return p.name.equals(this.name);
    }

    public String getAdminRef() {
        return adminRef;
    }

    public void setAdminRef(String adminRef) {
        this.adminRef = adminRef;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public void setPrivate(boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
