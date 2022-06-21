package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import pt.ulisboa.tecnico.cmov.conversationalist.R;
import pt.ulisboa.tecnico.cmov.conversationalist.adapters.ChatAdapter;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityChatBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Message;
import pt.ulisboa.tecnico.cmov.conversationalist.network.APIClient;
import pt.ulisboa.tecnico.cmov.conversationalist.network.APIService;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.FirebaseManager;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {
    public static final int PICKFILE_RESULT_CODE = 1;
    public static HashMap<String, String> remoteMsgHeaders;
    private ActivityChatBinding binding;
    private FirebaseManager firebaseManager;
    private Chatroom chatroom;
    private List<String> users;
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
                    message.media = documentChange.getDocument().getString("media");
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

    public static HashMap<String, String> getRemoteMsgHeaders() {
        if (remoteMsgHeaders == null) {
            remoteMsgHeaders = new HashMap<>();
            remoteMsgHeaders.put("Authorization", "key=AAAAOQvkomw:APA91bEwQucyF9iLxFYQxobId6YKo0s9YnwxSWHwsFKz3BE10x4Y1rzgg881vqPhVojCSGUA9zHiczimSK0TIJpI4AgrpL1JD75t1drUcNBTxqZkFPzqmRsIuLE0sGytGOsOKFfQS-Kj");
            remoteMsgHeaders.put("Content-Type", "application/json");
        }
        return remoteMsgHeaders;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        firebaseManager = new FirebaseManager(getApplicationContext());
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        getUsersOfRoom();
    }

    private void loadRoomInfo() {
        chatroom = (Chatroom) getIntent().getSerializableExtra("chatroom");
        binding.textName.setText(chatroom.name);
    }

    private void getUsersOfRoom() {
        users = new ArrayList<>();
        firebaseManager.getUsersOfRoom(chatroom.name).addOnCompleteListener(v -> {
            if (v.isSuccessful()) {
                for (DocumentSnapshot d : v.getResult().getDocuments()) {
                    if (!d.getId().equals(preferenceManager.getUser().getUsername())) {
                        users.add(d.getId());
                    }
                }
            }
        });
    }

    private void listenMessages() {
        db.collection("chats").whereEqualTo("chatroom", chatroom.name).addSnapshotListener(eventListener);
    }

    private void sendMessage(String type, String content) {
        if (!content.matches("") && !content.trim().isEmpty()) {
            HashMap<String, Object> message = new HashMap<>();
            message.put("sender", preferenceManager.getUser().getUsername());
            message.put("chatroom", chatroom.name);
            message.put("media", type);
            message.put("value", content);
            message.put("timestamp", new Date());

            db.collection("chats").add(message);
        }

        if (!users.isEmpty()) {
            for (String user : users) {
                firebaseManager.getUserInfoById(user).addOnCompleteListener(v -> {
                    if (v.isSuccessful()) {
                        try {
                            Long isOnline = v.getResult().getLong("online");
                            if (isOnline != null && isOnline == 1) {
                                return;
                            }
                            JSONArray tokens = new JSONArray();
                            String fcm = v.getResult().getString("fcm");
                            tokens.put(fcm);

                            JSONObject data = new JSONObject();
                            data.put("username", preferenceManager.getUser().getUsername());
                            data.put("chatroom", chatroom.name);
                            data.put("fcm", preferenceManager.getUser().getFCM());
                            data.put("message", binding.inputMessage.getText().toString());

                            JSONObject body = new JSONObject();
                            body.put("data", data);
                            body.put("registration_ids", tokens);

                            sendNotification(body.toString());
                        } catch (Exception e) {
                            //showToast(e.getMessage());
                        }
                    }
                });
            }
        }


        binding.inputMessage.setText(null);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody) {
        APIClient.getClient().create(APIService.class).sendMessage(getRemoteMsgHeaders(), messageBody).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJSON = new JSONObject(response.body());
                            JSONArray results = responseJSON.getJSONArray("results");
                            if (responseJSON.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                                return;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    showToast("Notification sent successfully");
                } else {
                    showToast("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    private void chooseFile() {
        Intent chooseFile = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        chooseFile.setType("*/*");
        chooseFile = Intent.createChooser(chooseFile, "Choose a file");
        // deprecated but better :)
        startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PICKFILE_RESULT_CODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String path = uri.getPath();

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
                        //t.getMetadata(); // contains file metadata such as size, content type
                        // send as a message when it has been posted on the server
                        sendMessage("image", uri.toString());
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

                    case R.id.shareChatroom:
                        shareChatroom();
                        return true;
                    default:
                        return false;
                }
            }
        });
        moreOptionsMenu.show();
    }

    private void leaveChatroom() {
        firebaseManager.leaveChatroom(chatroom.getName());
        onBackPressed();
    }

    private void shareChatroom() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "ConversationalIST");
//        TODO: translate below
        String shareMessage = "\nJoin me in " + chatroom.getName() + "\n\n";

        String shareUrl = Uri.parse("http://www.conversationalist.pt")
                .buildUpon().appendPath("chat")
                .appendQueryParameter("id", chatroom.getName())
                .build().toString();

        shareMessage = shareMessage + shareUrl + "\n\n";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
        startActivity(Intent.createChooser(shareIntent, "choose one"));
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.layoutSend.setOnClickListener(v -> {
            String content = binding.inputMessage.getText().toString().trim();
            sendMessage("text", content);
        });
        binding.layoutAttachFile.setOnClickListener(v -> chooseFile());
    }

    private String parseDate(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}