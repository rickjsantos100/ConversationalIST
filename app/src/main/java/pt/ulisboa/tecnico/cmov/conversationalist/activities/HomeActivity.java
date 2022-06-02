package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import pt.ulisboa.tecnico.cmov.conversationalist.R;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "Home Activity";
    private static final String USER_OBJECT = "user_object";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        String user = (String) getIntent().getSerializableExtra(USER_OBJECT);
        if (user != null) {
            Log.d(TAG, user);

        }
    }
}