package pt.ulisboa.tecnico.cmov.conversationalist.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import pt.ulisboa.tecnico.cmov.conversationalist.R;
import pt.ulisboa.tecnico.cmov.conversationalist.databinding.ActivityMapsBinding;

public class NavigationMapIntent extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private static final String TAG = "NavigationMapIntent";

    private static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private static final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        enableUserLocation();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
        }
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        String locationProvider = LocationManager.NETWORK_PROVIDER;

        Location currentLocation = locationManager.getLastKnownLocation(locationProvider);
        LatLng initialPos = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        Resources res = getResources();
        mMap.addMarker(new MarkerOptions().position(initialPos).title(res.getString(R.string.initial_pos)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialPos, 16));

        mMap.setOnMapLongClickListener(this);
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        } else {
            // ask for permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // we have permission
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
                }
                mMap.setMyLocationEnabled(true);
            }
        }

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // we have permission
                Resources res = getResources();
                Toast.makeText(this, res.getString(R.string.you_can_add_geofences), Toast.LENGTH_SHORT).show();
            } else {
                // we do not have permission
                Resources res = getResources();
                Toast.makeText(this, res.getString(R.string.no_permission_to_create_geofence), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void addTargetLocation(LatLng latLng) {
        Intent returnIntent = new Intent();
        Bundle args = new Bundle();
        args.putParcelable("result", latLng);
        returnIntent.putExtra("bundle", args);
        setResult(RESULT_OK, returnIntent);
        finish();
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        if (Build.VERSION.SDK_INT >= 29) {
            // we need background location permission as well
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                addTargetLocation(latLng);
            } else {
                // request permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
            }
        } else {
            addTargetLocation(latLng);
        }
    }
}





