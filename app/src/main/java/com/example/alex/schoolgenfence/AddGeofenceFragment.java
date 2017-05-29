package com.example.alex.schoolgenfence;

import android.app.AlertDialog;
import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.support.v4.app.DialogFragment;

import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static java.security.AccessController.getContext;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * Created by Alex on 2016/12/26.
 */

public class AddGeofenceFragment extends DialogFragment{
    // region Properties

    private ViewHolder viewHolder;

    private ViewHolder getViewHolder() {
        return viewHolder;
    }

    AddGeofenceFragmentListener listener;
    public void setListener(AddGeofenceFragmentListener listener) {
        this.listener = listener;
    }

    // endregion

    // region Overrides

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_geofence, null);

        viewHolder = new ViewHolder();
        viewHolder.populate(view);

        /*//Json
        try {
            JSONObject jsonObject = new JSONObject(loadJsonFromAsset());
            JSONArray jsonArray = jsonObject.getJSONArray("Schools");
            ArrayList<HashMap<String, String>> formList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> m_li;

            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jo_inside = jsonArray.getJSONObject(i);
                String name_value = jo_inside.getString("name");
                //String address_value = jo_inside.getString("address");
                //String lat_value = jo_inside.getDouble("lat");

                //Add the values in the ArrayList
                m_li = new HashMap<String, String>();
                m_li.put("name", name_value);

                formList.add(m_li);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
     */
        //autocompletetextview
        final String[] schoolNameList = getResources().getStringArray(R.array.Schools);               //total 524 schools array end 523
        ArrayAdapter<String> schoolnameadapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,schoolNameList);
        getViewHolder().nameEditText.setAdapter(schoolnameadapter);
        //autocompletetextview

        /*getViewHolder().nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                int j = 0;

                for (int i=0; i<=523; i++){
                    if (selectedSchoolName == schoolNameList[i]) {
                        j = i;
                        getViewHolder().latitudeEditText.setText(latList[0]);
                    }
                }
                //getViewHolder().latitudeEditText.setText(latList[j]);
            }
        });*/

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.Add, null)
                .setNegativeButton(R.string.Cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        AddGeofenceFragment.this.getDialog().cancel();

                        if (listener != null) {
                            listener.onDialogNegativeClick(AddGeofenceFragment.this);
                        }
                    }
                });

        final AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (dataIsValid()) {
                            NamedGeofence geofence = new NamedGeofence();
                            geofence.name = getViewHolder().nameEditText.getText().toString();
                            //geofence.address = (matchingaddress());
                            geofence.latitude = Double.parseDouble(matchinglat());
                            geofence.longitude = Double.parseDouble(matchinglng());
                            geofence.radius = Float.parseFloat(matchingradius()) * 1000.0f;
                            //SMS
                            geofence.phoneNumber = getViewHolder().phoneNoEditText.getText().toString();

                            if (listener != null) {
                                listener.onDialogPositiveClick(AddGeofenceFragment.this, geofence);
                                dialog.dismiss();
                            }
                        } else {
                            showValidationErrorToast();
                        }
                    }

                });

            }
        });

        return dialog;
    }

    // endregion

    // region Private


    private boolean dataIsValid() {
        boolean validData = true;

        String name = getViewHolder().nameEditText.getText().toString();
        String phone = getViewHolder().phoneNoEditText.getText().toString();

        if(TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            validData = false;
        } else {
            validData = false;
            String schoolname = getViewHolder().nameEditText.getText().toString();
            String[] nameList = getResources().getStringArray(R.array.Schools);
            for (int i = 0; i<=523; i++) {
                if (nameList[i].equals(schoolname)) {
                    validData = true;
                }
            }
        }

        return validData;
    }

    private void showValidationErrorToast() {
        Toast.makeText(getActivity(), getActivity().getString(R.string.Toast_Validation), Toast.LENGTH_SHORT).show();
    }

    // endregion

    // region Interfaces

    public interface AddGeofenceFragmentListener {
        void onDialogPositiveClick(android.support.v4.app.DialogFragment dialog, NamedGeofence geofence);
        void onDialogNegativeClick(android.support.v4.app.DialogFragment dialog);
    }

    // endregion

    // region Inner classes

    static class ViewHolder {
        AutoCompleteTextView nameEditText;
        EditText phoneNoEditText;

        public void populate(View v) {
            //autocompletetextview
            nameEditText = (AutoCompleteTextView) v.findViewById(R.id.fragment_add_geofence_name);
            //autocompletetextview
            //SMS
            phoneNoEditText = (EditText) v.findViewById(R.id.fragment_add_parent_phone_number);
            //SMS
            phoneNoEditText.setHint(String.format(v.getResources().getString(R.string.Hint_PhoneNo)));
        }
    }


    private String matchinglat() {
        int j = 0;
        String lat;
        String[] latList = getResources().getStringArray(R.array.latitude);
        String[] nameList = getResources().getStringArray(R.array.Schools);
        String name = getViewHolder().nameEditText.getText().toString();

        for (int i=0; i<=523; i++) {
            if(nameList[i].equals(name)) {
               j = i;
            }
        }

        lat = latList[j];
        return lat;
    }

    private String matchinglng() {
        int j = 0;
        String lng;
        String[] lngList = getResources().getStringArray(R.array.longitude);
        String[] nameList = getResources().getStringArray(R.array.Schools);
        String name = getViewHolder().nameEditText.getText().toString();

        for (int i=0; i<=523; i++) {
            if(nameList[i].equals(name)) {
                j = i;
            }
        }

        lng = lngList[j];
        return lng;
    }

    private String matchingradius() {
        int j = 0;
        String radius;
        String[] radiusList = getResources().getStringArray(R.array.radius);
        String[] nameList = getResources().getStringArray(R.array.Schools);
        String name = getViewHolder().nameEditText.getText().toString();

        for (int i=0; i<=523; i++) {
            if(nameList[i].equals(name)) {
                j = i;
            }
        }

        radius = radiusList[j];
        return radius;
    }

    /*private String matchingaddress() {
        int j = 0;
        String lng;
        String[] lngList = getResources().getStringArray(R.array.address);
        String[] nameList = getResources().getStringArray(R.array.Schools);
        String name = getViewHolder().nameEditText.getText().toString();

        for (int i=0; i<=523; i++) {
            if(nameList[i].equals(name)) {
                j = i;
            }
        }

        lng = lngList[j];
        return lng;
    }*/



    public String loadJsonFromAsset() {
        String json = null;
        try {
            InputStream inputStream = getActivity().getAssets().open("schools.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

}