package com.spottz.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.spottz.R;
import com.spottz.app.SpottzApplication;
import com.spottz.net.BaseJsonRes;
import com.spottz.net.NetClient;
import com.spottz.util.SessionManager;
import com.spottz.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends BaseActivity {

    private Context mContext;

    private Button but_apply;
    private Button but_cancel;

    private EditText edit_emailid;
    private EditText edit_name;
    private EditText edit_address;
    private EditText edit_telephone;

    private String mEmailId, mName, mAddress, mPhonenumber;
    private ProgressDialog pDialog;

    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        mContext = getApplicationContext();
        sessionManager = new SessionManager(mContext);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle("Mijn gegevens");

        edit_emailid = (EditText)findViewById(R.id.edit_emailid);
        edit_name = (EditText)findViewById(R.id.edit_name);
        edit_address = (EditText)findViewById(R.id.edit_address);
        edit_telephone = (EditText)findViewById(R.id.edit_telephone);
        but_apply = (Button)findViewById(R.id.but_apply);
        but_cancel = (Button)findViewById(R.id.but_cancel);

        but_apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmailId = edit_emailid.getText().toString();
                mName = edit_name.getText().toString();
                mAddress = edit_address.getText().toString();
                mPhonenumber = edit_telephone.getText().toString();

                if (!SpottzApplication.getInstance().isConnected()) {



                } else if (!checkEmailId() || !checkName()) {

                } else {

                    loginUser(mEmailId, mName, mPhonenumber, mAddress);
                }
            }
        });
        but_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();

            }
        });
        mEmailId = sessionManager.getEmailId();
        mName = sessionManager.getUserName();
        mAddress = sessionManager.getAddress();
        mPhonenumber = sessionManager.getPhonenumber();
        edit_emailid.setText(mEmailId);
        edit_address.setText(mAddress);
        edit_name.setText(mName);
        edit_telephone.setText(mPhonenumber);
        initpDialog();

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }

            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }
    public boolean checkName() {
        mName = edit_name.getText().toString();
        edit_name.setError(null);
        if(mName.length() < 3){
            edit_name.setError("invalid name");
            return false;
        }
        return true;
    }
    public Boolean checkEmailId() {

        mEmailId = edit_emailid.getText().toString();

        edit_emailid.setError(null);

        if (mEmailId.length() == 0) {

            edit_emailid.setError("DIt veld is verplicht");

            return false;
        }


        if (!Utils.isValidEmail(mEmailId)) {

            edit_emailid.setError("email format is wronng");

            return false;
        }

        if (mEmailId.length() < 5) {

            edit_emailid.setError("invalid email");

            return false;
        }
        return true;
    }
    public void loginUser(String strEmail, String strName, String strPhone, String strAddress) {
        String strFirstName = strName, strLastName = "";
        String[] strArray = strName.split(" ");
        if(strArray!= null && strArray.length>=2) {
            strFirstName = strArray[0];
            strLastName = strArray[1];
        }
        String strUDID = SpottzApplication.getInstance().strUDID;
        String strUrl = String.format(Utils.getAppURL("/lappapi/facebooklogin/get/?device_token=" + strUDID + "&token=" + Utils.API_TOKEN +
                "&email=" + strEmail + "&firstname=" + strFirstName + "&lastname=" + strLastName + "&phone=" + strPhone + "&address=" + strAddress + "&platform=Android&fbid=0" ));

        NetClient netClient = new NetClient(this);
        showpDialog();
        netClient.get(strUrl, null, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {

                try {
                    JSONObject obj = (JSONObject) response;
                    String strEmail = obj.getString("email");
                    String strFirstName = obj.getString("firstname");
                    String strLastName = obj.getString("lastname");
                    String strName = strFirstName;
                    if(!strLastName.isEmpty())
                        strName = strName + " " + strLastName;
                    String strPhone = obj.getString("phone");
                    String strAddress = obj.getString("address");
                    sessionManager.setEmailId(strEmail);
                    sessionManager.setUserName(strName);
                    sessionManager.setAddress(strAddress);
                    sessionManager.setPhonenumber(strPhone);
                    finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //sendNotification(Constants.INT_LOADED_SCORE);
                hidepDialog();
            }

            @Override
            public void onMyFailure(int status) {
                hidepDialog();
            }
        });
    }
    protected void initpDialog() {

        pDialog = new ProgressDialog(ProfileActivity.this);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }
}
