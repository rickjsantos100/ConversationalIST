package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.DialogFragment;

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

public class ChatroomActivity extends AppCompatActivity implements ChatroomListener {


    private ActivityChatroomBinding binding;
    private FirebaseManager firebaseManager;
    private List<Chatroom> chatrooms;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatroomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        firebaseManager = new FirebaseManager(getApplicationContext());
        loading(false);

        this.chatrooms = new ArrayList<>();

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
                chatrooms = new ArrayList<>();
                for (QueryDocumentSnapshot q : task.getResult()) {
                    Chatroom chatroom = new Chatroom();
                    chatroom.name = q.getId();
                    chatroom.region = q.getString("region");
                    chatroom.radius = q.getLong("radius");
                    chatroom.isPrivate = Boolean.TRUE.equals(q.getBoolean("isPrivate")) || Boolean.TRUE.equals(q.getBoolean("private"));
                    chatroom.adminRef = q.getString("admingRef");
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
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    filteredChatrooms = chatrooms.stream().filter(o -> o.name.startsWith(s)).collect(Collectors.toList());
                } else {
                    filteredChatrooms = new ArrayList<>();
                    for (Chatroom c : chatrooms) {
                        if (c.name.startsWith(s)) {
                            filteredChatrooms.add(c);
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


