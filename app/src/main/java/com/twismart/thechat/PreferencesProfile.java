package com.twismart.thechat;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by sneyd on 8/19/2016.
 **/
public class PreferencesProfile {

    public SharedPreferences preferences;

    public PreferencesProfile(Context context){
        preferences = context.getSharedPreferences(Constantes.PREFERENCES_PROFILE, Context.MODE_PRIVATE);
    }

    public String getId(){
        return preferences.getString(Constantes.ID, "");
    }

    public String getName(){
        return preferences.getString(Constantes.NAME, "");
    }

    public String getEmail(){
        return preferences.getString(Constantes.EMAIL, "");
    }

    public String getPhotoUrl(){
        return preferences.getString(Constantes.PHOTO_URL, "");
    }

    public double getBirthday(){
        return preferences.getLong(Constantes.BIRTHDAY, 0);
    }

    public String getGender(){
        return preferences.getString(Constantes.GENDER, "");
    }

    public String getLanguage(){
        return preferences.getString(Constantes.LANGUAGE, "");
    }

    public void setLogged(boolean logged){
        preferences.edit().putBoolean(Constantes.LOGGED, logged).apply();
    }
    public boolean getLogged(){
        return preferences.getBoolean(Constantes.LOGGED, false);
    }

    public void clear(){
        preferences.edit().clear().apply();
    }
}
