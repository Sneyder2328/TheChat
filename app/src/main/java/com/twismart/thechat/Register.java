package com.twismart.thechat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;

public class Register extends AppCompatActivity {

    public static final String TAG = "Register";
    private static final int PICK_PHOTO = 100, TAKE_PHOTO = 101;
    public static TextView birthday;

    private EditText inputName;
    private RadioButton optionFemale, optionMale;
    private CircleImageView imgAvatar;
    private Spinner spinnerLanguages;

    private String[] listLanguages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Bundle data = getIntent().getExtras();

        SharedPreferences.Editor editor = LoginActivity.preferences.edit();
        editor.putString(Constantes.SIGN_IN_MODE, data.getString(Constantes.SIGN_IN_MODE));//aun no la uso en dynamo// prox la integrare
        editor.putString(Constantes.ID, data.getString(Constantes.ID));
        editor.putString(Constantes.EMAIL, data.getString(Constantes.EMAIL));
        editor.putString(Constantes.PHOTO_URL, data.getString(Constantes.PHOTO_URL));
        editor.apply();

        inputName = (EditText) findViewById(R.id.inputName);
        inputName.setText(data.getString(Constantes.NAME));

        //
        imgAvatar = (CircleImageView) findViewById(R.id.imgAvatar);
        Glide.with(this).load(data.getString(Constantes.PHOTO_URL)).into(imgAvatar);

        //
        birthday = (TextView) findViewById(R.id.birthday);

        //
        optionFemale = (RadioButton) findViewById(R.id.optionFemale);
        optionMale = (RadioButton) findViewById(R.id.optionMale);

        //
        TextView text = (TextView)findViewById(R.id.textPolicy);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        //
        listLanguages = getResources().getStringArray(R.array.register_list_languages);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.register_list_languages, android.R.layout.simple_spinner_dropdown_item);

        spinnerLanguages = (Spinner) findViewById(R.id.spinnerLanguages);
        spinnerLanguages.setAdapter(adapter);
    }

    public void setBirthday(View v) {
        DialogDate dialogDate = new DialogDate();
        dialogDate.show(getFragmentManager(), "tag");
    }

    public void setAvatar(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(R.array.register_options_avatar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(cameraIntent, TAKE_PHOTO);
                    }
                }
                else{
                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, PICK_PHOTO);
                }
            }
        });
        builder.create();
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case PICK_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "Picked photo.");
                    Uri selectedImage = data.getData();
                    showNewAvatar(selectedImage);
                }
                break;
            case TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    Log.d(TAG, "Take photo.");
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    showNewAvatar(saveToInternalStorage(imageBitmap));
                }
        }
    }

    private String saveToInternalStorage(Bitmap bitmapImage){
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        // path to /data/data/yourapp/app_data/imageDir
        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        // Create imageDir
        File mypath = new File(directory, "TheChat" + (System.currentTimeMillis()/1000) + "Avatar.jpg");

        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(mypath);
            // Use the compress method on the BitMap object to write image to the OutputStream
            bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return mypath.getAbsolutePath();
    }

    private void showNewAvatar(Object img){
        try{
            Glide.with(this).load(img.toString()).into(imgAvatar);
            SharedPreferences.Editor editor = LoginActivity.preferences.edit();
            editor.putString(Constantes.PHOTO_URL, img.toString());
            editor.apply();
        }
        catch (Exception e) {
            Log.d(TAG, "Error en showNewAvatar: " + e.getMessage());
        }
    }

    public void register(View v){
        if(optionFemale.isChecked() || optionMale.isChecked()) {
            if (LoginActivity.preferences.getBoolean(Constantes.IS_ADULT, false)) {
                if(spinnerLanguages.getSelectedItemPosition() != 0){
                    savePerfil();
                    openChat();
                }
                else {
                    Toast.makeText(this, R.string.register_message_selectlanguage, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, R.string.register_message_under18years, Toast.LENGTH_LONG).show();
                birthday.setError("");
            }
        } else {
            Toast.makeText(this, R.string.register_message_selectgender, Toast.LENGTH_LONG).show();
        }
    }

    private void savePerfil(){
        SharedPreferences.Editor editor = LoginActivity.preferences.edit();
        editor.putString(Constantes.NAME, inputName.getText().toString());
        editor.putString(Constantes.LANGUAGE, listLanguages[spinnerLanguages.getSelectedItemPosition()]);

        if(optionFemale.isChecked()){
            editor.putString(Constantes.GENDER, Constantes.GENDER_FEMALE);
        }
        else{
            editor.putString(Constantes.GENDER, Constantes.GENDER_MALE);
        }
        editor.apply();
    }

    private void openChat(){
      //  startActivity(new Intent(this, Chat.class));
    }
}
