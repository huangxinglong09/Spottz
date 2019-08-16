package com.spottz.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.spottz.R;
import com.spottz.app.SpottzApplication;
import com.spottz.dialog.ResetPasswordDialog;
import com.spottz.net.BaseJsonRes;
import com.spottz.net.NetClient;
import com.spottz.util.SessionManager;
import com.spottz.util.Utils;
import com.spottz.view.OpenSansButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";
    private Context mContext;

    private LinearLayout lin_register;
    private LinearLayout lin_login;
    private LinearLayout lin_skip;

    private NetClient netClient;

    private WebView loginWebView;
    private WebView signupWebView;
    private WebView skipWebView;

    private LinearLayout lin_starten;
    private LinearLayout lin_inlogin;
    private LinearLayout lin_overslaan;
    private OpenSansButton btnFBLogin;

    private TextView txt_forgotpassword;
    private TextView txt_register;
    private TextView txt_login;
    private TextView txt_overslaan;


    private EditText edit_emailid;
    private EditText edit_name;
    private EditText edit_emaillogin;
    private EditText edit_password;

    private String mEmailId, mName;
    private String mEmailLogin, mPassword;

    private CallbackManager mCallbackManager;

    private ProgressDialog pDialog;
    private ResetPasswordDialog resetDialog;
    private SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        netClient = new NetClient(this);

        mContext = getApplicationContext();
        sessionManager = new SessionManager(mContext);
        ActionBar actionBar = getSupportActionBar();

        txt_register = (TextView) findViewById(R.id.txt_register);
        txt_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideAllLoginViews();
                txt_register.setAllCaps(true);
                lin_register.setVisibility(View.VISIBLE);
            }
        });
        txt_login = (TextView) findViewById(R.id.txt_login);
        txt_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideAllLoginViews();
                txt_login.setAllCaps(true);
                lin_login.setVisibility(View.VISIBLE);
            }
        });
        txt_overslaan = (TextView) findViewById(R.id.txt_overslaan);
        txt_overslaan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideAllLoginViews();
                txt_overslaan.setAllCaps(true);
                lin_skip.setVisibility(View.VISIBLE);
            }
        });
        txt_forgotpassword = (TextView) findViewById(R.id.txt_forgotpassword);
        txt_forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetDialog = new ResetPasswordDialog(LoginActivity.this);
                resetDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String strEmail = resetDialog.edit_emailid.getText().toString().trim();
                        showpDialog();
                        resetPassword(strEmail);
                    }
                });
                resetDialog.show();
            }
        });
        lin_register = (LinearLayout) findViewById(R.id.lin_register);
        lin_login = (LinearLayout) findViewById(R.id.lin_login);
        lin_skip = (LinearLayout) findViewById(R.id.lin_skip);
        edit_emailid = (EditText) findViewById(R.id.edit_emailid);
        edit_name = (EditText) findViewById(R.id.edit_name);
        edit_emaillogin = (EditText) findViewById(R.id.edit_emaillogin);
        edit_password = (EditText) findViewById(R.id.edit_password);
        btnFBLogin = (OpenSansButton) findViewById(R.id.btnFBLogin);
        lin_starten = (LinearLayout) findViewById(R.id.lin_starten);
        lin_starten.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmailId = edit_emailid.getText().toString();
                mName = edit_name.getText().toString();

                if (!SpottzApplication.getInstance().isConnected()) {



                } else if (!checkEmailId() || !checkName()) {

                } else {

                    registerUser(mEmailId, mName, "", "");
                }

            }
        });
        lin_inlogin = (LinearLayout) findViewById(R.id.lin_inlogin);
        lin_inlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mEmailLogin = edit_emaillogin.getText().toString();
                mPassword = edit_password.getText().toString();

                if (!SpottzApplication.getInstance().isConnected()) {



                } else if (!checkEmailLogin() || !checkPassword()) {


                } else {

                    loginUser(mEmailLogin, mPassword);
                }

            }
        });

        btnFBLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginWithFacebook();
            }
        });
        lin_overslaan = (LinearLayout) findViewById(R.id.lin_overslaan);
        lin_overslaan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        signupWebView = (WebView)findViewById(R.id.signup_webView);
        loginWebView = (WebView)findViewById(R.id.login_webView);
        skipWebView = (WebView)findViewById(R.id.skip_webView);

        loginWebView.loadData(SpottzApplication.getInstance().loginText, "text/html", null);
        signupWebView.loadData(SpottzApplication.getInstance().signupText, "text/html", null);
        skipWebView.loadData(SpottzApplication.getInstance().skipText, "text/html", null);

        mCallbackManager = CallbackManager.Factory.create();
        initpDialog();
    }

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

    }
    private void loginWithFacebook() {

        Collection<String> readPermissions = new ArrayList<>();
        readPermissions.add("public_profile");
        readPermissions.add("email");
        //readPermissions.add("user_birthday");
        readPermissions.add("user_friends");

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);

                AccessToken token = loginResult.getAccessToken();

                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response) {
                                Log.v("LoginActivity", response.toString());

                                // Application code
                                try {
                                    String email = object.getString("email");
                                    //String firstname = object.getString("first_name");
                                    //String lastname = object.getString("last_name");
                                    String strName = object.getString("name"); // 01/31/1980 format
                                    //String strName = firstname + " " + lastname;
                                    //Toast.makeText(mContext, "login success " +email + " " + strName, Toast.LENGTH_SHORT).show();
                                    registerUser(email, strName, "", "");
                                    //login(email, "", true);
                                    //showAlert(response.toString());
                                } catch (JSONException e) {
                                    Toast.makeText(mContext, "facebook:onError" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            }
                        });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,first_name,last_name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
                // ...
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                // ...
            }
        });
        LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, readPermissions);

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
    public Boolean checkEmailLogin() {

        mEmailLogin = edit_emaillogin.getText().toString();

        edit_emaillogin.setError(null);

        if (mEmailLogin.length() == 0) {

            edit_emaillogin.setError("DIt veld is verplicht");

            return false;
        }


        if (!Utils.isValidEmail(mEmailLogin)) {

            edit_emaillogin.setError("email format is wronng");

            return false;
        }

        if (mEmailLogin.length() < 5) {

            edit_emaillogin.setError("invalid email");

            return false;
        }
        return true;
    }
    public Boolean checkPassword() {
        mPassword = edit_password.getText().toString();

        edit_password.setError(null);
        if (mPassword.length() == 0) {

            edit_password.setError("please input password");

            return false;
        }
        return true;
    }
    public void registerUser(final String strEmail, String strName, String strPhone, String strAddress) {
        String strFirstName = strName, strLastName = "";
        String[] strArray = strName.split(" ");
        if(strArray!= null && strArray.length>=2) {
            strFirstName = strArray[0];
            strLastName = strArray[1];
        }
        String strUDID = SpottzApplication.getInstance().strUDID;
        String strUrl = String.format(Utils.getAppURL("/lappapi/facebooklogin/get/?device_token=" + strUDID + "&token=" + Utils.API_TOKEN +
                "&email=" + strEmail + "&firstname=" + strFirstName + "&lastname=" + strLastName + "&platform=Android&fbid=0"));
        NetClient netClient = new NetClient(this);
        showpDialog();
        netClient.get(strUrl, null, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {

                try {
                    JSONObject obj = (JSONObject) response;
                    String strRespEmail = obj.getString("email");
                    /*
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
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    */
                    if(strRespEmail.equals(strEmail)) {
                        Toast.makeText(mContext, "Signup success.", Toast.LENGTH_SHORT).show();
                        txt_login.performClick();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //sendNotification(Constants.INT_LOADED_SCORE);
                hidepDialog();
            }

            @Override
            public void onMyFailure(int status) {
                Toast.makeText(mContext, "Failed to register.", Toast.LENGTH_SHORT).show();
                hidepDialog();
            }
        });
    }
    public void loginUser(final String strEmail, String strPassword) {
        String strUDID = SpottzApplication.getInstance().strUDID;
        String strUrl = String.format(Utils.getAppURL("/lappapi/login/get/?device_token=" + strUDID + "&token=" + Utils.API_TOKEN + "&email=" + strEmail + "&password=" + strPassword + "&platform=Android"));
        NetClient netClient = new NetClient(this);
        showpDialog();
        netClient.get(strUrl, null, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {

                try {
                    JSONObject obj = (JSONObject) response;
                    String strRespEmail = obj.getString("email");
                    String strFirstName = obj.getString("firstname");
                    String strLastName = obj.getString("lastname");
                    String strName = strFirstName;
                    if(!strLastName.isEmpty())
                        strName = strName + " " + strLastName;
                    String strPhone = obj.getString("phone");
                    String strAddress = obj.getString("address");
                    sessionManager.hasLoggedIn();
                    sessionManager.setEmailId(strRespEmail);
                    sessionManager.setUserName(strName);
                    sessionManager.setAddress(strAddress);
                    sessionManager.setPhonenumber(strPhone);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                //sendNotification(Constants.INT_LOADED_SCORE);
                hidepDialog();
            }

            @Override
            public void onMyFailure(int status) {
                Toast.makeText(mContext, "Please check your email and password again.", Toast.LENGTH_SHORT).show();
                hidepDialog();
            }
        });
    }


    public void resetPassword(final String strEmail) {
        String strUrl = String.format(Utils.getAppURL("/lappapi/sendpassword/get/?email=" + strEmail + "&token=" + Utils.API_TOKEN));
        NetClient netClient = new NetClient(this);
        showpDialog();
        netClient.get(strUrl, null, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {
                resetDialog.dismiss();
                Toast.makeText(mContext, "Email sent.", Toast.LENGTH_SHORT).show();
                hidepDialog();
            }

            @Override
            public void onMyFailure(int status) {
                Toast.makeText(mContext, "Please check your email.", Toast.LENGTH_SHORT).show();
                hidepDialog();
            }
        });
    }
    protected void initpDialog() {

        pDialog = new ProgressDialog(LoginActivity.this);
        pDialog.setMessage(getString(R.string.msg_loading));
        pDialog.setCancelable(false);
    }

    protected void showpDialog() {

        if (!pDialog.isShowing()) pDialog.show();
    }

    protected void hidepDialog() {

        if (pDialog.isShowing()) pDialog.dismiss();
    }
    private void hideAllLoginViews(){
        lin_register.setVisibility(View.GONE);
        lin_login.setVisibility(View.GONE);
        lin_skip.setVisibility(View.GONE);
        txt_register.setAllCaps(false);
        txt_login.setAllCaps(false);
        txt_overslaan.setAllCaps(false);
    }


}
