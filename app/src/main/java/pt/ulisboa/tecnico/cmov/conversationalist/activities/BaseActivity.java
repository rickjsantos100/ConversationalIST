package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;

public class BaseActivity extends AppCompatActivity {

    private DocumentReference docRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        docRef = db.collection("users")
                .document(preferenceManager.getUser().getUsername());
    }

    @Override
    protected void onPause() {
        super.onPause();
        docRef.update("online", 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        docRef.update("online", 1);
    }

}
