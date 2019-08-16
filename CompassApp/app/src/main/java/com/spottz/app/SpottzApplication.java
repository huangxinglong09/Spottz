package com.spottz.app;

import android.Manifest;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.se.omapi.Session;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.spottz.constant.Constants;
import com.spottz.custom.AeSimpleSHA1;
import com.spottz.model.CategoryModel;
import com.spottz.model.LocalScoreModel;
import com.spottz.model.QuestionModel;
import com.spottz.model.SpotsModel;
import com.spottz.net.BaseJsonRes;
import com.spottz.net.NetClient;
import com.spottz.util.Constant;
import com.spottz.util.SessionManager;
import com.spottz.util.Utils;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import javax.net.ssl.HttpsURLConnection;


public class SpottzApplication extends Application implements LocationListener {
    public static final String TAG = SpottzApplication.class.getSimpleName();
    private static SpottzApplication instance;
    public CategoryModel currentItem;
    public SpotsModel curSpots;
    public Handler handlerSpotsLoaded;
    public Handler handlerCategory;
    public Handler handlerLocationChanged;
    public static boolean bHasGPS = false;

    private NetClient netClient;
    private int iLoadedCategoryID = 0;
    public int iCurrentSpot = 0;
    public String strUDID;
    public ArrayList<CategoryModel> arrCategory = new ArrayList<>();
    public ArrayList<LocalScoreModel> arrScores = new ArrayList<>();
    public Location curLocation;
    public String strID;
    public static final boolean testmode = false;
    private SessionManager sessionManager;
    public String strShareLink;
    private LocationManager locationManager;
    public Bitmap bitmap;

    // texts
    // when start_route function call, displayed message
    public String startRouteAlertMessage;
    public String signupText;
    public String loginText;
    public String skipText;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        netClient = new NetClient(this);

