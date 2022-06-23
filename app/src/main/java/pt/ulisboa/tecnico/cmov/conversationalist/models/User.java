package pt.ulisboa.tecnico.cmov.conversationalist.models;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {

    //    Hidden firestore id field
    @DocumentId
    public String username;
    public Long lastAccess;
    public List<Chatroom> chatroomsRefs;
    public List<GeoCage> geofencesRefs;
    public String password;
    public String fcm;

    public User() {
        this.username = "";
        this.lastAccess = 0L;
        this.chatroomsRefs = new ArrayList<>();
        this.geofencesRefs = new ArrayList<>();
        this.password = "";
        this.fcm = "";
    }

}
