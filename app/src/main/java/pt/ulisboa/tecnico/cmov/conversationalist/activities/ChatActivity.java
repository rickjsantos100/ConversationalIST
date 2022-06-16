package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import pt.ulisboa.tecnico.cmov.conversationalist.R;
import pt.ulisboa.tecnico.cmov.conversationalist.adapters.ChatAdapter;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityChatBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Message;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;

public class ChatActivity extends AppCompatActivity {
    public static final int PICKFILE_RESULT_CODE = 1;

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
        messages = new ArrayList<>();

        chatAdapter = new ChatAdapter(
                messages,
                preferenceManager.getUser().getUsername()
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
        if (!binding.inputMessage.getText().toString().matches("") && !binding.inputMessage.getText().toString().trim().isEmpty()) {
            HashMap<String, Object> message = new HashMap<>();
            message.put("sender", preferenceManager.getUser().getUsername());
            message.put("chatroom", chatroom.name);
            message.put("value", binding.inputMessage.getText().toString());
            message.put("timestamp", new Date());

            db.collection("chats").add(message);
        }
        binding.inputMessage.setText(null);
    }

    private void chooseFile() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        // deprecated but better :)
        startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("POTATO", "im in " + resultCode + " " + RESULT_OK);

        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();

                    //storage on firebase
                    // Create a storage reference from our app
                    FirebaseStorage storage = FirebaseStorage.getInstance("gs://converstaionalist.appspot.com");
                    StorageReference storageRef = storage.getReference();

                    // Create file metadata including the content type
                    StorageMetadata metadata = new StorageMetadata.Builder()
                            .setContentType("image/jpg")
                            .build();
                    UploadTask uploadTask = storageRef.child("images/" + uri.getLastPathSegment()).putFile(uri, metadata);

                    // Register observers to listen for when the download is done or if it fails
                    uploadTask.addOnFailureListener(t -> {
                        // handle failure
                    }).addOnSuccessListener(t -> {
                        // handle success (add to chat)
                        t.getMetadata(); // contains file metadata such as size, content type
                        Log.d("POTATO", "this: " + t);
                        Log.d("POTATO", "other: " + Objects.requireNonNull(t.getMetadata()));
                    });
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void showMoreOptionsMenu(View view) {
        PopupMenu moreOptionsMenu = new PopupMenu(getApplicationContext(), view);
        MenuInflater inflater = moreOptionsMenu.getMenuInflater();
        inflater.inflate(R.menu.chat_more_options_menu, moreOptionsMenu.getMenu());
        moreOptionsMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.leaveChatroom:
                        leaveChatroom();
                        return true;
                    default:
                        return false;
                }
            }
        });
        moreOptionsMenu.show();
    }

    private void leaveChatroom() {
        DocumentReference docRef = db.collection("users").document(preferenceManager.getUser().getUsername());
        docRef.update("chatroomsRefs", FieldValue.arrayRemove(chatroom.getName()));
        List<String> userChatrooms = preferenceManager.getUser().getChatroomsRefs();
        userChatrooms.remove(chatroom.getName());
        preferenceManager.getUser().setChatroomsRefs(userChatrooms);
        onBackPressed();
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> sendMessage());
        binding.layoutAttachFile.setOnClickListener(v -> chooseFile());
    }

    private String parseDate(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}