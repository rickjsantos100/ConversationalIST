package pt.ulisboa.tecnico.cmov.conversationalist.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ItemContainerChatroomBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;

public class ChatroomAdapter extends RecyclerView.Adapter<ChatroomAdapter.ChatroomViewHolder> {

    private final List<Chatroom> chatrooms;

    public ChatroomAdapter(List<Chatroom> chatrooms) {
        Log.d("potato", "consutrcutor: " + chatrooms.get(0).name);
        this.chatrooms = chatrooms;
    }

    @NonNull
    @Override
    public ChatroomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d("potato", "oncreateviewholder: " + chatrooms.get(0).name);
        ItemContainerChatroomBinding itemContainerChatroomBinding = ItemContainerChatroomBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );

        return new ChatroomViewHolder(itemContainerChatroomBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatroomViewHolder holder, int position) {
        Log.d("potato", "onbindingview: " + position);
        holder.setChatroomData(chatrooms.get(position));
    }

    @Override
    public int getItemCount() {
        Log.d("potato", "itemcount: " + chatrooms.get(0).name);
        return chatrooms.size();
    }

    class ChatroomViewHolder extends RecyclerView.ViewHolder {

        ItemContainerChatroomBinding binding;

        ChatroomViewHolder(ItemContainerChatroomBinding itemContainerChatroomBinding) {
            super(itemContainerChatroomBinding.getRoot());
            Log.d("potato", "viewholderconstructor: " + chatrooms.get(0).name);
            binding = itemContainerChatroomBinding;
        }

        void setChatroomData(Chatroom chatroom) {
            Log.d("potato", "im here bro look: " + chatroom.name);

            binding.textName.setText(chatroom.name);
            binding.imageChat.setText(chatroom.name);
            // maybe add other things like private here
            binding.textOther.setText(chatroom.region);
        }
    }
}
