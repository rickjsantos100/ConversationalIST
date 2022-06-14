package pt.ulisboa.tecnico.cmov.conversationalist.services;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationsService extends FirebaseMessagingService {

    private static final String TAG = "FIREBASE";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        if (message.getNotification() != null) {

            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(() -> Toast.makeText(getApplicationContext(), message.getNotification().getBody(), Toast.LENGTH_LONG).show());
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        Log.d(TAG, "Refreshed token: " + token);

    }
}
