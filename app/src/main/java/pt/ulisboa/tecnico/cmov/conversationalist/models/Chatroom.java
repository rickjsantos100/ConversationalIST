package pt.ulisboa.tecnico.cmov.conversationalist.models;

import java.io.Serializable;

public class Chatroom implements Serializable {

    public String name;
    public Boolean isPrivate;
    public String adminRef;
    public Float radius;
    public GeoCage geofence;

    public Chatroom() {
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
}
