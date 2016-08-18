package com.twismart.thechat;

/**
 * Created by sneyd on 8/3/2016.
 **/
public class Constantes {
    public static final String MY_PREFERENCES = "jlhgsghd", LOGGED = "hgfdsbflddgnl";

    public static final String SIGN_IN_MODE = "signInMode", ID = "id", EMAIL = "email", NAME  = "name", PHOTO_URL = "photoUrl", LANGUAGE = "language",GENDER ="gender", GENDER_FEMALE = "genderFemale", GENDER_MALE = "genderMale";

    public static final String IS_ADULT = "adultolder18years", BIRTHDAY = "birthdaylongtime";

    public enum SignInMode {
        GOOGLE, FACEBOOK, TWITER
    }

    public enum Status{
        SEARCHING, ONLINE, OFFLINE
    }
}
