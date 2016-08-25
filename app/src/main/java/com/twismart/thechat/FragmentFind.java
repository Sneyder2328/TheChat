package com.twismart.thechat;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class FragmentFind extends Fragment {

    private OnFragmentInteractionListener mListener;

    private CheckBox checkFemale, checkMale, checkMyLanguage;
    private Spinner spinnerDistance, spinnerAgeMin, spinnerAgeMax;
    private Button btnStart;

    public FragmentFind() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_find, container, false);

        checkFemale = (CheckBox) v.findViewById(R.id.checkFemale);
        checkMale = (CheckBox) v.findViewById(R.id.checkMale);

        //
        spinnerDistance = (Spinner) v.findViewById(R.id.spinnerDistance);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.find_options_distance, android.R.layout.simple_spinner_dropdown_item);
        spinnerDistance.setAdapter(adapter);

        //
        spinnerAgeMin = (Spinner) v.findViewById(R.id.spinnerAgeMin);
        adapter = ArrayAdapter.createFromResource(getContext(), R.array.find_options_age, android.R.layout.simple_spinner_dropdown_item);
        spinnerAgeMin.setAdapter(adapter);

        spinnerAgeMax = (Spinner) v.findViewById(R.id.spinnerAgeMax);
        adapter = ArrayAdapter.createFromResource(getContext(), R.array.find_options_age, android.R.layout.simple_spinner_dropdown_item);
        spinnerAgeMax.setAdapter(adapter);
        spinnerAgeMax.setSelection(adapter.getCount()-1);

        //
        checkMyLanguage = (CheckBox) v.findViewById(R.id.checkInMyLanguage);

        //
        btnStart = (Button) v.findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePreferencesFind();
                startActivity(new Intent(getActivity(), ChatActivity.class));
            }
        });
        return v;
    }


    private void savePreferencesFind(){
        PreferencesFind preferencesFind = new PreferencesFind(getContext());

        if(checkFemale.isChecked() && !checkMale.isChecked()){
            preferencesFind.setGender(Constantes.GENDER_FEMALE);
        }
        else if(!checkFemale.isChecked() && checkMale.isChecked()){
            preferencesFind.setGender(Constantes.GENDER_MALE);
        }
        else{
            preferencesFind.setGender(Constantes.GENDER_FEMALE + Constantes.SEPARATOR +Constantes.GENDER_MALE);
        }
        int distanceMax;
        switch (spinnerDistance.getSelectedItemPosition()){
            case 0:
                distanceMax = 10;
                break;
            case 1:
                distanceMax = 50;
                break;
            case 2:
                distanceMax = 100;
                break;
            case 3:
                distanceMax = 250;
                break;
            case 4:
                distanceMax = 500;
                break;
            case 5:
                distanceMax = 1000;
                break;
            case 6:
                distanceMax = 2500;
                break;
            default:
                distanceMax = 100000;
        }
        preferencesFind.setDistanceMax(distanceMax);


        Calendar nowValid = Calendar.getInstance();

        String ageMin = spinnerAgeMin.getSelectedItem().toString();
        Date dateMin = nowValid.getTime();

        try{
            dateMin.setYear(dateMin.getYear() - Integer.parseInt(ageMin));
            preferencesFind.setAgeMin(dateMin.getTime());
        }
        catch (Exception e){
            dateMin.setYear(dateMin.getYear() - 0);
            preferencesFind.setAgeMin(dateMin.getTime());
        }

        String ageMax = spinnerAgeMax.getSelectedItem().toString();
        Date dateMax = nowValid.getTime();

        try{
            dateMax.setYear(dateMax.getYear() - Integer.parseInt(ageMax));
            preferencesFind.setAgeMax(dateMax.getTime());
        }
        catch (Exception e){
            dateMax.setYear(dateMax.getYear() - 150);
            preferencesFind.setAgeMax(dateMax.getTime());
        }

        preferencesFind.setInMyLanguage(checkMyLanguage.isChecked());
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
            //throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
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
