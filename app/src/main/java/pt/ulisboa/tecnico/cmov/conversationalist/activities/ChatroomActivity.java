package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.adapters.ChatroomAdapter;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityChatroomBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;

public class ChatroomActivity extends AppCompatActivity {


    private ActivityChatroomBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatroomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        loading(false);

        setListeners();
        getChatrooms();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void getChatrooms() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("chatrooms").get().addOnCompleteListener(task -> {
            loading(false);
            if (task.isSuccessful() && task.getResult() != null) {
                List<Chatroom> chatrooms = new ArrayList<>();
                //TODO: iterate over the rooms that the user already belongs and remove those from here
                for (QueryDocumentSnapshot q : task.getResult()) {
                    Chatroom chatroom = new Chatroom();
                    chatroom.name = q.getString("name");
                    chatroom.region = q.getString("region");
                    chatrooms.add(chatroom);
                    Log.d("potato", chatroom.name);
                }
                if (chatrooms.size() > 0) {
                    ChatroomAdapter chatroomAdapter = new ChatroomAdapter(chatrooms);
                    binding.chatroomsRecycleView.setAdapter(chatroomAdapter);
                    binding.chatroomsRecycleView.setVisibility(View.VISIBLE);
                    Log.d("potato", "im right here bro");
                } else {
                    showErrorMessage();
                }
            } else {
                showErrorMessage();
            }
        });
    }

    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "No chatroom available"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);

        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}