package pt.ulisboa.tecnico.cmov.conversationalist.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

import pt.ulisboa.tecnico.cmov.conversationalist.R;
import pt.ulisboa.tecnico.cmov.conversationalist.activities.ChatActivity;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.models.User;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.FirebaseManager;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM";
    private static PreferenceManager preferenceManager;
    private static FirebaseManager firebaseManager;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        preferenceManager = new PreferenceManager(getApplicationContext());
        firebaseManager = new FirebaseManager(getApplicationContext());
        User user = new User();
        user.username = message.getData().get("username");
        user.fcm = message.getData().get("fcm");
        Chatroom chatroom = new Chatroom();
        chatroom.name = message.getData().get("chatroom");

        int notificationId = new Random().nextInt();
        String channelId = "chat_message";

        Intent intent = new Intent(this, ChatActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("chatroom", chatroom);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(chatroom.name);
        builder.setContentText(message.getData().get("message"));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message.getData().get("message")));
        builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence channelName = "Chat Message";
            String channelDescription = "This notification channel is used for chat message notifications";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDescription);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(notificationId, builder.build());

        User currentUser = preferenceManager.getUser();
        String currentChatroomRef = preferenceManager.getString("currentChatroom");

        firebaseManager.getUserById(currentUser.username).addOnCompleteListener(v -> {
            if (v.isSuccessful()) {
                try {
                    Long isOnline = v.getResult().getLong("online");

                    if (isOnline != null && isOnline == 1) {
                        if (currentChatroomRef != null && currentChatroomRef.equals(chatroom.name)) {
                            String toastInfo = user.username + " @ " + chatroom.name + ": " + message.getData().get("message");
                            Toast.makeText(getApplicationContext(), toastInfo, Toast.LENGTH_LONG).show();
                        }
                    }
                } catch (Exception e) {
                    //showToast(e.getMessage());
                }
            }
        });
    }
}
