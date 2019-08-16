package com.spottz.activity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.spottz.R;
import com.spottz.app.SpottzApplication;
import com.spottz.constant.Constants;
import com.spottz.net.NetClient;
import com.spottz.util.SessionManager;
import com.spottz.util.Utils;

public class SplashActivity extends BaseActivity {

    private static final String[] INITIAL_PERMS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_CONTACTS
    };

    private int loadflag = 0;
    private NetClient netClient;
    private ProgressBar progress;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e("========-------=====", String.valueOf(msg.what));
            if (msg.what == Constants.INT_STANBY_WHILE_LOADING) {
                gotoMainScreen(true);
            } else if (msg.what == Constants.INT_UPDATED_LOCATION_CHANGED) {
                gotoMainScreen(true);
            } else {
                loadflag |= msg.what;
                gotoMainScreen(false);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        progress = (ProgressBar) findViewById(R.id.progress);
        SpottzApplication.getInstance().handlerCategory = handler;
        SpottzApplication.getInstance().handlerLocationChanged = handler;

        SpottzApplication.getInstance().initialize();
        Utils.loadCustomFont(this);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkLocationPermission()) {
                SpottzApplication.getInstance().updatedLocation();
            }
        } else {
            SpottzApplication.getInstance().updatedLocation();
        }
    }

    private void gotoMainScreen(boolean repeatflag) {
        if (isFinishing()) {
            return;
        }

        // If all data was loaded succesfully, then open Main page
        if (loadflag == Constants.INT_LOADED_ALL_DATA && SpottzApplication.getInstance().curLocation != null) {
            SpottzApplication.getInstance().handlerCategory = null;
            SpottzApplication.getInstance().handlerLocationChanged = null;
            SessionManager sessionManager = new SessionManager(getApplicationContext());
            if(/*sessionManager.getEmailId().isEmpty()*/ !sessionManager.isLoggedIn()) {
                Intent intent = new Intent(this, LoginActivity.class);
                startActivity(intent);
            }else{
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            finish();
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
        } else {
            if (repeatflag) {
                Message msg = handler.obtainMessage(Constants.INT_STANBY_WHILE_LOADING);
                handler.sendMessageDelayed(msg, 1000);

            }
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.exit(0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (SpottzApplication.getInstance().curLocation == null)
                            SpottzApplication.getInstance().updatedLocation();
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    public void statusCheck() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        } else {
            SpottzApplication.getInstance().updatedLocation();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

}
