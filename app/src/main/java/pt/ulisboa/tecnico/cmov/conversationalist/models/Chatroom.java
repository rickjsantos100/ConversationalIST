package pt.ulisboa.tecnico.cmov.conversationalist.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.List;

public class Chatroom implements Serializable {
    @DocumentId
    public String name;
    public boolean isPrivate;
    public User admin;
    public String region;
    public List<Message> messages;

    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof Chatroom))
            return false;
        Chatroom p = (Chatroom) other;
        return p.name.equals(this.name);
    }
}
