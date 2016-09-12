package com.twismart.thechat;

import com.google.android.gms.ads.AdRequest;

/**
 * Created by sneyd on 8/3/2016.
 **/
public class Constantes {

    public static final String PREFERENCES_PROFILE = "jlhgsghd", PREFERENCES_FIND = "jkhgjhgjh", LOGGED = "hgfdsbflddgnl";

    public static final String SIGN_IN_MODE = "signInMode", ID = "id", TOKEN_ID = "tokenId", EMAIL = "email", NAME  = "name", PHOTO_URL = "photoUrl", LANGUAGE = "language",GENDER ="gender", GENDER_FEMALE = "genderFemale", GENDER_MALE = "genderMale";

    public static final String DISTANCE_MAX = "distanceMax", MY_LANGUAGE = "myLanguage", AGE_MAX = "ageMax", AGE_MIN ="ageMin";

    public static final String IS_ADULT = "adultolder18years", BIRTHDAY = "birthdaylongtime";

    public static final String SEPARATOR = "-";

    public static final String ID_ADMOB = "ca-app-pub-2063878848044934~3456337403", ADS_AMAZON = "0ee32d416b424c61ba5e6b3d4f482a73";

    public static AdRequest getAdRequest(){
        //return new AdRequest.Builder().addTestDevice("B2401252970D3BE49377E60BD774B087").addTestDevice("D02BE77B5DF7C7CC1108087092746DAC").addTestDevice("D7F979F440628D70BB965EF52BCACB8C").build();
        return new AdRequest.Builder().build();
    }

    public enum SignInMode {
        GOOGLE, FACEBOOK, TWITER
    }

    public enum Status {
        SEARCHING, FOUND, ONLINE, OFFLINE
    }

    public enum TypeMessage {
        TEXT, IMAGE, STICKER, EFECT
    }

    public static final String TYPE_BACKGROUND = "typeBackground", BACKGROUND = "background";

    public enum TypeBackground {
        IMAGE, COLOR, DEFAULT
    }
}
