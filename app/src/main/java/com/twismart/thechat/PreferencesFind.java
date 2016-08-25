package com.twismart.thechat;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by sneyd on 8/19/2016.
 **/
public class PreferencesFind {

    public SharedPreferences preferences;

    public PreferencesFind(Context context){
        preferences = context.getSharedPreferences(Constantes.PREFERENCES_FIND, Context.MODE_PRIVATE);
    }

    public void setGender(String gender){
        preferences.edit().putString(Constantes.GENDER, gender).apply();
    }

    public String getGender(){
        return preferences.getString(Constantes.GENDER, "");
    }

    public void setDistanceMax(int distanceMax){
        preferences.edit().putInt(Constantes.DISTANCE_MAX, distanceMax).apply();
    }

    public int getDistanceMax(){
        return preferences.getInt(Constantes.DISTANCE_MAX, 100000);
    }

    public void setInMyLanguage(boolean inMyLanguage){
        preferences.edit().putBoolean(Constantes.MY_LANGUAGE, inMyLanguage).apply();
    }

    public boolean getInMyLanguage(){
        return preferences.getBoolean(Constantes.MY_LANGUAGE, true);
    }

    public void setAgeMin(long ageMin){
        Log.d("agemin", "agemin " + ageMin);
        preferences.edit().putLong(Constantes.AGE_MIN, ageMin).apply();
    }

    public long getAgeMin(){
        return preferences.getLong(Constantes.AGE_MIN, 0);
    }

    public void setAgeMax(long ageMax){
        Log.d("agemax", "agemax " + ageMax);
        preferences.edit().putLong(Constantes.AGE_MAX, ageMax).apply();
    }

    public long getAgeMax(){
        return preferences.getLong(Constantes.AGE_MAX, 0);
    }


    public void clear(){
        preferences.edit().clear().apply();
    }
}
