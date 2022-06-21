package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.QuerySnapshot;

import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivitySignInBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.User;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.FirebaseManager;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        firebaseManager = new FirebaseManager(getApplicationContext());

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        User user = preferenceManager.getUser();
        if (user == null) {
            setListeners();
        } else {
//            TODO: update the stored user with the most recent user information
            firebaseManager.getUserById(user.getUsername()).addOnCompleteListener(task -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });
        }
    }

    private void setListeners() {
        binding.textSignUp.setOnClickListener(v -> {
            startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
        });
        binding.buttonSignIn.setOnClickListener(v -> {
            String username = binding.inputUsername.getText().toString().trim();
            String password = binding.inputPassword.getText().toString().trim();
            if (username.isEmpty() || password.isEmpty()) {
                //                                TODO: change below line to use translations
                Toast.makeText(getApplicationContext(), "Confirm Inputs", Toast.LENGTH_SHORT).show();
            } else {
                loading(true);
                firebaseManager.getUserIfPasswordMatches(username, password).addOnCompleteListener(task -> {
                    loading(false);
                    if (task.isSuccessful()) {
                        QuerySnapshot document = task.getResult();
                        if (document.getDocuments().size() > 0) {
                            Log.d("POTATO", document.getDocuments().get(0).getId());
                            navigateToMainActivity();
                        } else {
                            Toast.makeText(getApplicationContext(), "Something wrong happened while creating the user", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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