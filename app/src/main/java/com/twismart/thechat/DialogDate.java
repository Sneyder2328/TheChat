package com.twismart.thechat;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by sneyd on 8/3/2016.
 **/

public class DialogDate extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new DatePickerDialog(getActivity(), this, 1998, 0, 1);
    }

    @Override
    public void onDateSet(DatePicker arg0, int year, int month, int day) {
        Calendar birthDateMore18 = Calendar.getInstance();
        birthDateMore18.set(Calendar.YEAR, year + 18);
        birthDateMore18.set(Calendar.MONTH, month);
        birthDateMore18.set(Calendar.DAY_OF_MONTH, day);

        Calendar birthDate = Calendar.getInstance();
        birthDate.set(Calendar.YEAR, year);
        birthDate.set(Calendar.MONTH, month);
        birthDate.set(Calendar.DAY_OF_MONTH, day);

        SharedPreferences.Editor editor = LoginActivity.preferences.edit();
        editor.putLong(Constantes.BIRTHDAY, birthDate.getTime().getTime());

        Calendar dateNow = Calendar.getInstance();
        if(dateNow.after(birthDateMore18)){//is over 18 years
            editor.putBoolean(Constantes.IS_ADULT, true);
            Register.birthday.setError(null);
        }
        else{
            editor.putBoolean(Constantes.IS_ADULT, false);
        }
        editor.apply();

        StringBuilder date = new StringBuilder("Birthday: ").append(day).append("/").append(month+1).append("/").append(year);
        Register.birthday.setText(date);

        cumple(18, 59, birthDate.getTime());
    }

    private void cumple(int ageMin, int ageMax, Date date){
        Calendar nowValid = Calendar.getInstance();

        Date dateMin = nowValid.getTime();
        dateMin.setYear(dateMin.getYear() - ageMin);

        Date dateMax = nowValid.getTime();
        dateMax.setYear(dateMax.getYear() - ageMax);

        long min = dateMin.getTime(), max = dateMax.getTime(), timeMilli = date.getTime();
        Log.d("DATA", "min: " + min + " birthday " + timeMilli + " max: " + max + " date: " + date.toString());
        Log.d("DARA", "AgeValid = " + (timeMilli <= min && timeMilli >= max));
    }
}
