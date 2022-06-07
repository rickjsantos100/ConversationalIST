package pt.ulisboa.tecnico.cmov.conversationalist.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
        View view = inflater.inflate(R.layout.create_chatroom_dialog, null);
        builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.create_chatroom, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {

//                        TODO: Fix, the binding isn't working properly, name is always coming empty
                        String name = ((EditText)view.findViewById(R.id.name)).getText().toString();
                        String region = ((EditText)view.findViewById(R.id.region)).getText().toString();

                        if(name.isEmpty()) {
//                            TODO: Stop this situation from closing the dialog
                            binding.name.setError("Required field");
                        } else {

//                        TODO:  As of now the chatroom name is the identifier, if we keep that logic we need add that protection.
                            FirebaseFirestore database = FirebaseFirestore.getInstance();
                            Chatroom chatroom = new Chatroom(name,region);
                            database.collection("chatrooms").document(chatroom.getName().toString()).set(chatroom).addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    User user = preferenceManager.getUser();
                                    DocumentReference docRef =  database.collection("users").document(user.getUsername());

                                    user.getChatroomsRefs().add(chatroom.getName().toString());
//                                    TODO: Create service to run regular firestore calls that also keep shared preferences updated
                                    docRef.update("chatroomsRefs" , user.getChatroomsRefs());
                                    preferenceManager.setUser(user);

                                } else {
                                    Toast.makeText(getActivity(), "Error creating chatroom", Toast.LENGTH_SHORT).show();
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

