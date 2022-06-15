package pt.ulisboa.tecnico.cmov.conversationalist.utilities;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.conversationalist.activities.MainActivity;
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
                    User firestoreUser = task.getResult().toObject(User.class);
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


}
