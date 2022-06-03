package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.conversationalist.adapters.ChatAdapter;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityChatBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Message;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private Chatroom chatroom;
    private List<Message> messages;

    private ChatAdapter chatAdapter;
    private final EventListener<QuerySnapshot> eventListener = (value, err) -> {
        if (err != null) {
            return;
        }
        if (value != null) {
            int count = messages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    Message message = new Message();
                    message.senderId = documentChange.getDocument().getString("sender");
                    message.chatroom = documentChange.getDocument().getString("chatroom");
                    message.value = documentChange.getDocument().getString("value");
                    message.timestamp = documentChange.getDocument().getDate("timestamp");
                    messages.add(message);
                }
            }
            Collections.sort(messages, (o, q) -> o.timestamp.compareTo(q.timestamp));
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
    private PreferenceManager preferenceManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        messages = new ArrayList<Message>();

        chatAdapter = new ChatAdapter(
                messages,
                preferenceManager.getString("username")
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        db = FirebaseFirestore.getInstance();
        setListeners();
        loadRoomInfo();
        listenMessages();
    }

    private void loadRoomInfo() {
        chatroom = (Chatroom) getIntent().getSerializableExtra("chatroom");
        binding.textName.setText(chatroom.name);
    }

    private void listenMessages() {
        db.collection("chats").whereEqualTo("chatroom", chatroom.name).addSnapshotListener(eventListener);
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put("sender", preferenceManager.getString("username"));
        message.put("chatroom", chatroom.name);
        message.put("value", binding.inputMessage.getText().toString());
        message.put("timestamp", new Date());

        db.collection("chats").add(message);

        binding.inputMessage.setText(null);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private String parseDate(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}