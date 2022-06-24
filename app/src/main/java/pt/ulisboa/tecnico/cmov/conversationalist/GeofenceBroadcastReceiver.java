package pt.ulisboa.tecnico.cmov.conversationalist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.activities.MapsActivity;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.models.User;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.FirebaseManager;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.NotificationHelper;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiv";

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        PreferenceManager preferenceManager = new PreferenceManager(context);
        FirebaseManager firebaseManager = new FirebaseManager(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        User currentUser = preferenceManager.getUser();

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
        }

        List<Geofence> geos = geofencingEvent.getTriggeringGeofences();

        int transitionType = geofencingEvent.getGeofenceTransition();

        // deal with events here
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                //
                Toast.makeText(context, "Geofence enter", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Geofence", "Entered Geofence", MapsActivity.class);

                for (int i = 0; i < geos.size(); i++) {
                    Geofence currentGeo = geos.get(i);
                    firebaseManager.getChatroom(currentGeo.getRequestId()).addOnSuccessListener(documentSnapshot -> {
                        Chatroom chatroom = documentSnapshot.toObject(Chatroom.class);
                        List<Chatroom> userChatrooms = currentUser.chatroomsRefs;

                        if (userChatrooms.contains(chatroom)) {
                            db.collection("users").document(currentUser.username).update("activeGeofences", FieldValue.arrayUnion(chatroom.name));
                        } else {

                        }
                    });
                }
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "Geofence exit", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Geofence", "Exited Geofence", MapsActivity.class);

                for (int i = 0; i < geos.size(); i++) {
                    Geofence currentGeo = geos.get(i);
                    firebaseManager.getChatroom(currentGeo.getRequestId()).addOnSuccessListener(documentSnapshot -> {
                        Chatroom chatroom = documentSnapshot.toObject(Chatroom.class);
                        List<Chatroom> userChatrooms = currentUser.chatroomsRefs;

                        if (userChatrooms.contains(chatroom)) {
                            db.collection("users").document(currentUser.username).update("activeGeofences", FieldValue.arrayRemove(chatroom.name));
                        } else {

                        }
                    });
                }
                break;
        }
    }
}