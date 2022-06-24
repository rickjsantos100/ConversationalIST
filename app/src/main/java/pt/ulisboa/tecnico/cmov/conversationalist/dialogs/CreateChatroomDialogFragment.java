package pt.ulisboa.tecnico.cmov.conversationalist.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

import pt.ulisboa.tecnico.cmov.conversationalist.R;
import pt.ulisboa.tecnico.cmov.conversationalist.activities.MapsActivity;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.CreateChatroomDialogBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.models.User;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.FirebaseManager;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;


public class CreateChatroomDialogFragment extends DialogFragment {

    private CreateChatroomDialogBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseManager firebaseManager;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = CreateChatroomDialogBinding.inflate(getLayoutInflater());

        preferenceManager = new PreferenceManager(getActivity());
        firebaseManager = new FirebaseManager(getActivity());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        binding.radius.setVisibility(View.INVISIBLE);
        RadioButton geofencing = binding.geofencingRadioButton;
        geofencing.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    binding.radius.setVisibility(View.VISIBLE);
                }
            }
        });
        builder.setView(binding.getRoot())
                // Add action buttons
                .setPositiveButton(R.string.create_chatroom, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String name = binding.name.getText().toString().trim();
                        Boolean isPrivate = binding.privateRadioButton.isChecked();
                        Boolean hasGeofencing = binding.geofencingRadioButton.isChecked();
                        String radiusInput = binding.radius.getText().toString().trim();
                        float radius = 0.0f;

                        User user = preferenceManager.getUser();
                        Chatroom chatroom = new Chatroom(name);
                        chatroom.isPrivate = isPrivate;
                        chatroom.name = name;
                        chatroom.adminRef = user.username;

                        if (name.isEmpty()) {
                            Resources res = getResources();
                            binding.name.setError(res.getString(R.string.required_field));
                        } else {
                            if (hasGeofencing) {
                                if (TextUtils.isEmpty(radiusInput)) {
                                    Resources res = getResources();
                                    binding.radius.setError(res.getString(R.string.radius_required_when_geofencing_is_checked));
                                } else {
                                    radius = Float.parseFloat(radiusInput);
                                    Intent toGeoIntent = new Intent(getContext(), MapsActivity.class);
                                    toGeoIntent.putExtra("radius", radius);
                                    toGeoIntent.putExtra("chatroomGeofence", chatroom);
                                    startActivity(toGeoIntent);
                                }
                            }


                            if (radius > 0.0f) {
                                chatroom.radius = radius;
                            }

                            firebaseManager.createChatroom(chatroom).addOnFailureListener(task -> {
                                Resources res = getResources();
                                Toast.makeText(getActivity(), res.getString(R.string.error_creating_chatroom), Toast.LENGTH_SHORT).show();
                            });
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (CreateChatroomDialogFragment.this.getDialog() != null) {
                            CreateChatroomDialogFragment.this.getDialog().cancel();
                        }
                    }
                });
        builder.setTitle(R.string.create_chatroom_dialog_tittle);

        return builder.create();
    }


}


