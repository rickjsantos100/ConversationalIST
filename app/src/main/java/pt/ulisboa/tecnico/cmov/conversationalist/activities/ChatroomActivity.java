package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.R;
import pt.ulisboa.tecnico.cmov.conversationalist.adapters.ChatroomAdapter;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityChatroomBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.dialogs.CreateChatroomDialogFragment;
import pt.ulisboa.tecnico.cmov.conversationalist.listeners.ChatroomListener;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.FirebaseManager;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;

public class ChatroomActivity extends AppCompatActivity implements ChatroomListener {


    private ActivityChatroomBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatroomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        firebaseManager = new FirebaseManager(getApplicationContext());
        loading(false);

        setListeners();
        getChatrooms();
    }

    private void setListeners() {

        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.buttonCreate.setOnClickListener(v -> {
            DialogFragment newFragment = new CreateChatroomDialogFragment();
            newFragment.show(getSupportFragmentManager(), "create_chatroom");
        });
    }

    private void getChatrooms() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection("chatrooms").get().addOnCompleteListener(task -> {
            loading(false);
            if (task.isSuccessful() && task.getResult() != null) {
                List<Chatroom> chatrooms = new ArrayList<>();
                //TODO: iterate over the rooms that the user already belongs and remove those from here
                //TODO: Find a way to use the @documentId notation to directly cast to list
                for (QueryDocumentSnapshot q : task.getResult()) {
                    Chatroom chatroom = new Chatroom(q.getString("name"),q.getString("region") );
                    chatrooms.add(chatroom);
                }
                if (chatrooms.size() > 0) {
                    ChatroomAdapter chatroomAdapter = new ChatroomAdapter(chatrooms, this);
                    binding.chatroomsRecycleView.setAdapter(chatroomAdapter);
                    binding.chatroomsRecycleView.setVisibility(View.VISIBLE);
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

    @Override
    public void onChatroomClicked(Chatroom chatroom) {
        firebaseManager.joinChatroom(chatroom.getName());
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra("chatroom", chatroom);
        startActivity(intent);
        finish();
    }
}


