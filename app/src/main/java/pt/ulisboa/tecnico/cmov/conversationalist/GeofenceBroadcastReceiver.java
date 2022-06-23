package pt.ulisboa.tecnico.cmov.conversationalist;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.activities.MapsActivity;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.NotificationHelper;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceBroadcastReceiv";

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationHelper notificationHelper = new NotificationHelper(context);
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        PreferenceManager preferenceManager = new PreferenceManager(context);

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