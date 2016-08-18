package com.twismart.thechat;

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
            openChat();
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

        startActivity(new Intent(this, MainActivity.class));
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
            GoogleSignInAccount acct = result.getSignInAccount();

            try {
                Log.d("getEmail", acct.getEmail());
                Log.d("getDisplayName", acct.getDisplayName());
                Log.d("getId", acct.getId());
                Log.d("getPhotoUrl", ""+acct.getPhotoUrl());

                openRegister(Constantes.SignInMode.GOOGLE.name(), acct.getId(), acct.getEmail(), acct.getDisplayName(), acct.getPhotoUrl()+"");
            }
            catch (Exception e){
                Log.d("Error", "catch en log de datos " +e.getMessage());
            }
        } else {
            Toast.makeText(this, R.string.main_signin_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void openRegister(String signInMode, String id, String email, String name, String photoUrl){
        Bundle data = new Bundle();
        data.putString(Constantes.SIGN_IN_MODE, signInMode);
        data.putString(Constantes.ID, id);
        data.putString(Constantes.EMAIL, email);
        data.putString(Constantes.NAME, name);
        data.putString(Constantes.PHOTO_URL, photoUrl);

        Intent intent = new Intent(this, Register.class);
        intent.putExtras(data);
        startActivity(intent);
    }

    private void openChat(){
       // startActivity(new Intent(this, Chat.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
