package com.twismart.thechat;

/**
 * Created by sneyd on 8/3/2016.
 **/
public class Constantes {
    public static final String PREFERENCES_PROFILE = "jlhgsghd", PREFERENCES_FIND = "jkhgjhgjh", LOGGED = "hgfdsbflddgnl";

    public static final String SIGN_IN_MODE = "signInMode", ID = "id", TOKEN_ID = "tokenId", EMAIL = "email", NAME  = "name", PHOTO_URL = "photoUrl", LANGUAGE = "language",GENDER ="gender", GENDER_FEMALE = "genderFemale", GENDER_MALE = "genderMale";

    public static final String DISTANCE_MAX = "distanceMax", MY_LANGUAGE = "myLanguage", AGE_MAX = "ageMax", AGE_MIN ="ageMin";

    public static final String IS_ADULT = "adultolder18years", BIRTHDAY = "birthdaylongtime";

    public static final String SEPARATOR = "-";

    public enum SignInMode {
        GOOGLE, FACEBOOK, TWITER
    }

    public enum Status{
        SEARCHING, FOUND, ONLINE, OFFLINE
    }

    public enum TypeMessage {
        TEXT, IMAGE, STICKER, EFECT
    }
}
