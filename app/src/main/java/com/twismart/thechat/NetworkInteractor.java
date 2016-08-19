package com.twismart.thechat;

import android.app.Activity;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.amazonaws.AmazonClientException;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.mysampleapp.demo.nosql.UserDO;

/**
 * Created by sneyd on 8/17/2016.
 **/
public class NetworkInteractor {
    private static final String TAG = "NetworkInteractor";
    public DynamoDBMapper mapper;
    private Activity activity;

    public NetworkInteractor(Activity activity){
        mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
        this.activity = activity;
    }

    public void existUserById(final String id, final IExistUserListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final UserDO userDO = mapper.load(UserDO.class, id);
                    if(userDO != null){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onYesExist(userDO);
                            }
                        });
                    }
                    else{
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listener.onNotExist();
                            }
                        });
                    }
                }
                catch (Exception e){
                    Log.d(TAG, "catch in existUserById " + e.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onNotExist();
                        }
                    });
                }
            }
        }).start();
    }

    public interface IExistUserListener {
        void onYesExist(UserDO user);
        void onNotExist();
    }

    public void writeProfile(final boolean isNewUser, final IWriteProfileListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    UserDO userDO;
                    if(isNewUser){
                        //create a new user with all the data
                        userDO = new UserDO();
                        userDO.setUserId(getIdLocal());
                        userDO.setName(getNameLocal());
                        userDO.setEmail(getEmailLocal());
                        userDO.setStatus(Constantes.Status.ONLINE.name());
                        userDO.setPhotoUrl(getPhotoUrlLocal());
                        userDO.setGender(getGenderLocal());
                        userDO.setBirthday(getBirthdayLocal());
                        userDO.setPoints(10d);//por defecto todos los usuarios tendran 10 points
                        userDO.setLanguage(getLanguageLocal());
                    }
                    else{
                        //uptade only the data visible of profile
                        userDO = mapper.load(UserDO.class, getIdLocal());
                        userDO.setName(getNameLocal());
                        userDO.setStatus(Constantes.Status.ONLINE.name());
                        userDO.setPhotoUrl(getPhotoUrlLocal());
                        userDO.setGender(getGenderLocal());
                        userDO.setBirthday(getBirthdayLocal());
                        userDO.setLanguage(getLanguageLocal());
                    }
                    final UserDO newUser = userDO;

                    mapper.save(newUser);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSucces();
                        }
                    });
                } catch (final AmazonClientException ex) {
                    Log.e(TAG, "catch in writeProfile " + ex.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(ex.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public interface IWriteProfileListener {
        void onSucces();
        void onFailure(String error);
    }


    public void writeStatusSearching(final IWriteStatusSearchingListener listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final UserDO userDO = mapper.load(UserDO.class, getIdLocal());
                    if(userDO != null){
                        userDO.setStatus(Constantes.Status.SEARCHING.name());
                        mapper.save(userDO);
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSucces();
                        }
                    });
                } catch (final AmazonClientException ex) {
                    Log.e(TAG, "catch in writeStatusSearching " + ex.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(ex.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public interface IWriteStatusSearchingListener {
        void onSucces();
        void onFailure(String error);
    }


    public void getStatus(final IGetStatus listener){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final UserDO userDO = mapper.load(UserDO.class, getIdLocal());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onSucces(userDO.getStatus());
                        }
                    });
                }
                catch (final Exception e){
                    Log.e(TAG, "catch in getStatus " + e.getMessage());
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listener.onFailure(e.getMessage());
                        }
                    });
                }
            }
        }).start();
    }

    public interface IGetStatus {
        void onSucces(String status);
        void onFailure(String error);
    }


    public void searchUserOnline(final ISearchUserOnline listener){
        try{
            new Thread(new Runnable() {
                @Override
                public void run() {

                    UserDO userToFind = new UserDO();
                    userToFind.setStatus(Constantes.Status.SEARCHING.name());
                    Log.d(TAG, "setStatus");

                    final DynamoDBQueryExpression<UserDO> queryExpression = new DynamoDBQueryExpression<UserDO>()
                            .withHashKeyValues(userToFind)
                            .withConsistentRead(false);
                    Log.d(TAG, "DynamoDBQueryExpression");

                    final PaginatedQueryList<UserDO> results = mapper.query(UserDO.class, queryExpression);
                    Log.d(TAG, "PaginatedQueryList ");
                    Log.d(TAG, "PaginatedQueryList " + results.toString());

                    UserDO useFound = results.get(0);
                    listener.onSucces(useFound.getUserId());
                }
            }).start();
        }
        catch (Exception e){
            listener.onFailure(e.getMessage());
        }
    }

    public interface ISearchUserOnline {
        void onSucces(String idUser);
        void onFailure(String error);
    }

    public void findInterlocutor(IfindInterLocutorListener listener ){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final UserDO userDO = mapper.load(UserDO.class, getIdLocal());
                    userDO.setStatus(Constantes.Status.SEARCHING.name());
                    mapper.save(userDO);



                } catch (final Exception e) {
                    Log.e(TAG, "catch in findInterlocutor " + e.getMessage());
                }
            }
        }).start();
    }

    public interface IfindInterLocutorListener {
        void onSucces(String id);
        void onFailure(String error);
    }


    public void getUserBySearch(){

    }



    public void writeLocationInPerfil(double latitude, double longitude){
        UserDO newUser = new UserDO();
        newUser.setUserId(getIdLocal());
        newUser.setLatitude(latitude);
        newUser.setLongitude(longitude);
    }

    public String getIdLocal(){
        return LoginActivity.preferences.getString(Constantes.ID, "");
    }

    public String getNameLocal(){
        return LoginActivity.preferences.getString(Constantes.NAME, "");
    }

    public String getEmailLocal(){
        return LoginActivity.preferences.getString(Constantes.EMAIL, "");
    }

    public String getPhotoUrlLocal(){
        return LoginActivity.preferences.getString(Constantes.PHOTO_URL, "");
    }

    public double getBirthdayLocal(){
        return LoginActivity.preferences.getLong(Constantes.BIRTHDAY, 0);
    }

    public String getGenderLocal(){
        return LoginActivity.preferences.getString(Constantes.GENDER, "");
    }

    public String getLanguageLocal(){
        return LoginActivity.preferences.getString(Constantes.LANGUAGE, "");
    }
}
