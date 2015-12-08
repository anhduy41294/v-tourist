package com.group5.controller;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.group5.model.Place;
import com.group5.service.PlaceServices;
import com.parse.ParseException;

import org.w3c.dom.Text;

/**
 * Created by Salmon on 11/27/2015.
 * Detail clearly about this place
 */
public class InfomationFragment  extends android.support.v4.app.Fragment {

    Place place;
    TextView txtPlaceName;
    TextView txtAddress;
    TextView txtPhone;
    TextView txtDescription;

    @Override
    public View onCreateView(LayoutInflater inflater,  ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_information, container, false);

        getFormWidget(view);
        getDataFromService();
        addDataToView();
        return view;
    }

    /**
     * Function get Place Data from Service
     */
    protected void getDataFromService(){
        DataFromOnePlace asyncTask = new DataFromOnePlace();
        asyncTask.execute();
    }

    /**
     * Fill data from Place to View
     */
    protected void addDataToView(){
        txtPlaceName.setText(place.getPlaceName());
        txtPhone.setText(place.getPhone());
        txtAddress.setText(place.getAddress());
        txtDescription.setText(place.getLongDescription());
    }

    /**
     * Get Form Widget
     */
    protected void getFormWidget(View v) {
        txtAddress = (TextView) v.findViewById(R.id.txtAddress);
        txtPlaceName = (TextView) v.findViewById(R.id.txtPlaceName);
        txtPhone = (TextView) v.findViewById(R.id.txtPhone);
        txtDescription = (TextView) v.findViewById(R.id.txtDescription);
    }

    /**
     * AsyncTask
     */
    private class DataFromOnePlace extends AsyncTask<Void, Long, Place> {


        @Override
        protected Place doInBackground(Void... params) {
            try {
                return PlaceServices.getPlace(GlobalVariable.idGlobalPlaceCurrent);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return place;
        }

        @Override
        protected void onPostExecute(Place mplace) {
            super.onPostExecute(place);
            place = mplace;
        }
    }
}
