package com.twismart.thechat;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import com.amazonaws.mobile.AWSConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.mysampleapp.demo.nosql.UserDO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

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

    public static File saveToInternalStorage(Context context, Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(context);
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "TheChat" + (System.currentTimeMillis()/1000) + "Avatar.jpg");

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath;
    }

    public static String generateURL(AmazonS3 amazonS3, String filePath){
        return amazonS3.generatePresignedUrl(AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET, "public/" + filePath, new Date(new Date().getTime() + 24 * 60 * 60 * 1000)).toString();
    }
}
