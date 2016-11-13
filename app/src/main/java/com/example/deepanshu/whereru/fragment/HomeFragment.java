package com.example.deepanshu.whereru.fragment;


import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.deepanshu.whereru.R;
import com.example.deepanshu.whereru.activity.FirstRun;
import com.example.deepanshu.whereru.other.Locations;
import com.example.deepanshu.whereru.other.MobileNumberPreferences;
import com.example.deepanshu.whereru.other.PermissionForGPS;
import com.example.deepanshu.whereru.other.User;

public class HomeFragment extends Fragment {

    private static final int requestCode = 0;
    ProgressDialog progressDoalog;
    private TextView userMobileNumberTextBox, latitudeTextView, longitudeTextView, lastLocationTimeTextView;
    private User user;
    private Locations locations;
    private Location loc;
    private Handler handler;
    private PermissionForGPS permissionForGPS;
    private int cylce = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        userMobileNumberTextBox = (TextView) getActivity().findViewById(R.id.user_mobile_no);
        latitudeTextView = (TextView) getActivity().findViewById(R.id.latitude_cordinates);
        longitudeTextView = (TextView) getActivity().findViewById(R.id.longitude_cordinates);
        lastLocationTimeTextView = (TextView) getActivity().findViewById(R.id.last_location_time);
        handler = new Handler();
        user = new User(getActivity().getApplicationContext());
        locations = new Locations(getActivity().getApplicationContext(), handler);

        locationUpdate();
        locations.setLocationListener(new Locations.locationChanging() {
            @Override
            public void locationChanged(Location loc) {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy HH:mm:ss");
                user.setLatitude(loc.getLatitude());
                user.setLongitude(loc.getLongitude());
                user.setLastLocationUpdateTime(sdf.format(java.util.Calendar.getInstance().getTime()));

                latitudeTextView.setText("Latitude: " + loc.getLatitude());
                longitudeTextView.setText("Longitude: " + loc.getLongitude());
                lastLocationTimeTextView.setText("Last Locations Time: " + user.getLastLocationUpdateTime());
            }
        });

        if (savedInstanceState == null) {
            getMobileNo();
        } else {
            user.setMobNo(savedInstanceState.getString("MOBILE"));
            userMobileNumberTextBox.append(user.getMobNo());
        }
        if (loc != null) {
            latitudeTextView.setText("Latitude: " + user.getLatitude());
            longitudeTextView.setText("Longitude: " + user.getLongitude());
            lastLocationTimeTextView.setText("Last Locations Time: " + user.getLastLocationUpdateTime());
        }
    }

    private boolean locationUpdate() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd MMM yyyy HH:mm:ss");

        if (!locations.isGpsEnabled()) {
            FragmentManager manager = getFragmentManager();
            permissionForGPS = new PermissionForGPS();
            permissionForGPS.show(manager, "GPS_PERMISSION");
            cylce = 1;
            return true;
        } else {

            progressDoalog = new ProgressDialog(getActivity());

            progressDoalog.setMessage("Loading Location....");
            progressDoalog.setTitle("LOCATION");
            progressDoalog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDoalog.show();
            int i = 0;

            while (loc == null && i <= 15) {
                try {
                    Thread.sleep(1000);
                    i++;
                    loc = locations.getLocation();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (i <= 15) {
                    progressDoalog.dismiss();
                }
            }
            progressDoalog.dismiss();

            if (loc != null) {
                user.setLatitude(loc.getLatitude());
                user.setLongitude(loc.getLongitude());
                user.setLastLocationUpdateTime(sdf.format(java.util.Calendar.getInstance().getTime()));
            } else {
                latitudeTextView.setText("Unable to get a location.Please try Later");
            }
            return false;

        }
    }

    private void getMobileNo() {
        String mobileNo = MobileNumberPreferences.getMobileNo(getActivity());
        if (mobileNo != null) {
            user.setMobNo(mobileNo);
            userMobileNumberTextBox.setText(mobileNo);
            Toast.makeText(getActivity(),mobileNo, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("MOBILE", user.getMobNo());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();

        locationUpdate();
        if (loc != null) {
            latitudeTextView.setText("Latitude: " + user.getLatitude());
            longitudeTextView.setText("Longitude: " + user.getLongitude());
            lastLocationTimeTextView.setText("Last Locations Time: " + user.getLastLocationUpdateTime());
        }
        if (permissionForGPS != null)
            permissionForGPS.dismiss();
    }
}