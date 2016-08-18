package com.twismart.thechat;

import com.mysampleapp.demo.nosql.UserDO;

/**
 * Created by sneyd on 8/17/2016.
 **/
public class NetworkInteractor {

    public void writePerfil(){
        String id = LoginActivity.preferences.getString(Constantes.ID, "");
        String name = LoginActivity.preferences.getString(Constantes.NAME, "");
        String email = LoginActivity.preferences.getString(Constantes.EMAIL, "");
        String photoUrl = LoginActivity.preferences.getString(Constantes.PHOTO_URL, "");
        double birthday = LoginActivity.preferences.getLong(Constantes.BIRTHDAY, 0);
        String gender = LoginActivity.preferences.getString(Constantes.GENDER, "");
        String language = LoginActivity.preferences.getString(Constantes.LANGUAGE, "");


        UserDO newUser = new UserDO();
        newUser.setUserId(id);
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setStatus(Constantes.Status.ONLINE.name());
        newUser.setPhotoUrl(photoUrl);
        newUser.setGender(gender);
        newUser.setBirthday(birthday);
        newUser.setPoints(10d);
        newUser.setLanguage(language);
        //newUser.setLatitude();
        //newUser.setLongitude();
    }
}
