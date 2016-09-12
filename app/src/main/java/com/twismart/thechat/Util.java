package com.twismart.thechat;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.util.Log;

import com.amazonaws.mobile.AWSConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.mysampleapp.demo.nosql.UserDO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
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

        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);

        File mypath = new File(directory, "TheChat" + (System.currentTimeMillis()/1000) + ".png");

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(mypath);
            bitmapImage = getResizedBitmap(bitmapImage, bitmapImage.getWidth() * 3 , bitmapImage.getHeight() * 3);
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath;
    }

    public static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }


    public static String generateURL(AmazonS3 amazonS3, String filePath){
        return amazonS3.generatePresignedUrl(AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET, "public/" + filePath, new Date(new Date().getTime() + 24 * 60 * 60 * 1000)).toString();
    }
}
