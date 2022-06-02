package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.R;
import pt.ulisboa.tecnico.cmov.conversationalist.adapters.ChatAdapter;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityChatBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Message;
import pt.ulisboa.tecnico.cmov.conversationalist.models.User;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private User receiver;
    private List<Message> messages;
    private ChatAdapter chatAdapter;

    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        setListeners();
        init();
    }

    private void init() {
        messages = new ArrayList<>();
        chatAdapter = new ChatAdapter(messages);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        db = FirebaseFirestore.getInstance();
    }

    private void listenMessages() {

    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put("message", binding.inputMessage.getText().toString());
        //messages.add(message);
        binding.inputMessage.setText(null);
    }

    private void setListeners() {
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }
}