package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.R;
import pt.ulisboa.tecnico.cmov.conversationalist.adapters.ChatAdapter;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityChatBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.MediaType;
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

    private final EventListener eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = messages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Message message = new Message(null, MediaType.TEXT, documentChange.getDocument().getString("message"), 0);
                }
            }
            Collections.sort(messages, (obj1, obj2) -> obj1.compareTo(obj2));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(messages.size(), messages.size());
                binding.chatRecyclerView.smoothScrollToPosition(messages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
    };

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put("message", binding.inputMessage.getText().toString());
        messages.add(message);
        binding.inputMessage.setText(null);
    }

    private void setListeners() {
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }
}