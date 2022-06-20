package pt.ulisboa.tecnico.cmov.conversationalist.utilities;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.activities.MainActivity;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.models.User;

public class FirebaseManager {
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    public FirebaseManager(Context context) {
        preferenceManager = new PreferenceManager(context);
        database = FirebaseFirestore.getInstance();
    }



    public Task<DocumentSnapshot> getUserFromContext() throws NullPointerException{
        User user = preferenceManager.getUser();
//        user can be null hence the nullpointerexcption
        return getUserById(user.getUsername());

    }

    public Task<DocumentSnapshot> getUserById(String username) {
        return database.collection("users").document(username).get().addOnCompleteListener(task -> {

            if(task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if(document.exists()){
                    User firestoreUser = document.toObject(User.class);
                    firestoreUser.setUsername(username);
                    preferenceManager.setUser(firestoreUser);
                }
//                User firestoreUser = new User(task.getResult().getData()

            }

        });
    }

    public Task<Void> createUser(String username) {
        User newUser = new User(System.currentTimeMillis());
        newUser.setUsername(username);
        newUser.setChatroomsRefs(new ArrayList<String>());

        return database.collection("users").document(username).set(newUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                preferenceManager.setUser(newUser);
            }
        });
    }

    public void leaveChatroom(String chatroomId) {
        User user = preferenceManager.getUser();
//      Update the database user
        DocumentReference docRef = database.collection("users").document(user.getUsername());
        docRef.update("chatroomsRefs", FieldValue.arrayRemove(chatroomId));
//      Update the state user
        List<String> userChatrooms = user.getChatroomsRefs();
        userChatrooms.remove(chatroomId);
        user.setChatroomsRefs(userChatrooms);
        preferenceManager.setUser(user);
    }

    public void joinChatroom(String chatroomId) {
        User user = preferenceManager.getUser();
//       Update the database user
        DocumentReference docRef = database.collection("users").document(user.getUsername());
        docRef.update("chatroomsRefs", FieldValue.arrayUnion(chatroomId));
//        Update the state user
        List<String> userChatrooms = user.getChatroomsRefs();
        userChatrooms.add(chatroomId);
        user.setChatroomsRefs(userChatrooms);
        preferenceManager.setUser(user);
    }


    public Task<Void> createChatroom(Chatroom chatroom) {
        return database.collection("chatrooms").document(chatroom.getName()).set(chatroom).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = preferenceManager.getUser();
                DocumentReference docRef =  database.collection("users").document(user.getUsername());

                user.getChatroomsRefs().add(chatroom.getName().toString());
                docRef.update("chatroomsRefs" , user.getChatroomsRefs());
                preferenceManager.setUser(user);

            }
        });
    }

    public Task<DocumentSnapshot> getChatroom(String id) {
//        TODO: manipulate result to return a Task<Chatroom>
        return database.collection("chatrooms").document(id).get();
//                .addOnSuccessListener(documentSnapshot -> {
//                    return documentSnapshot.toObject(Chatroom.class);
//                });
    }


}
