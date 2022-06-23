package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;

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
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.FirebaseManager;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;


public class MainActivity extends BaseActivity implements ChatroomListener {

    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        firebaseManager = new FirebaseManager(getApplicationContext());
        loadUserDetails();
        setListeners();
        getUserChatrooms();
        getToken();
        handleIntent(getIntent());
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserChatrooms();
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            if (preferenceManager.getUser().getUsername() != null) {
                String chatroomId = appLinkData.getQueryParameter("id");
                firebaseManager.getChatroom(chatroomId).addOnSuccessListener(documentSnapshot -> {
                    Chatroom chatroom = documentSnapshot.toObject(Chatroom.class);
                    navigateToChatroom(chatroom);
                });
            } else {
                Intent signInIntent = new Intent(getApplicationContext(), SignInActivity.class);
                signInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(signInIntent);
            }
        }
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

    private void updateToken(String token) {
        firebaseManager.updateToken(token);
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void signOut() {
        Toast.makeText(getApplicationContext(), "Signing out...", Toast.LENGTH_SHORT).show();
        firebaseManager.clearToken();
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
            int lowerBound = 0;
            int upperBound = userChatroomsId.size();
            if (userChatroomsId.size() > 10) {
                for (int i = 0; i < userChatroomsId.size() / 10; i++) {
                    lowerBound = i * 10;
                    upperBound = lowerBound + 10;
                    fetchUserChatroomsByBoundary(database, userChatroomsId, userChatrooms, lowerBound, upperBound);
                }

                if (userChatroomsId.size() % 10 != 0) {
                    lowerBound = userChatroomsId.size() / 10 * 10;
                    upperBound = userChatroomsId.size();
                    fetchUserChatroomsByBoundary(database, userChatroomsId, userChatrooms, lowerBound, upperBound);
                }
            } else {
                fetchUserChatroomsByBoundary(database, userChatroomsId, userChatrooms, lowerBound, upperBound);
            }
        } else {
//            TODO: Add message to the screen saying the user is not in any room
            loading(false);
        }
    }

    private void fetchUserChatroomsByBoundary(FirebaseFirestore database, List<String> userChatroomsId, List<Chatroom> userChatrooms, int lowerBound, int upperBound) {
        database.collection("chatrooms")
                .whereIn(FieldPath.documentId(), userChatroomsId.subList(lowerBound, upperBound))
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
        navigateToChatroom(chatroom);
    }

    private void navigateToChatroom(Chatroom chatroom) {
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