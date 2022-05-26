package pt.ulisboa.tecnico.cmov.conversationalist;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MyActivity";
    private static final String EXTRA_OBJECT = "extra_object";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersCollection =  db.collection("users");

        EditText usernameField = findViewById(R.id.username);

        usernameField.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String username = usernameField.getText().toString();
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    usersCollection.document(username)
                            .set(new User(System.currentTimeMillis()), SetOptions.merge()).addOnSuccessListener(
                                    new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void nothing) {

                                            usersCollection.whereEqualTo("id", username).limit(1).get()
                                                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                for (QueryDocumentSnapshot document : task.getResult()) {
                                                                    Log.d(TAG, document.getId() + " => " + document.getData());
//                                                                    Intent intent = new Intent(this, HomeActivity.class);
//                                                                    intent.putExtra(EXTRA_OBJECT,document.getData() );
//                                                                    startActivity(intent);

                                                                }
                                                            } else {
                                                                Log.d(TAG, "Error getting documents: ", task.getException());
                                                            }
                                                        }
                                                    });



                                        }
                                    });

                    return true;
                }
                return false;
            }
        });

        // Create a new user with a first and last name
//        Map<String, Object> user = new HashMap<>();
//        user.put("first", "Ada");
//        user.put("last", "Lovelace");
//        user.put("born", 1815);
//
//        // Add a new document with a generated ID
//        db.collection("users")
//                .add(user)
//                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                    @Override
//                    public void onSuccess(DocumentReference documentReference) {
//                        Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        Log.w(TAG, "Error adding document", e);
//                    }
//                });


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
    }
}