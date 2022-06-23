package pt.ulisboa.tecnico.cmov.conversationalist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.activities.MapsActivity;
import pt.ulisboa.tecnico.cmov.conversationalist.models.User;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.GeofenceHelper;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.NotificationHelper;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiv";
    private List<String> geofencesRefs;
    private List<Geofence> userGeofences;
    private GeofenceHelper geoHelper;

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        PreferenceManager preferenceManager = new PreferenceManager(context);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (geofencingEvent.hasError()) {
            Log.d(TAG, "onReceive: Error receiving geofence event...");
        }

        //TODO: Uncomment the below-written code snippet and, finally, check which userGeofences are within the perimeter of the user, followed by changing the visibility of the chatrooms associated with said geofences
        /**User currentUser = preferenceManager.getUser();
        DocumentReference docRef = db.collection("users").document(currentUser.getUsername());

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        geofencesRefs = (List<String>) document.get("geofencesRefs");
                    } else {
                        Log.d(TAG, "No such document");
                    }
                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        DocumentReference currentGeo;
        for (int i = 0; i < geofencesRefs.size(); i++) {
            String currentGeoId = geofencesRefs.get(i);
            currentGeo = db.collection("geofences").document(currentGeoId);

            currentGeo.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String chatroom = (String) document.get("chatroom");
                            LatLng latLng = (LatLng) document.get("latLng");
                            double radius = (Double) document.get("radius");
                            userGeofences.add(geoHelper.getGeofence(chatroom, latLng, (float) radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT));
                        } else {
                            Log.d(TAG, "No such document");
                        }
                    } else {
                        Log.d(TAG, "get failed with ", task.getException());
                    }
                }
            });
        }**/

        List<Geofence> geos = geofencingEvent.getTriggeringGeofences();
        for (Geofence geofence : geos) {
            Log.d(TAG, "onReceive: " + geofence.getRequestId());
        }

        int transitionType = geofencingEvent.getGeofenceTransition();

        // deal with events here
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                Toast.makeText(context, "Geofence enter", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Geofence", "Entered Geofence", MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                Toast.makeText(context, "Geofence dwell", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Geofence", "Dwelling inside Geofence", MapsActivity.class);
                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                Toast.makeText(context, "Geofence exit", Toast.LENGTH_SHORT).show();
                notificationHelper.sendHighPriorityNotification("Geofence", "Exited Geofence", MapsActivity.class);
                break;

        }
    }
}