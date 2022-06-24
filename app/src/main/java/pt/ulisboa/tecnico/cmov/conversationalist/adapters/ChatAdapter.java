package pt.ulisboa.tecnico.cmov.conversationalist.adapters;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import pt.ulisboa.tecnico.cmov.conversationalist.R;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ItemContainerReceivedMessageBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ItemContainerReceivedMessageImageBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ItemContainerSentMessageBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ItemContainerSentMessageImageBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Message;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.FirebaseManager;

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

    static class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        public Message message;

        CustomViewHolder(View view) {
            super(view);
            view.setOnLongClickListener(this);

        }


        @Override
        public boolean onLongClick(View view) {

            //creating a popup menu
            PopupMenu popup = new PopupMenu(view.getContext(), view);
            //inflating menu from xml resource
            popup.inflate(R.menu.message_more_options_menu);
            //adding click listener
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.shareMessage:
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "ConversationalIST");

                            if (message.media.equals("text")) {
                                shareIntent.setType("text/plain");
                                String shareMessage = message.value;
                                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

                            } else {

                                Uri uri = Uri.parse(message.value);
//                                Uri uri = Uri.fromFile(new File(Uri.parse(message.value).getPath()));

                                ContentResolver cR = view.getContext().getContentResolver();
                                String type = cR.getType(uri);

                                shareIntent.setType(type);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                java.lang.SecurityException: UID 10492 does not have permission to content://com.android.providers.media.documents/document/image%3A39485 [user 0]; you could obtain access using ACTION_OPEN_DOCUMENT or related APIs

                            }
                            view.getContext().startActivity(Intent.createChooser(shareIntent, "choose one"));
                            return true;
                        default:
                            return false;
                    }
                }
            });
            popup.show();
            return true;
        }

    }

    static class SentMessageViewHolder extends CustomViewHolder {
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding itemContainerSentMessageBinding) {
            super(itemContainerSentMessageBinding.getRoot());
            binding = itemContainerSentMessageBinding;
        }

        void setData(Message message) {
            this.message = message;
            binding.textMessage.setText(message.value);
            binding.textDateTime.setText(message.senderId + " @ " + new SimpleDateFormat("hh:mm dd/MM/yyyy", Locale.getDefault()).format(message.timestamp));
        }


    }

    static class SentMessageImageViewHolder extends CustomViewHolder {
        private final ItemContainerSentMessageImageBinding binding;

        SentMessageImageViewHolder(ItemContainerSentMessageImageBinding itemContainerSentMessageImageBinding) {
            super(itemContainerSentMessageImageBinding.getRoot());
            binding = itemContainerSentMessageImageBinding;
        }

        void setData(Message message) {
            this.message = message;

            binding.textDateTime.setText(message.senderId + " @ " + new SimpleDateFormat("hh:mm dd/MM/yyyy", Locale.getDefault()).format(message.timestamp));

            if (this.message.media.equals("geo")) {
                List<String> values = Arrays.asList(message.value.split(","));
                if (values.size() == 2) {
                    binding.imagePos.setVisibility(View.VISIBLE);
                    binding.locationText.setVisibility(View.VISIBLE);
                    binding.locationText.setText(MessageFormat.format("({0}, {1})", values.get(0), values.get(1)));
                    binding.imagePos.setOnClickListener(v -> {

                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + values.get(0) + "," + values.get(1) + "&mode=w");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        binding.getRoot().getContext().startActivity(mapIntent);
                    });
                    return;
                }

            }

            // get firebase image file reference
            Uri uri = Uri.parse(message.value);

            ContentResolver cR = binding.getRoot().getContext().getContentResolver();
            String type = cR.getType(uri);

            StorageReference storageReference = FirebaseStorage.getInstance("gs://converstaionalist.appspot.com").getReference("images/" + uri.getLastPathSegment());
            if (type != null && type.equals("application/pdf")) {
                binding.imagePDF.setVisibility(View.VISIBLE);
                binding.imagePDF.setOnClickListener(v -> {
                    storageReference.getDownloadUrl().addOnSuccessListener(t -> {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(t, "application/pdf");
                        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        binding.getRoot().getContext().startActivity(intent);
                    });
                });
            } else {
                binding.imagePDF.setVisibility(View.INVISIBLE);

                ConnectivityManager connMgr = (ConnectivityManager) binding.getRoot().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                if(connMgr.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI){
                    binding.downloadIcon.setVisibility(View.GONE);
                    Glide.with(binding.getRoot())
                            .load(storageReference)
                            .into(binding.imgMessage);
                } else {
                    binding.downloadIcon.setOnClickListener(t -> {
                        binding.downloadIcon.setVisibility(View.GONE);
                        Glide.with(binding.getRoot())
                                .load(storageReference)
                                .into(binding.imgMessage);
                    });
                }
            }


        }
    }

    static class ReceivedMessageImageViewHolder extends CustomViewHolder {
        private final ItemContainerReceivedMessageImageBinding binding;

        ReceivedMessageImageViewHolder(ItemContainerReceivedMessageImageBinding itemContainerReceivedMessageImageBinding) {
            super(itemContainerReceivedMessageImageBinding.getRoot());
            binding = itemContainerReceivedMessageImageBinding;
        }

        void setData(Message message) {
            binding.textDateTime.setText(message.senderId + " @ " + new SimpleDateFormat("hh:mm dd/MM/yyyy", Locale.getDefault()).format(message.timestamp));

            if (message.media.equals("geo")) {
                List<String> values = Arrays.asList(message.value.split(","));
                if (values.size() == 2) {
                    binding.imagePos.setVisibility(View.VISIBLE);
                    binding.locationText.setVisibility(View.VISIBLE);
                    binding.locationText.setText(MessageFormat.format("({0}, {1})", values.get(0), values.get(1)));
                    binding.imagePos.setOnClickListener(v -> {

                        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + values.get(0) + "," + values.get(1) + "&mode=w");
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        binding.getRoot().getContext().startActivity(mapIntent);
                    });
                    return;
                }

            }

            // get firebase file reference
            Uri uri = Uri.parse(message.value);
            StorageReference storageReference = FirebaseStorage.getInstance("gs://converstaionalist.appspot.com").getReference("images/" + uri.getLastPathSegment());

            storageReference.getMetadata().addOnSuccessListener(v -> {
                if (v != null && Objects.equals(v.getContentType(), "application/pdf")) {
                    binding.imagePDF.setVisibility(View.VISIBLE);
                    binding.imagePDF.setOnClickListener(vv -> {
                        storageReference.getDownloadUrl().addOnSuccessListener(t -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(t, "application/pdf");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            binding.getRoot().getContext().startActivity(intent);
                        });
                    });
                } else {
                    binding.imagePDF.setVisibility(View.INVISIBLE);

                    ConnectivityManager connMgr = (ConnectivityManager) binding.getRoot().getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                    if(connMgr.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI){
                        binding.downloadIcon.setVisibility(View.GONE);
                        Glide.with(binding.getRoot())
                                .load(storageReference)
                                .into(binding.imgMessage);
                    } else {
                        binding.downloadIcon.setOnClickListener(t -> {
                            binding.downloadIcon.setVisibility(View.GONE);
                            Glide.with(binding.getRoot())
                                    .load(storageReference)
                                    .into(binding.imgMessage);
                        });
                    }
                }
            });
        }
    }

    static class ReceivedMessageViewHolder extends CustomViewHolder {
        private final ItemContainerReceivedMessageBinding binding;
        private final LanguageIdentifier languageIdentifier;
        private final FirebaseManager firebaseManager;


        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding itemContainerReceivedMessageBinding) {
            super(itemContainerReceivedMessageBinding.getRoot());
            binding = itemContainerReceivedMessageBinding;
            languageIdentifier = LanguageIdentification.getClient();
            firebaseManager = new FirebaseManager(binding.getRoot().getContext());
        }

        private void doBinding(String content, Message message) {
            binding.textMessage.setText(content);
            binding.textDateTime.setText(message.senderId + " @ " + new SimpleDateFormat("hh:mm dd/MM/yyyy", Locale.getDefault()).format(message.timestamp));
        }


        void setData(Message message) {
            Resources res = binding.getRoot().getResources();
            binding.textMessage.setText(MessageFormat.format("{0}...", res.getString(R.string.loading)));
            languageIdentifier.identifyLanguage(message.value).addOnSuccessListener(t -> {
                String translated;
                if (t.equals("und") && !t.equals(Locale.getDefault().getLanguage())) {
                    // no need to translate message
                    translated = message.value;
                    doBinding(translated, message);
                } else {
                    // translate message
                    String source = TranslateLanguage.fromLanguageTag(t);
                    String target = TranslateLanguage.fromLanguageTag(Locale.getDefault().getLanguage());

                    // check if precomputed already
                    String trans = message.translations.get(target);
                    if (trans != null) {
                        doBinding(trans, message);
                        return;
                    }

                    if (source != null && target != null) {
                        TranslatorOptions options = new TranslatorOptions.Builder()
                                .setSourceLanguage(source)
                                .setTargetLanguage(target)
                                .build();
                        final Translator translator = Translation.getClient(options);
                        // download the model if needed (should have been downloaded as we start the app though)
                        DownloadConditions conditions = new DownloadConditions.Builder().requireWifi().build();
                        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(d -> {
                            translator.translate(message.value).addOnSuccessListener(translatedMessage -> {
                                doBinding(translatedMessage, message);

                                // store this translated message for later use
                                firebaseManager.updateMessage(message, target, translatedMessage);
                            }).addOnFailureListener(ff -> {
                                doBinding(message.value, message);
                            });
                        }).addOnFailureListener(f -> {
                            doBinding(message.value, message);
                        });
                    } else {
                        doBinding(message.value, message);
                    }
                }
            });
        }
    }
}