        sessionManager = new SessionManager(getApplicationContext());
    }
    public void initialize(){
        strUDID = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (strUDID == null) {
            strUDID = this.loadUDID();
            if (strUDID == null) {
                String strDate = Calendar.getInstance().getTime().toString();
                try {
                    strUDID = AeSimpleSHA1.SHA1(strDate);
                    this.saveUDID(strUDID);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        } else {
            //strUDID = "AffDB";
        }

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.spottz",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.e("********KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadTextsFromCms();
                loadCategory();
                //loadCompletedScores();
            }
        }, 2000);
    }
    public void startRoute(int categoryId) {

        SimpleDateFormat defaultDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        String strStartDate = defaultDateFormat.format(calendar.getTime());

        SimpleDateFormat defaultTimeFormat = new SimpleDateFormat("HH:mm:ss");
        String strStartTime = defaultTimeFormat.format(calendar.getTime());
        String strEmail = sessionManager.getEmailId();

        RequestParams param = new RequestParams();
        param.put("device_token", strUDID);
        param.put("device_type", "Android");
        param.put("Startdate", strStartDate);
        param.put("Starttime", strStartTime);
        param.put("category_id", String.valueOf(categoryId));
        param.put("email", strEmail);

        netClient.post(Utils.getAppURL("/lappapi/post/score/"), param, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {
                JSONObject json = (JSONObject) response;
                String strURL = json.optString("link");
                String[] lstParams = strURL.split("=");
                if (lstParams.length > 1) {
                    strID = lstParams[lstParams.length - 1];
                }
                strShareLink = strURL;
            }

            @Override
            public void onMyFailure(int status) {

            }
        });
    }

    public void updatedLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (locationManager == null) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.GPS_MIN_TIME, Constants.GPS_MIN_DISTANCE, this);
                curLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.GPS_MIN_TIME, Constants.GPS_MIN_DISTANCE, this);
                Location loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (loc != null)
                    curLocation = loc;
            }
        }
        if(curLocation == null)
            Toast.makeText(getApplicationContext(), "GPS function is not supported in your device.", Toast.LENGTH_SHORT).show();
    }

    private void saveUDID(String udid) {
        SharedPreferences preferences = getSharedPreferences("appconfigure", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        if (udid == null)
            editor.remove("udid");
        else
            editor.putString("udid", udid);
        editor.commit();
    }

    public String loadUDID() {
        SharedPreferences preferences = getSharedPreferences("appconfigure", Context.MODE_PRIVATE);
        String udid = preferences.getString("udid", null);
        return udid;
    }

    public class loadgCompletedScoresTask extends AsyncTask<String, Void, String> {
        String response = "";

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            response = getCompletedScores();
            return "";

        }

        @Override
        protected void onPostExecute(String result) {
            if (!response.isEmpty()) {
                JSONArray array = null;
                try {
                    array = new JSONArray(response);
                    JSONObject obj;
                    for (int idx = 0; idx < array.length(); idx++) {
                        obj = array.optJSONObject(idx);
                        arrScores.add(new LocalScoreModel(obj));
                    }

                    sendNotification(Constants.INT_LOADED_SCORE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Please check your internet connections!", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    private String getCompletedScores(){
        String strEmail = sessionManager.getEmailId();
        arrScores.clear();
        String strUrl = Utils.getAppURL("/lappapi/get/score/?email=") + strEmail;
        URL url;
        // Create a new HttpClient and Post Header
        String response = "";
        try {
            url = new URL(strUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }
    public void loadCompletedScores() {
        new loadgCompletedScoresTask().execute();
/*
        arrScores.clear();
        String strUrl = Utils.getAppURL("/lappapi/get/score/?device_token=") + strUDID;
        NetClient netClient = new NetClient(this);
        netClient.get(strUrl, null, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {
                JSONArray array = (JSONArray) response;
                JSONObject obj;
                for (int idx = 0; idx < array.length(); idx++) {
                    obj = array.optJSONObject(idx);
                    arrScores.add(new LocalScoreModel(obj));
                }

                sendNotification(Constants.INT_LOADED_SCORE);
            }

            @Override
            public void onMyFailure(int status) {
            }
        });
        */
    }

    private void sendNotification(int type) {
        if (handlerCategory != null) {
            Message message = handlerCategory.obtainMessage(type);
            handlerCategory.sendMessageDelayed(message, 0);
        }
    }

    public class loadCategoryTask extends AsyncTask<String, Void, String> {
        String response = "";

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            response = getCategory();
            return "";

        }

        @Override
        protected void onPostExecute(String result) {
            if (!response.isEmpty()) {
                JSONArray array = null;
                try {
                    array = new JSONArray(response);
                    JSONObject obj;
                    for (int idx = 0; idx < array.length(); idx++) {
                       obj = array.optJSONObject(idx);
                        if (!obj.has("id"))
                            continue;
                        arrCategory.add(new CategoryModel(obj));
                    }
                    sendNotification(Constants.INT_LOADED_CATEGORY_LIST);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(getApplicationContext(), "Please check your internet connections!", Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {

        }
    }

    private String getCategory(){
        arrCategory.clear();
        String strUrl = Utils.getAppURL("/lappapi/get/spots_categories/?token=" + Utils.API_TOKEN + "&page=1");
        URL url;
        // Create a new HttpClient and Post Header
        String response = "";
        try {
            url = new URL(strUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            int responseCode=conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
            }
            else {
                response="";

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private void loadTextsFromCms() {
        // singup text
        netClient.get(Utils.getAppURL("/lappapi/get/page/13"), null, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {
                JSONObject obj = (JSONObject) response;
                if (obj.has("title")) {
                    String strTitle = obj.optString("title");
                }

                if (obj.has("content")) {
                    String strContent = obj.optString("content");
                    String contents = "<body style='background-color: #a8dcf2'>" + strContent + "</body>";
                    signupText = contents;
                    sendNotification(Constants.INT_SIGNUPTEXT_LOADED);
                }
            }

            @Override
            public void onMyFailure(int status) {

            }
        });

        // login text
        netClient.get(Utils.getAppURL("/lappapi/get/page/14"), null, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {
                JSONObject obj = (JSONObject) response;
                if (obj.has("title")) {
                    String strTitle = obj.optString("title");
                }

                if (obj.has("content")) {
                    String strContent = obj.optString("content");
                    String contents = "<body style='background-color: #a8dcf2'>" + strContent + "</body>";
                    loginText = contents;
                    sendNotification(Constants.INT_LOGINTEX_LOADED);
                }
            }

            @Override
            public void onMyFailure(int status) {

            }
        });

        // skip text
        netClient.get(Utils.getAppURL("/lappapi/get/page/15"), null, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {
                JSONObject obj = (JSONObject) response;
                if (obj.has("title")) {
                    String strTitle = obj.optString("title");
                }

                if (obj.has("content")) {
                    String strContent = obj.optString("content");
                    String contents = "<body style='background-color: #a8dcf2'>" + strContent + "</body>";
                    skipText = contents;
                    sendNotification(Constants.INT_SKIPTEXT_LOADED);
                }
            }

            @Override
            public void onMyFailure(int status) {

            }
        });

        // startRoute Alert Message
        netClient.get(Utils.getAppURL("/lappapi/get/page/16"), null, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {
                JSONObject obj = (JSONObject) response;
                if (obj.has("title")) {
                    String strTitle = obj.optString("title");
                }

                if (obj.has("content")) {
                    String strContent = obj.optString("content");
                    String contents = "<body style='background-color: #a8dcf2'>" + strContent + "</body>";
                    startRouteAlertMessage = contents;
                    sendNotification(Constants.INT_STARTROUTETEXT_LOADED);
                }
            }

            @Override
            public void onMyFailure(int status) {

            }
        });
    }

    public void loadCategory() {
        new loadCategoryTask().execute();
        /*arrCategory.clear();
        String strUrl = Utils.getAppURL("/lappapi/get/spots_categories/?token=" + Utils.API_TOKEN + "&page=1");
        netClient.get(strUrl, null, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {
                JSONArray array = (JSONArray) response;
                JSONObject obj;
                for (int idx = 0; idx < array.length(); idx++) {
                    obj = array.optJSONObject(idx);
                    if (!obj.has("id"))
                        continue;
                    arrCategory.add(new CategoryModel(obj));
                }
                sendNotification(Constants.INT_LOADED_CATEGORY_LIST);
            }

            @Override
            public void onMyFailure(int status) {
                Log.e("=========", "spots_categories === Failed");
            }
        });
        */
    }

    public static SpottzApplication getInstance() {
        return instance;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.gc();
    }


    public void loadSpots(int categoryid) {

        if (currentItem == null && categoryid == iLoadedCategoryID) {
            return;
        }

        curSpots = null;
        iCurrentSpot = 0;

        String strURL = String.format(Utils.getAppURL("/lappapi/get/spots/?token=" + Utils.API_TOKEN + "&page=1&category_id=%d"), categoryid);
        netClient.get(strURL, null, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {
                JSONArray jsonResult = (JSONArray) response;
                curSpots = new SpotsModel(jsonResult);
                if (handlerSpotsLoaded != null) {
                    synchronized (handlerSpotsLoaded) {
                        Message msg = handlerSpotsLoaded.obtainMessage();
                        handlerSpotsLoaded.sendMessageDelayed(msg, 0);
                    }
                }
            }

            @Override
            public void onMyFailure(int status) {
                Log.e("=========", "spots === Failed");
            }
        });
    }

    public QuestionModel getCurrentQuestion() {
        if (curSpots == null || curSpots.arrQuestoins.size() <= iCurrentSpot)
            return null;

        return curSpots.arrQuestoins.get(iCurrentSpot);
    }

    public boolean isFinalSpot() {
        if (curSpots == null || curSpots.arrQuestoins.size() <= iCurrentSpot + 1)
            return true;

        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!location.hasAccuracy())
            return;

        if (location.getProvider().equals(LocationManager.GPS_PROVIDER))
            bHasGPS = true;

        if (bHasGPS && location.getProvider().equals(LocationManager.NETWORK_PROVIDER))
            return;

        curLocation = location;
        if (handlerLocationChanged != null) {
            Message msg = handlerLocationChanged.obtainMessage(Constants.INT_UPDATED_LOCATION_CHANGED);
            handlerLocationChanged.sendMessage(msg);
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Log.e("=> onStatusChanged", s);
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public LocalScoreModel getScoreInfo(int catid) {
        for (int idx = 0; idx < arrScores.size(); idx++) {
            LocalScoreModel item = arrScores.get(idx);
            if (item.catid == catid)
                return item;
        }

        return null;
    }

    public void saveScore(int iID, int totalScore, long endtime) {
        LocalScoreModel model = new LocalScoreModel(null);
        model.catid = iID;
        model.score = totalScore;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(endtime);

        SimpleDateFormat defaultDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        model.date = defaultDateFormat.format(cal.getTime());
        arrScores.add(model);
    }

    public void clearImage() {
    }
    public boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }

        return false;
    }

}