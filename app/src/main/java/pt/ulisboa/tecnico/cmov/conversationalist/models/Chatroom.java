package pt.ulisboa.tecnico.cmov.conversationalist.models;

import com.google.firebase.firestore.DocumentId;

import java.util.List;

public class Chatroom {
    @DocumentId
    private String name;
    private boolean isPrivate;
    private User admin;
    private String region;
    private List<User> users;
    private List<Message> messages;

}
