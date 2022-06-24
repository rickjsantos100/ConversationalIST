package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import pt.ulisboa.tecnico.cmov.conversationalist.R;
import pt.ulisboa.tecnico.cmov.conversationalist.adapters.ChatroomAdapter;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityChatroomBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.dialogs.CreateChatroomDialogFragment;
import pt.ulisboa.tecnico.cmov.conversationalist.listeners.ChatroomListener;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.FirebaseManager;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;

public class ChatroomActivity extends BaseActivity implements ChatroomListener {


    private ActivityChatroomBinding binding;
    private FirebaseManager firebaseManager;
    private List<Chatroom> chatrooms;
    private PreferenceManager preferenceManager;

    private String sharedText;
    private Uri sharedUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatroomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseManager = new FirebaseManager(getApplicationContext());
        preferenceManager = new PreferenceManager(this);
        loading(false);

        this.chatrooms = new ArrayList<>();

        setListeners();

        handleIntent();

    }

    @Override
    public void onResume() {
        super.onResume();
        getChatrooms();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
//                handleSendText(intent); // Handle text being sent
                this.sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
            } else if (type.startsWith("image/") || type.startsWith("application/")) {
//                handleSendContent(intent); // Handle single image being sent
                this.sharedUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
            }
            binding.buttonCreate.setVisibility(View.GONE);
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                handleSendMultipleContents(intent); // Handle multiple images being sent
            } else if (type.startsWith("application/")) {
                handleSendMultipleContents(intent); // Handle multiple images being sent
            }
        }
    }

    void handleSendMultipleContents(Intent intent) {
        ArrayList<Uri> contentUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (contentUris != null) {
            // Update UI to reflect multiple images being shared
        }
    }


    private void setListeners() {

        binding.imageBack.setOnClickListener(v -> onBackPressed());
        binding.buttonCreate.setOnClickListener(v -> {
            DialogFragment newFragment = new CreateChatroomDialogFragment();
            newFragment.show(getSupportFragmentManager(), "create_chatroom");
        });
    }

    private void getChatrooms() {
        chatrooms = new ArrayList<Chatroom>();
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        List<Chatroom> userChatroomsId = preferenceManager.getUser().chatroomsRefs;
        List<String> userChatroomsIdString = new ArrayList<>();
        for (Chatroom c : userChatroomsId) {
            userChatroomsIdString.add(c.name);
        }
        if (userChatroomsIdString.isEmpty()) {
            userChatroomsIdString.add("$$NOTAREALROOMONLYFORFIREBASETHIINGS$$");
        }


        database.collection("chatrooms")
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot q : task.getResult()) {
                            Chatroom chatroom = q.toObject(Chatroom.class);
                            if (chatroom.isPrivate != null && !chatroom.isPrivate) {
                                chatrooms.add(chatroom);
                            }
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
        Resources res = getResources();
        binding.textErrorMessage.setText(String.format("%s", res.getString(R.string.no_chatroom_available)));
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
        firebaseManager.joinChatroom(chatroom);
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra("chatroom", chatroom);
        intent.putExtra("sharedText", this.sharedText);
        intent.putExtra("sharedUri", this.sharedUri);
        this.sharedUri = null;
        this.sharedText = null;
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.searchable_menu_chatrooms, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();

        ChatroomActivity self = this;

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                List<Chatroom> filteredChatrooms;
                if (s.equals("")) {
                    filteredChatrooms = chatrooms;
                }else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        filteredChatrooms = chatrooms.stream().filter(o -> o.name.toLowerCase().startsWith(s.toLowerCase())).collect(Collectors.toList());
                    } else {
                        filteredChatrooms = new ArrayList<>();
                        for (Chatroom c : chatrooms) {
                            if (c.name.toLowerCase().startsWith(s.toLowerCase())) {
                                filteredChatrooms.add(c);
                            }
                        }
                    }
                }
                ChatroomAdapter chatroomAdapter = new ChatroomAdapter(filteredChatrooms, self);
                binding.chatroomsRecycleView.setAdapter(chatroomAdapter);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}


