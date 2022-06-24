package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static final int LAUNCH_SECOND_ACTIVITY = 1001;
    public static final int LAUNCH_THIRD_ACTIVITY = 1002;
    private static final String TAG = "ChatActivity";
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
                    message.id = documentChange.getDocument().getId();
                    message.senderId = documentChange.getDocument().getString("sender");
                    message.chatroom = documentChange.getDocument().getString("chatroom");
                    message.media = documentChange.getDocument().getString("media");
                    message.value = documentChange.getDocument().getString("value");
                    message.timestamp = documentChange.getDocument().getDate("timestamp");
                    message.translations = (Map<String, String>) documentChange.getDocument().getData().get("translations");
                    if (message.translations == null) {
                        message.translations = new HashMap<>();
                    }
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
                preferenceManager.getUser().username
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

        String sharedText = (String) getIntent().getSerializableExtra("sharedText");
        Uri sharedUriDirty = (Uri) getIntent().getSerializableExtra("sharedUri");
        String sharedUri = "";
        if (sharedUriDirty != null) {
            sharedUri = sharedUriDirty.toString();
        }
        if (sharedText != null) {
            binding.inputMessage.setText(sharedText);
        } else if (!sharedUri.equals("")) {
            sendContentFile(Uri.parse(sharedUri));
        }
    }

    private void getUsersOfRoom() {
        users = new ArrayList<>();
        firebaseManager.getUsersOfRoom(chatroom.name).addOnCompleteListener(v -> {
            if (v.isSuccessful()) {
                for (DocumentSnapshot d : v.getResult().getDocuments()) {
                    if (!d.getId().equals(preferenceManager.getUser().username)) {
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
            message.put("sender", preferenceManager.getUser().username);
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
                            data.put("username", preferenceManager.getUser().username);
                            data.put("chatroom", chatroom.name);
                            data.put("fcm", preferenceManager.getUser().fcm);
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
                            if (responseJSON.getInt("failure") == 1) {
                                Log.d(TAG, "Error sending the notification");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, String.valueOf(response.code()));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.d(TAG, "Failed to create Notification");
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
                    sendContentFile(uri);
                }
                break;
            case LAUNCH_SECOND_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    Bundle bundle = data.getParcelableExtra("bundle");
                    LatLng latLng = bundle.getParcelable("result");
                    sendMessage("geo", latLng.latitude + "," + latLng.longitude);
                }
                break;

            case LAUNCH_THIRD_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");

                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                    String path = MediaStore.Images.Media.insertImage(getApplicationContext().getContentResolver(), imageBitmap, "temp", null);

                    sendContentFile(Uri.parse(path));
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendContentFile(Uri uri) {

        ContentResolver cR = getContentResolver();
        String type = cR.getType(uri);

        //storage on firebase
        // Create a storage reference from our app
        FirebaseStorage storage = FirebaseStorage.getInstance("gs://converstaionalist.appspot.com");
        StorageReference storageRef = storage.getReference();

        // Create file metadata including the content type
        StorageMetadata metadata = new StorageMetadata.Builder()
                .setContentType(type)
                .build();
        UploadTask uploadTask = storageRef.child("images/" + uri.getLastPathSegment()).putFile(uri, metadata);

        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(t -> {
            // handle failure
            Resources res = getResources();
            showToast(res.getString(R.string.error_sending_file));
        }).addOnSuccessListener(t -> {
            sendMessage("image", uri.toString());
        });
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
        firebaseManager.leaveChatroom(chatroom.name);
        onBackPressed();
    }

    private void shareChatroom() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "ConversationalIST");
//        TODO: translate below
        Resources res = getResources();
        String shareMessage = "\n" + res.getString(R.string.join_me_in) + " " + chatroom.name + "\n\n";

        String shareUrl = Uri.parse("http://www.conversationalist.pt")
                .buildUpon().appendPath("chat")
                .appendQueryParameter("id", chatroom.name)
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
        binding.layoutSendLocation.setOnClickListener(v -> sendLocation());
        binding.layoutCamera.setOnClickListener(v -> sendPhoto());
    }

    private void sendLocation() {
        Intent i = new Intent(this, NavigationMapIntent.class);
        startActivityForResult(i, LAUNCH_SECOND_ACTIVITY);
    }

    private void sendPhoto() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, LAUNCH_THIRD_ACTIVITY);
        } catch (ActivityNotFoundException e) {
//            TODO: translate below
            Resources res = getResources();
            showToast(res.getString(R.string.error_occured_when_taking_picture));
        }
    }


}