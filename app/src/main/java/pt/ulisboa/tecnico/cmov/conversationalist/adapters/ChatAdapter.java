package pt.ulisboa.tecnico.cmov.conversationalist.adapters;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ItemContainerReceivedMessageBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ItemContainerReceivedMessageImageBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ItemContainerSentMessageBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ItemContainerSentMessageImageBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Message;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;
    public static final int VIEW_TYPE_SENT_IMAGE = 3;
    public static final int VIEW_TYPE_RECEIVED_IMAGE = 4;

    private final List<Message> messages;
    private final String senderId;

    public ChatAdapter(List<Message> messages, String senderId) {
        this.messages = messages;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_RECEIVED) {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else if (viewType == VIEW_TYPE_SENT_IMAGE) {
            return new SentMessageImageViewHolder(ItemContainerSentMessageImageBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false)
            );
        } else {
            return new ReceivedMessageImageViewHolder(
                    ItemContainerReceivedMessageImageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(messages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_RECEIVED) {
            ((ReceivedMessageViewHolder) holder).setData(messages.get(position));
        } else if (getItemViewType(position) == VIEW_TYPE_SENT_IMAGE) {
            ((SentMessageImageViewHolder) holder).setData(messages.get(position));
        } else {
            ((ReceivedMessageImageViewHolder) holder).setData(messages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        String type = messages.get(position).media;
        if (messages.get(position).senderId == null || type == null) {
            return VIEW_TYPE_SENT;
        }
        if (messages.get(position).senderId.equals(senderId)) {
            if (type.equals("text")) {
                return VIEW_TYPE_SENT;
            } else {
                return VIEW_TYPE_SENT_IMAGE;
            }
        } else {
            if (type.equals("text")) {
                return VIEW_TYPE_RECEIVED;
            } else {
                return VIEW_TYPE_RECEIVED_IMAGE;
            }
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(Message message) {
            binding.textMessage.setText(message.value);
//            TODO: change the implementation below to not be deprecated ,possibly use Calendar instead of Date
            binding.textDateTime.setText(message.senderId + " @ " + message.timestamp.getHours() + ":" + message.timestamp.getMinutes());
        }
    }

    static class SentMessageImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageImageBinding binding;

        SentMessageImageViewHolder(ItemContainerSentMessageImageBinding itemContainerSentMessageImageBinding) {
            super(itemContainerSentMessageImageBinding.getRoot());
            binding = itemContainerSentMessageImageBinding;
        }

        void setData(Message message) {

            // get firebase image file reference
            Uri uri = Uri.parse(message.value);
            Log.d("POTATO", "images/" + uri.getLastPathSegment());
            StorageReference storageReference = FirebaseStorage.getInstance("gs://converstaionalist.appspot.com").getReference().child("images/" + uri.getLastPathSegment());
            Glide.with(binding.getRoot())
                    .load(storageReference)
                    .into(binding.imgMessage);

//          TODO: change the implementation below to not be deprecated ,possibly use Calendar instead of Date
            binding.textDateTime.setText(message.senderId + " @ " + message.timestamp.getHours() + ":" + message.timestamp.getMinutes());
        }
    }

    static class ReceivedMessageImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageImageBinding binding;

        ReceivedMessageImageViewHolder(ItemContainerReceivedMessageImageBinding itemContainerReceivedMessageImageBinding) {
            super(itemContainerReceivedMessageImageBinding.getRoot());
            binding = itemContainerReceivedMessageImageBinding;
        }

        void setData(Message message) {

            // get firebase image file reference
            Uri uri = Uri.parse(message.value);
            StorageReference storageReference = FirebaseStorage.getInstance("gs://converstaionalist.appspot.com").getReference("images/" + uri.getLastPathSegment());

            Glide.with(binding.getRoot())
                    .load(storageReference)
                    .into(binding.imgMessage);
//          TODO: change the implementation below to not be deprecated ,possibly use Calendar instead of Date
            binding.textDateTime.setText(message.senderId + " @ " + message.timestamp.getHours() + ":" + message.timestamp.getMinutes());
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
        }

        void setData(Message message) {
            binding.textMessage.setText(message.value);
//            TODO: change the implementation below to not be deprecated ,possibly use Calendar instead of Date
            binding.textDateTime.setText(message.senderId + " @ " + message.timestamp.getHours() + ":" + message.timestamp.getMinutes());
        }
    }
}
