package com.twismart.thechat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Register extends AppCompatActivity {

    FragmentProfile fragmentProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView text = (TextView) findViewById(R.id.textPolicy);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        fragmentProfile = (FragmentProfile) getSupportFragmentManager().findFragmentById(R.id.fragmentProfile);
    }

    boolean register = false;
    public void register(View v){
        if(!register) {
            Log.d("RERE", "rere");
            fragmentProfile.register();
            register = true;
        }
    }
}
