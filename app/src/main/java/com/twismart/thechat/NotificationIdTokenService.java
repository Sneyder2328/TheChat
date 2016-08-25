package com.twismart.thechat;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by sneyd on 7/18/2016.
 **/
public class NotificationIdTokenService extends FirebaseInstanceIdService {

    private static final String TAG="IdTokenService";

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "token: " + token);
        NetworkInteractor networkInteractor = new NetworkInteractor(getApplicationContext());
        networkInteractor.writeTokenIdFirebase(token, null);
    }
}
