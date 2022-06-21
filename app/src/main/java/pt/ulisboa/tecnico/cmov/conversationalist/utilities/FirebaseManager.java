package pt.ulisboa.tecnico.cmov.conversationalist.utilities;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.models.User;

public class FirebaseManager {
    private final PreferenceManager preferenceManager;
    private final FirebaseFirestore database;

    public FirebaseManager(Context context) {
        preferenceManager = new PreferenceManager(context);
        database = FirebaseFirestore.getInstance();
    }


    public Task<DocumentSnapshot> getUserFromContext() throws NullPointerException {
        User user = preferenceManager.getUser();
//        user can be null hence the nullpointerexcption
        return getUserById(user.getUsername());

    }

    public Task<DocumentSnapshot> getUserById(String username) {
        return database.collection("users").document(username).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    User firestoreUser = document.toObject(User.class);
                    firestoreUser.setUsername(username);
                    preferenceManager.setUser(firestoreUser);
                }
//                User firestoreUser = new User(task.getResult().getData()

            }

        });
    }

    public Task<QuerySnapshot> getUserIfPasswordMatches(String username, String password) {
        return database.collection("users").whereEqualTo(FieldPath.documentId(), username).whereEqualTo("password", password).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot snapshot = task.getResult();
                if (snapshot.getDocuments().size() > 0) {
                    User firestoreUser = snapshot.getDocuments().get(0).toObject(User.class);
                    if (firestoreUser != null) {
                        firestoreUser.setUsername(username);
                        preferenceManager.setUser(firestoreUser);
                    }
                } else {
                    preferenceManager.setUser(null);
                }
            }
        });
    }

    public Task<Void> createUser(String username, String password) {
        User newUser = new User(System.currentTimeMillis());
        newUser.setUsername(username);
        newUser.setPassword(password);
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
                DocumentReference docRef = database.collection("users").document(user.getUsername());

                user.getChatroomsRefs().add(chatroom.getName());
                docRef.update("chatroomsRefs", user.getChatroomsRefs());
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
