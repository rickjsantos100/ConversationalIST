package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivitySignInBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.models.User;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if(preferenceManager.getUser() == null) {
            setListeners();
        } else {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

    private void setListeners() {
        binding.buttonSignIn.setOnClickListener(v -> {
            String username = binding.inputUsername.getText().toString().trim();
            if (username.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Enter a username", Toast.LENGTH_SHORT).show();
            } else {
                loading(true);
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection("users").document(username).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() == null) {
//                        TODO: Evaluate necessity, should this page just be composed of a nick name insertion
                        loading(false);
                        Toast.makeText(getApplicationContext(), "Username already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        User newUser = new User(System.currentTimeMillis());
                        database.collection("users").document(username).set(newUser).addOnCompleteListener(tt -> {
                            if (tt.isSuccessful()) {
                                // we got a new user and we can start it
                                DocumentSnapshot document = task.getResult();
                                assert document != null;
                                newUser.setUsername(username);
                                newUser.setChatroomsRefs(new ArrayList<String>());
                                preferenceManager.setUser(newUser);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                loading(false);
                                Toast.makeText(getApplicationContext(), "Username already exists", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                });
            }
        });
    }

    private void loading(boolean isLoading) {
        if (isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignIn.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}