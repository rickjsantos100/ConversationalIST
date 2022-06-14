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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.adapters.ChatroomAdapter;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.listeners.ChatroomListener;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Message;
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

        String username = preferenceManager.getUser().getUsername();

        database.collection("chats").whereEqualTo("sender", username).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                loading(false);
                List<Message> messages = new ArrayList<>();
                List<Chatroom> chatrooms = new ArrayList<>();

                for (QueryDocumentSnapshot q : task.getResult()) {
                    Message message = new Message();
                    message.chatroom = q.getString("chatroom");
                    messages.add(message);
                }

                for (Message m : messages) {
                    Chatroom chatroom = new Chatroom(m.chatroom);
                    if (!chatrooms.contains(chatroom)) {
                        chatrooms.add(chatroom);
                    }
                }

                if (chatrooms.size() > 0) {
                    ChatroomAdapter chatroomAdapter = new ChatroomAdapter(chatrooms, this);
                    binding.chatroomsRecycleView.setAdapter(chatroomAdapter);
                    binding.chatroomsRecycleView.setVisibility(View.VISIBLE);
                }
            }
        });
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