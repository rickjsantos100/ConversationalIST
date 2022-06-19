package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.adapters.ChatroomAdapter;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.listeners.ChatroomListener;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;


public class MainActivity extends AppCompatActivity implements ChatroomListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loadUserDetails();
        setListeners();
        getUserChatrooms();

        getFSMToken();
    }

    private void getFSMToken() {
        String TAG = "FIREBASE";
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        Log.w(TAG, "Fetching FCM token" + task.getResult());
                        Toast.makeText(MainActivity.this, token, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setListeners() {
        binding.newRoom.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ChatroomActivity.class)));
        binding.imgsignOut.setOnClickListener(v -> {
            signOut();
        });
        binding.imgToggleTheme.setOnClickListener(v -> {
            if (preferenceManager.getInt("night") == 1) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            preferenceManager.putInt("night", AppCompatDelegate.getDefaultNightMode());
        });
        binding.imgOpenMaps.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), MapsActivity.class));
        });
    }

    private void loadUserDetails() {
        binding.textName.setText(preferenceManager.getUser().getUsername());
    }

    private void signOut() {
        Toast.makeText(getApplicationContext(), "Signing out...", Toast.LENGTH_SHORT).show();
        preferenceManager.clear();
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        finish();
    }

    private void getUserChatrooms() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        List<String> userChatroomsId = preferenceManager.getUser().getChatroomsRefs();
        List<Chatroom> userChatrooms = new ArrayList<>();

        if (userChatroomsId.size() > 0) {
            database.collection("chatrooms")
                    .whereIn(FieldPath.documentId(), userChatroomsId)
                    .get()
                    .addOnCompleteListener(task -> {
                        loading(false);
                        QuerySnapshot result = task.getResult();
                        for (QueryDocumentSnapshot snapshot : result) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                Chatroom chatroom = new Chatroom(snapshot.getId(), snapshot.getString("region"));
                                userChatrooms.add(chatroom);
                            }
                        }

                        if (userChatrooms.size() > 0) {
                            ChatroomAdapter chatroomAdapter = new ChatroomAdapter(userChatrooms, this);
                            binding.chatroomsRecycleView.setAdapter(chatroomAdapter);
                            binding.chatroomsRecycleView.setVisibility(View.VISIBLE);
                        }
                    });
        } else {
//            TODO: Add message to the screen saying the user is not in any room
            loading(false);
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);

        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onChatroomClicked(Chatroom chatroom) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra("chatroom", chatroom);
        startActivity(intent);
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        CollectionReference usersCollection = db.collection("users");
//
//        EditText usernameField = findViewById(R.id.username);
//
//        Button btnLogin = findViewById(R.id.btn_login);
//
//        Activity context = this;
//
//        btnLogin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                // Add document data with auto-generated id.
//                username = usernameField.getText().toString();
//
//                if (username.equals("")) {
//                    return;
//                }
//
//
//                usersCollection.document(username).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                        if (task.isSuccessful() && Objects.requireNonNull(task.getResult()).exists()) {
//                            Log.w(TAG, "Document already exists");
//                        } else {
//                            usersCollection.document(username).set(new User(System.currentTimeMillis())).addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void unused) {
//                                    if (task.isSuccessful()) {
//                                        DocumentSnapshot document = task.getResult();
//                                        assert document != null;
//                                        Log.d(TAG, document.getId() + " => " + document.get("lastAccess"));
//                                        Intent intent = new Intent(context, HomeActivity.class);
//                                        intent.putExtra(USER_OBJECT, document.getId());
//                                        startActivity(intent);
//
//                                    }
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.w(TAG, "Error adding document", e);
//                                }
//                            });
//                        }
//                    }
//                });
//            }
//        });
//        Read data example
//        db.collection("users")
//                .get()
//                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//                    @Override
//                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                        if (task.isSuccessful()) {
//                            for (QueryDocumentSnapshot document : task.getResult()) {
//                                Log.d(TAG, document.getId() + " => " + document.getData());
//                            }
//                        } else {
//                            Log.w(TAG, "Error getting documents.", task.getException());
//                        }
//                    }
//                });
    //}
}