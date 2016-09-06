package com.twismart.thechat;

import android.location.Location;
import android.util.Log;

import com.mysampleapp.demo.nosql.UserDO;

/**
 * Created by sneyd on 9/2/2016.
 **/
public class Util {

    public static int distanceBetweenUsers(UserDO userDO1, UserDO userDO2){
        try {
            Location selected_location = new Location("locationA");
            selected_location.setLatitude(userDO1.getLatitude());
            selected_location.setLongitude(userDO1.getLongitude());

            Location near_locations = new Location("locationB");
            near_locations.setLatitude(userDO2.getLatitude());
            near_locations.setLongitude(userDO2.getLongitude());

            return (int) selected_location.distanceTo(near_locations) / 1000;//return the result in km
        }
        catch(Exception e){
            Log.e("Util", "catch in distanceBetweenUsers " + e.getMessage());
            return 0;
        }
    }
}
