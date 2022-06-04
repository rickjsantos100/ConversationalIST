package pt.ulisboa.tecnico.cmov.conversationalist.utilities;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

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

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, null);
    }

    public void setUser(User user) {
        Gson gson = new Gson();
        String serialized_user = gson.toJson(user);
        putString("user",serialized_user );
    }

    public User getUser() {
        Gson gson = new Gson();
        return gson.fromJson(getString("user"), User.class);
    }


    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
