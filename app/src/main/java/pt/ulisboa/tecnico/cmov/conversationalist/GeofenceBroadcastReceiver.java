package pt.ulisboa.tecnico.cmov.conversationalist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.activities.MapsActivity;
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

        Resources res = context.getResources();
        // deal with events here
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context, res.getString(R.string.entered_geofence), Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Geofence", res.getString(R.string.entered_geofence), MapsActivity.class);

                for (Geofence currentGeo : geos) {
                    firebaseManager.getChatroom(currentGeo.getRequestId()).addOnSuccessListener(documentSnapshot -> {
                        Chatroom chatroom = documentSnapshot.toObject(Chatroom.class);

                        if (currentUser.chatroomsRefs.contains(chatroom)) {
                            List<String> triggeringGeofences = preferenceManager.getTriggeringGeofences();
                            if (triggeringGeofences == null) {
                                triggeringGeofences = new ArrayList<>();
                            }
                            triggeringGeofences.add(currentGeo.getRequestId());
                            preferenceManager.setTriggeringGeofences(triggeringGeofences);
                        }
                    });
                }
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, res.getString(R.string.geofence_exit), Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Geofence", res.getString(R.string.entered_geofence), MapsActivity.class);

                for (Geofence currentGeo : geos) {
                    firebaseManager.getChatroom(currentGeo.getRequestId()).addOnSuccessListener(documentSnapshot -> {
                        Chatroom chatroom = documentSnapshot.toObject(Chatroom.class);

                        if (currentUser.chatroomsRefs.contains(chatroom)) {
                            List<String> triggeringGeofences = preferenceManager.getTriggeringGeofences();
                            if (triggeringGeofences != null) {
                                triggeringGeofences.remove(currentGeo.getRequestId());
                                preferenceManager.setTriggeringGeofences(triggeringGeofences);
                            }
                        }
                    });
                }
                break;
        }
    }
}