package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;


public class MainActivity extends AppCompatActivity {

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
    }

    private void setListeners() {
        binding.imgsignOut.setOnClickListener(v -> signOut());
        binding.newRoom.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ChatroomActivity.class)));
    }

    private void loadUserDetails() {
        binding.textName.setText(preferenceManager.getString("username"));
    }

    private void signOut() {
        Toast.makeText(getApplicationContext(), "Signing out...", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        finish();
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