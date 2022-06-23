package pt.ulisboa.tecnico.cmov.conversationalist.utilities;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.models.GeoCage;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Message;
import pt.ulisboa.tecnico.cmov.conversationalist.models.User;

public class FirebaseManager {
    private static final String TAG = "FirebaseManager";

    private final PreferenceManager preferenceManager;
    private final FirebaseFirestore database;
    private final Context context;

    public FirebaseManager(Context context) {
        this.context = context;
        preferenceManager = new PreferenceManager(context);
        database = FirebaseFirestore.getInstance();
    }


    public Task<DocumentSnapshot> getUserFromContext() throws NullPointerException {
        User user = preferenceManager.getUser();
//        user can be null hence the nullpointerexcption
        return getUserById(user.username);

    }

    public Task<DocumentSnapshot> getUserById(String username) {
        return database.collection("users").document(username).get().addOnCompleteListener(task -> {

            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    User firestoreUser = document.toObject(User.class);
                    firestoreUser.username = username;
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
                        firestoreUser.username = username;
                        preferenceManager.setUser(firestoreUser);
                    }
                } else {
                    preferenceManager.setUser(null);
                }
            }
        });
    }

    public Task<Void> createUser(String username, String password) {
        User newUser = new User();
        newUser.username = username;
        newUser.password = password;
        newUser.chatroomsRefs = new ArrayList<>();
        newUser.geofencesRefs = new ArrayList<>();

        return database.collection("users").document(username).set(newUser).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this.context, "Successfully Signed Up", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void leaveChatroom(String chatroomId) {
        User user = preferenceManager.getUser();
//      Update the database user
        DocumentReference docRef = database.collection("users").document(user.username);
        docRef.update("chatroomsRefs", FieldValue.arrayRemove(chatroomId));
//      Update the state user
        List<Chatroom> userChatrooms = user.chatroomsRefs;
        userChatrooms.remove(new Chatroom(chatroomId));
        List<GeoCage> userGeofences = user.geofencesRefs;
        docRef.update("geofencesRefs", userGeofences);
        user.chatroomsRefs = userChatrooms;
        preferenceManager.setUser(user);
    }

    public void joinChatroom(Chatroom chatroom) {
        User user = preferenceManager.getUser();
//       Update the database user
        DocumentReference docRef = database.collection("users").document(user.username);
        docRef.update("chatroomsRefs", FieldValue.arrayUnion(chatroom));
//        Update the state user
        List<Chatroom> userChatrooms = user.chatroomsRefs;
        userChatrooms.add(chatroom);
        user.chatroomsRefs = userChatrooms;
        preferenceManager.setUser(user);
    }

    public Task<Void> createGeofence(GeoCage geofence) {
        return database.collection("geofences").document(geofence.chatroomID).set(geofence).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = preferenceManager.getUser();
                DocumentReference docRef = database.collection("users").document(user.username);

                user.geofencesRefs.add(geofence);
                docRef.update("geofencesRefs", user.geofencesRefs);
                preferenceManager.setUser(user);
            }
        });
    }

    public Task<Void> updateChatroomGeofence(GeoCage geofence, Chatroom chatroom) {
        return database.collection("chatrooms").document(chatroom.name).update("geofence", geofence);
    }


    public Task<Void> createChatroom(Chatroom chatroom) {
        return database.collection("chatrooms").document(chatroom.name).set(chatroom).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                User user = preferenceManager.getUser();
                DocumentReference docRef = database.collection("users").document(user.username);

                user.chatroomsRefs.add(chatroom);
                docRef.update("chatroomsRefs", user.chatroomsRefs);
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

    public void updateToken(String token) {
        DocumentReference reference = database.collection("users").document(preferenceManager.getUser().username);
        reference.update("fcm", token).addOnSuccessListener(v -> {
            preferenceManager.getUser().fcm = token;
            Log.d(TAG, "Updated FCM");
        });
    }

    public void clearToken() {
        DocumentReference reference = database.collection("users").document(preferenceManager.getUser().username);
        HashMap<String, Object> updates = new HashMap<>();
        updates.put("fcm", FieldValue.delete());
        reference.update(updates);
    }

    public Task<QuerySnapshot> getUsersOfRoom(String room) {
        return database.collection("users").whereArrayContains("chatroomsRefs", room).get();
    }

    public Task<DocumentSnapshot> getUserInfoById(String username) {
        return database.collection("users").document(username).get();
    }

    public Task<Void> updateMessage(Message message, String language, String newTranslation) {
        HashMap<String, Object> updates = new HashMap<>();
        message.translations.put(language, newTranslation);
        updates.put("translations", message.translations);
        return database.collection("chats").document(message.id).update(updates);
    }
}
