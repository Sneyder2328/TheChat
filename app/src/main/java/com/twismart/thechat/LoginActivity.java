package com.twismart.thechat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mysampleapp.demo.nosql.UserDO;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 1;
    public static SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        preferences = getSharedPreferences(Constantes.MY_PREFERENCES, MODE_PRIVATE);
        if(preferences.getBoolean(Constantes.LOGGED, false)){
            openMainActivity();
        }
        else{
            //init process sign in with Google
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .enableAutoManage(this, null /* OnConnectionFailedListener */)
                    .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                    .build();
        }
    }

    public void signIn(View v){
        Log.d(TAG, "sign in");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
        else{
            Log.e(TAG, "requestCode no es igual al de Signin: " + requestCode);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("handleSignInResult", "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            final GoogleSignInAccount acct = result.getSignInAccount();

            try {
                Log.d("getEmail", acct.getEmail());
                Log.d("getDisplayName", acct.getDisplayName());
                Log.d("getId", acct.getId());
                Log.d("getPhotoUrl", ""+acct.getPhotoUrl());

                final ProgressDialog mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage(getString(R.string.login_message_progress_load));
                mProgressDialog.show();

                NetworkInteractor networkInteractor = new NetworkInteractor(this);
                networkInteractor.existUserById(acct.getId(), new NetworkInteractor.IExistUserListener() {
                    @Override
                    public void onYesExist(UserDO user) {
                        saveUserInLocal(user);
                        openMainActivity();
                        mProgressDialog.cancel();
                    }
                    @Override
                    public void onNotExist() {
                        openRegister(Constantes.SignInMode.GOOGLE.name(), acct.getId(), acct.getEmail(), acct.getDisplayName(), acct.getPhotoUrl()+"");
                        mProgressDialog.cancel();
                    }
                });
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            }
            catch (Exception e){
                Log.d("Error", "catch en log de datos " +e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.login_signin_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void saveUserInLocal(UserDO user){
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constantes.ID, user.getUserId());
        editor.putString(Constantes.NAME, user.getName());
        editor.putString(Constantes.EMAIL, user.getEmail());
        editor.putString(Constantes.PHOTO_URL, user.getPhotoUrl());
        editor.putString(Constantes.GENDER, user.getGender());
        editor.putString(Constantes.LANGUAGE, user.getLanguage());
        editor.putBoolean(Constantes.IS_ADULT, true);
        double birthday = user.getBirthday();
        editor.putLong(Constantes.BIRTHDAY, (long) birthday);
        editor.apply();
    }

    private void openMainActivity(){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    private void openRegister(String signInMode, String id, String email, String name, String photoUrl){
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putString(Constantes.SIGN_IN_MODE, signInMode);//aun no la uso en dynamo// prox la integrare
        editor.putString(Constantes.ID, id);
        editor.putString(Constantes.NAME, name);
        editor.putString(Constantes.EMAIL, email);
        editor.putString(Constantes.PHOTO_URL, photoUrl);
        editor.apply();

        startActivity(new Intent(this, Register.class));
        finish();
    }
}
