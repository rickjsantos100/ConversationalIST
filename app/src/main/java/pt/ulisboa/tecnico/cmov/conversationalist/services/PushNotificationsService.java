package pt.ulisboa.tecnico.cmov.conversationalist.services;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class PushNotificationsService extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
    }

    @Override
    public void onNewToken(@NonNull String token) {
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {

    }
}
