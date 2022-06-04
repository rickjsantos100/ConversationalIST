package pt.ulisboa.tecnico.cmov.conversationalist.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.fragment.app.DialogFragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import pt.ulisboa.tecnico.cmov.conversationalist.R;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.CreateChatroomDialogBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.models.User;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;


public class CreateChatroomDialogFragment extends DialogFragment {

    private CreateChatroomDialogBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = CreateChatroomDialogBinding.inflate(getLayoutInflater());
        preferenceManager = new PreferenceManager(getActivity());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflater.inflate(R.layout.create_chatroom_dialog, null))
                // Add action buttons
                .setPositiveButton(R.string.create_chatroom, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
//                        TODO: Fix, the binding isn't working properly, name is always coming empty
                        String name = binding.name.getText().toString();
                        String region = binding.region.getText().toString();

                        name = "testChat";
                        if(name.isEmpty()) {
//                            TODO: Stop this situation from closing the dialog
                            binding.name.setError("Required field");
                        } else {
//                        TODO:    Create the chatroom
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            Chatroom chatroom = new Chatroom(binding.name.getText().toString(),binding.region.getText().toString());
                            database.collection("chatrooms").add(chatroom).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    CreateChatroomDialogFragment.this.getDialog().dismiss();
                                    User user = preferenceManager.getUser();
                                    DocumentReference docRef =  database.collection("users").document(user.getUsername());

                                    user.getChatroomsRefs().add(chatroom.getName().toString());
                                    docRef.update("chatrooms" , user.getChatroomsRefs());

                                } else {
//                                    TODO: handle this situation where the creation fails
//                                    Toast.makeText(getApplicationContext(), "Error creating chatroom", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CreateChatroomDialogFragment.this.getDialog().cancel();
                    }
                });
        builder.setTitle(R.string.create_chatroom_dialog_tittle);

        return builder.create();
    }


}


