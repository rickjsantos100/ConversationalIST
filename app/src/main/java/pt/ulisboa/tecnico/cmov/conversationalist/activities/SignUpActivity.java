package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivitySignUpBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.FirebaseManager;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseManager = new FirebaseManager(getApplicationContext());
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners() {
        binding.textSignIn.setOnClickListener(v -> {
            onBackPressed();
        });
        binding.buttonSignUp.setOnClickListener(v -> {
            signUp();
        });
    }

    private void signUp() {
        loading(true);
        FirebaseManager fb = new FirebaseManager(this);
        String username = binding.inputUsername.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();

        if (!username.isEmpty() && !password.isEmpty() && firebaseManager.getUserById(username) != null) {
            Toast.makeText(getApplicationContext(), "Username already exists, try with a different username.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!username.isEmpty() && !password.isEmpty()) {
            fb.createUser(username, password).addOnCompleteListener(v -> {
                loading(false);
                if (v.isSuccessful()) {
                    onBackPressed();
                }
            });
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }

}