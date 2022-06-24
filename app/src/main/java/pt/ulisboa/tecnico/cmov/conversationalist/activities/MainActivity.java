package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.R;
import pt.ulisboa.tecnico.cmov.conversationalist.adapters.ChatroomAdapter;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityMainBinding;
import pt.ulisboa.tecnico.cmov.conversationalist.listeners.ChatroomListener;
import pt.ulisboa.tecnico.cmov.conversationalist.models.Chatroom;
import pt.ulisboa.tecnico.cmov.conversationalist.models.GeoCage;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.FirebaseManager;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.GeofenceHelper;
import pt.ulisboa.tecnico.cmov.conversationalist.utilities.PreferenceManager;


public class MainActivity extends BaseActivity implements ChatroomListener {

    private static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseManager firebaseManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        firebaseManager = new FirebaseManager(getApplicationContext());

        handleIntent(getIntent());
        loadUserDetails();
        setListeners();
        getUserChatrooms();
        setUserGeofences();
        getToken();
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserChatrooms();
    }

    private void handleIntent(Intent intent) {
        String appLinkAction = intent.getAction();
        Uri appLinkData = intent.getData();
        if (Intent.ACTION_VIEW.equals(appLinkAction) && appLinkData != null) {
            if (preferenceManager.getUser().username != null) {
                String chatroomId = appLinkData.getQueryParameter("id");
                firebaseManager.getChatroom(chatroomId).addOnSuccessListener(documentSnapshot -> {
                    Chatroom chatroom = documentSnapshot.toObject(Chatroom.class);
                    navigateToChatroom(chatroom);
                });
            } else {
                Intent signInIntent = new Intent(getApplicationContext(), SignInActivity.class);
                signInIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(signInIntent);
            }
        }
    }


    private void setListeners() {
        binding.newRoom.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), ChatroomActivity.class)));
        binding.imgsignOut.setOnClickListener(v -> {
            signOut();
        });
        binding.imgToggleTheme.setOnClickListener(v -> {
            if (preferenceManager.getInt("night") == 1) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            preferenceManager.putInt("night", AppCompatDelegate.getDefaultNightMode());
        });
    }

    private void loadUserDetails() {
        binding.textName.setText(preferenceManager.getUser().username);
    }

    private void updateToken(String token) {
        firebaseManager.updateToken(token);
    }

    private void getToken() {
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void signOut() {
        Resources res = getResources();
        Toast.makeText(getApplicationContext(), res.getString(R.string.signing_out), Toast.LENGTH_SHORT).show();
        firebaseManager.clearToken();
        preferenceManager.clear();
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
        finish();
    }

    private void getUserChatrooms() {
        List<Chatroom> userChatrooms = preferenceManager.getUser().chatroomsRefs;

        if (userChatrooms.size() > 0) {
            ChatroomAdapter chatroomAdapter = new ChatroomAdapter(userChatrooms, this);
            binding.chatroomsRecycleView.setAdapter(chatroomAdapter);
            binding.chatroomsRecycleView.setVisibility(View.VISIBLE);
        }
        loading(false);
    }

    private void setUserGeofences() {
        List<GeoCage> userGeocages = preferenceManager.getUser().geofencesRefs;
        List<Geofence> userGeofences = new ArrayList<>();
        GeofenceHelper geofenceHelper = new GeofenceHelper(this);
        GeofencingClient geoClient = LocationServices.getGeofencingClient(this);

        // permission is checked elsewhere
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // ask for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
        }

        for (GeoCage geoCage : userGeocages) {
            String geoChatroomId = geoCage.chatroomID;
            Double geoLatitude = geoCage.latitude;
            Double geoLongitude = geoCage.longitude;
            LatLng geoLatLng = new LatLng(geoLatitude, geoLongitude);
            Long radius = geoCage.radius;

            Geofence geofence = geofenceHelper.getGeofence(geoChatroomId, geoLatLng, radius, Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL | Geofence.GEOFENCE_TRANSITION_EXIT);
            GeofencingRequest geofencingRequest = geofenceHelper.getGeofenceRequest(geofence);
            PendingIntent pendingIntent = geofenceHelper.getPendingIntent();
            userGeofences.add(geofence);

            geoClient.addGeofences(geofencingRequest, pendingIntent);
        }

        if (userGeofences.size() > 0) {
            preferenceManager.setGeofences(userGeofences);
        }
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);

        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onChatroomClicked(Chatroom chatroom) {
        navigateToChatroom(chatroom);
    }

    private void navigateToChatroom(Chatroom chatroom) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra("chatroom", chatroom);
        startActivity(intent);
    }
    
}