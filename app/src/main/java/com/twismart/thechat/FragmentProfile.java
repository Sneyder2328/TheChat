package com.twismart.thechat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.amazonaws.mobile.content.UserFileManager;


import com.amazonaws.mobile.util.ImageSelectorUtils;
import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.CountDownLatch;

import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentProfile extends Fragment implements View.OnClickListener {

    private OnFragmentInteractionListener mListener;
    public static final String TAG = "FragmentProfile";
    private static final int PICK_PHOTO = 100, TAKE_PHOTO = 101;

    public static TextView birthday;

    private EditText inputName;
    private RadioButton optionFemale, optionMale;
    private CircleImageView imgAvatar;
    private Spinner spinnerLanguages;

    private String[] listLanguages;

    NetworkInteractor networkInteractor;
    private PreferencesProfile preferencesProfile;

    private final CountDownLatch userFileManagerCreatingLatch = new CountDownLatch(1);
    private UserFileManager userFileManagerClient;

    public FragmentProfile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_profile, container, false);

        if(getActivity() instanceof Register){
            v.findViewById(R.id.linearChanges).setVisibility(View.INVISIBLE);
        }
        else{
            v.findViewById(R.id.buttonSave).setOnClickListener(this);
            v.findViewById(R.id.buttonCancel).setOnClickListener(this);
        }

        inputName = (EditText) v.findViewById(R.id.inputName);

        //
        imgAvatar = (CircleImageView) v.findViewById(R.id.imgAvatar);
        imgAvatar.setOnClickListener(this);

        //
        birthday = (TextView) v.findViewById(R.id.birthday);
        birthday.setOnClickListener(this);

        //
        optionFemale = (RadioButton) v.findViewById(R.id.optionFemale);
        optionMale = (RadioButton) v.findViewById(R.id.optionMale);

        //
        listLanguages = getResources().getStringArray(R.array.register_list_languages);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.register_list_languages, android.R.layout.simple_spinner_dropdown_item);

        spinnerLanguages = (Spinner) v.findViewById(R.id.spinnerLanguages);
        spinnerLanguages.setAdapter(adapter);

        preferencesProfile = new PreferencesProfile(getContext());
        loadDataFromProfileLocal();

        networkInteractor = new NetworkInteractor(getActivity());
        networkInteractor.createUserFileManager();

        return v;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.birthday:
                setBirthday();
                break;
            case R.id.imgAvatar:
                setAvatar();
                break;
            case R.id.buttonSave:
                register();
                break;
            case R.id.buttonCancel:
                loadDataFromProfileLocal();
                break;
        }
    }

    private void loadDataFromProfileLocal(){
        //load user name
        inputName.setText(preferencesProfile.preferences.getString(Constantes.NAME, ""));

        //load avatar
        Glide.with(this).load(preferencesProfile.preferences.getString(Constantes.PHOTO_URL, "")).into(imgAvatar);

        //load gender selected
        if(preferencesProfile.preferences.getString(Constantes.GENDER, "").equals(Constantes.GENDER_MALE)){
            optionMale.setChecked(true);
        }
        else if(preferencesProfile.preferences.getString(Constantes.GENDER, "").equals(Constantes.GENDER_FEMALE)){
            optionFemale.setChecked(true);
        }

        //load birthday
        if(preferencesProfile.preferences.getLong(Constantes.BIRTHDAY, 0) != 0){
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(preferencesProfile.preferences.getLong(Constantes.BIRTHDAY, 0));

            StringBuilder stringDate = new StringBuilder("Birthday: ").append(calendar.get(Calendar.DAY_OF_MONTH)).append("/").append(calendar.get(Calendar.MONTH)+1).append("/").append(calendar.get(Calendar.YEAR));
            birthday.setText(stringDate);
        }
    }

    private void setBirthday(){
        DialogDate dialogDate = new DialogDate();
        dialogDate.show(getActivity().getFragmentManager(), "tag");
    }

    private void setAvatar(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setItems(R.array.register_options_avatar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(i==0){
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(cameraIntent, TAKE_PHOTO);
                    }
                }
                else{
                    if (ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                        return;
                    }
                    // We have permission, so show the image selector.
                    final Intent intent = ImageSelectorUtils.getImageSelectionIntent();
                    startActivityForResult(intent, PICK_PHOTO);
                }
            }
        });
        builder.create();
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final String permissions[], final int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                final Intent intent = ImageSelectorUtils.getImageSelectionIntent();
                startActivityForResult(intent, PICK_PHOTO);
            } else {
                // Inform the user they won't be able to upload without permission.
                final Activity activity = getActivity();
                if (activity != null) {
                    final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
                    dialogBuilder.setTitle(activity.getString(R.string.content_permission_failure_title_text));
                    dialogBuilder.setMessage(activity.getString(R.string.content_permission_failure_text));
                    dialogBuilder.setNegativeButton(activity.getString(android.R.string.ok), null);
                    dialogBuilder.show();
                }
            }
        }
    }

    private File fileImg = null;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case PICK_PHOTO:
                    Uri selectedImage = data.getData();

                    showNewAvatar(selectedImage);

                    final String path = ImageSelectorUtils.getFilePathFromUri(getActivity(), selectedImage);

                    fileImg = new File(path);
                    break;
                case TAKE_PHOTO:
                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    fileImg = Util.saveToInternalStorage(getContext(), imageBitmap);
                    showNewAvatar(fileImg.getAbsolutePath());
            }
            final ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setTitle(R.string.register_text_progress_dialog_wait);
            dialog.setMessage(getString(R.string.register_text_progress_dialog_upload_file, fileImg.getName()));
            dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            dialog.setMax((int) fileImg.length());
            dialog.setCancelable(false);
            dialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    networkInteractor.uploadFile(fileImg, new NetworkInteractor.IUploadFile() {
                        @Override
                        public void onSuccess(final String url) {
                            dialog.dismiss();
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    networkInteractor.writePhotoUrl(url);
                                }
                            });
                        }

                        @Override
                        public void onProgress(String fileName, boolean isWaiting, long bytesCurrent, long bytesTotal) {
                            dialog.setProgress((int) bytesCurrent);
                        }

                        @Override
                        public void onError(String fileName, Exception ex) {
                            dialog.dismiss();
                            Toast.makeText(getContext(), getString(R.string.register_text_error_upload_file, ex.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }).start();
        }
    }

    private void showNewAvatar(Object img){
        try{
            Glide.with(this).load(img.toString()).into(imgAvatar);
            SharedPreferences.Editor editor = preferencesProfile.preferences.edit();
            editor.putString(Constantes.PHOTO_URL, img.toString());
            editor.apply();
            Log.d(TAG, "showNewAvatar: " + img.toString());
        }
        catch (Exception e) {
            Log.e(TAG, "Error en showNewAvatar: " + e.getMessage());
        }
    }


    public void register(){
        if(optionFemale.isChecked() || optionMale.isChecked()) {//if gender is selected
            if (preferencesProfile.preferences.getBoolean(Constantes.IS_ADULT, false)) {//if is over 18 age years
                if(spinnerLanguages.getSelectedItemPosition() != 0){//if lenguage is selected
                    saveProfileInLocal();
                    networkInteractor.writeProfile(getActivity() instanceof Register, new NetworkInteractor.IWriteProfileListener() {
                        @Override
                        public void onSucces() {
                            if(getActivity() instanceof MainActivity){
                                Toast.makeText(getContext(), R.string.register_message_profile_dataupdated, Toast.LENGTH_LONG).show();
                            }
                            else{
                                startActivity(new Intent(getActivity(), MainActivity.class));
                                getActivity().finish();
                            }
                        }
                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                        }
                    });
                }
                else {
                    Toast.makeText(getContext(), R.string.register_message_selectlanguage, Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getContext(), R.string.register_message_under18years, Toast.LENGTH_LONG).show();
                birthday.setError("");
            }
        } else {
            Toast.makeText(getContext(), R.string.register_message_selectgender, Toast.LENGTH_LONG).show();
        }
    }

    private void saveProfileInLocal(){
        SharedPreferences.Editor editor = preferencesProfile.preferences.edit();
        if(inputName.getText().toString().equals("")){
            editor.putString(Constantes.NAME, getString(R.string.register_text_anonymous));
        }
        else{
            editor.putString(Constantes.NAME, inputName.getText().toString());
        }
        editor.putString(Constantes.LANGUAGE, listLanguages[spinnerLanguages.getSelectedItemPosition()]);

        if(optionFemale.isChecked()){
            editor.putString(Constantes.GENDER, Constantes.GENDER_FEMALE);
        }
        else{
            editor.putString(Constantes.GENDER, Constantes.GENDER_MALE);
        }
        editor.apply();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
         //   throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
