package pt.ulisboa.tecnico.cmov.conversationalist.adapters;

import android.content.res.Resources;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.R;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ItemContainerChatroomBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.listeners.ChatroomListener;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;

public class ChatroomAdapter extends RecyclerView.Adapter<ChatroomAdapter.ChatroomViewHolder> {

    private final List<Chatroom> chatrooms;
    private final ChatroomListener chatroomListener;

    public ChatroomAdapter(List<Chatroom> chatrooms, ChatroomListener chatroomListener) {
        this.chatrooms = chatrooms;
        this.chatroomListener = chatroomListener;
    }

    @NonNull
    @Override
    public ChatroomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerChatroomBinding itemContainerChatroomBinding = ItemContainerChatroomBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new ChatroomViewHolder(itemContainerChatroomBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatroomViewHolder holder, int position) {
        holder.setChatroomData(chatrooms.get(position));
    }

    @Override
    public int getItemCount() {
        return chatrooms.size();
    }

    class ChatroomViewHolder extends RecyclerView.ViewHolder {

        ItemContainerChatroomBinding binding;
        PreferenceManager preferenceManager;

        ChatroomViewHolder(ItemContainerChatroomBinding itemContainerChatroomBinding) {
            super(itemContainerChatroomBinding.getRoot());
            binding = itemContainerChatroomBinding;
            preferenceManager = new PreferenceManager(binding.getRoot().getContext());
        }

        void setChatroomData(Chatroom chatroom) {
            if (chatroom.radius != null) {
                if (preferenceManager.getTriggeringGeofences() == null || (preferenceManager.getTriggeringGeofences() != null && !preferenceManager.getTriggeringGeofences().contains(chatroom.name))) {
                    binding.textName.setText(chatroom.name);
                    binding.textName.setTextColor(Color.RED);
                    binding.imageChat.setText(chatroom.name);
                    binding.textOther.setText("Can't access room...");
                    return;
                }
            }
            binding.textName.setText(chatroom.name);
            binding.imageChat.setText(chatroom.name);
            Resources res = binding.getRoot().getResources();
            binding.textOther.setText(chatroom.isPrivate ? res.getString(R.string.is_private) : res.getString(R.string.is_not_private));

            binding.getRoot().setOnClickListener(v -> chatroomListener.onChatroomClicked(chatroom));
        }
    }
}
