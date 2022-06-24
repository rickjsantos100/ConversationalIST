package pt.ulisboa.tecnico.cmov.conversationalist.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.location.Geofence;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.List;

import pt.ulisboa.tecnico.cmov.conversationalist.models.User;

public class PreferenceManager {

    private final SharedPreferences sharedPreferences;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences("appPreference", Context.MODE_PRIVATE);
    }

    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public Boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public Integer getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public User getUser() {
        Gson gson = new Gson();
        return gson.fromJson(getString("user"), User.class);
    }

    public void setUser(User user) {
        Gson gson = new Gson();
        String serialized_user = gson.toJson(user);
        putString("user", serialized_user);
    }

    public List<Geofence> getGeofences() {
        Type geofencesListType = new TypeToken<List<Geofence>>() {
        }.getType();
        Gson gson = new Gson();
        return gson.fromJson(getString("geofences"), geofencesListType);
    }

    public void setGeofences(List<Geofence> geofences) {
        Type geofencesListType = new TypeToken<List<Geofence>>() {
        }.getType();
        Gson gson = new Gson();
        String serializedGeofences = gson.toJson(geofences, geofencesListType);
        putString("geofences", serializedGeofences);
    }

    public List<String> getTriggeringGeofences() {
        Type stringsListType = new TypeToken<List<String>>() {
        }.getType();
        Gson gson = new Gson();
        return gson.fromJson(getString("localGeofences"), stringsListType);
    }

    public void setTriggeringGeofences(List<String> localGeofences) {
        Type stringsListType = new TypeToken<List<String>>() {
        }.getType();
        Gson gson = new Gson();
        String serializedLocalGeofences = gson.toJson(localGeofences, stringsListType);
        putString("localGeofences", serializedLocalGeofences);
    }

    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
