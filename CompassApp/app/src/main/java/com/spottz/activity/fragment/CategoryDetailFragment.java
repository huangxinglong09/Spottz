package com.spottz.activity.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.spottz.R;
import com.spottz.activity.BaseActivity;
import com.spottz.activity.MainActivity;
import com.spottz.activity.PaymentActivity;
import com.spottz.app.SpottzApplication;
import com.spottz.constant.Constants;
import com.spottz.custom.compass.CompassView;
import com.spottz.model.CategoryModel;
import com.spottz.util.MessageDialog;
import com.spottz.util.SessionManager;
import com.spottz.util.Utils;

import static android.content.Context.MODE_PRIVATE;

public class CategoryDetailFragment extends Fragment implements View.OnClickListener, SensorEventListener {

    Activity activity;
    SharedPreferences settings;
    SharedPreferences.Editor editor;

    private TextView lblNavTitle;
    private ImageButton btnMenu, btnProfile;
    private Button btnBack;

    private View categoryTypeActions;
    private Button btnInputCode;
    private Button btnPay;

    private static final int TYPE_FREE = 1;
    private static final int TYPE_PAID = 2;
    private static final int TYPE_CODE = 3;
    private int currentType = TYPE_FREE;

    private TextView lblTitle, lblContent, lblAddress, lblPlace, lblDistance;
    private ImageView ivImage;
    private CategoryModel info;
    private Button btnStart;
    private View viewStartDialog;
    private TextView lblHint;
    private TextView lblStart;
    private Sensor sensor;
    private SensorManager sensorManager;
    private float currentDegree = 0f;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.INT_UPDATED_LOCATION_CHANGED) {
                refreshDistance();
            }
        }
    };
    private ImageView ivDirection;
    private float angle;
    private ImageView ivBackground;
    private float[] mMatrixR = new float[9];
    private float[] mMatrixValues = new float[3];


    public static final int REQUEST_PAYMENT = 5150;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity = getActivity();
        settings = activity.getPreferences(MODE_PRIVATE);
        editor = settings.edit();

        lblNavTitle = (TextView) view.findViewById(R.id.lblNavTitle);
        btnMenu = (ImageButton) view.findViewById(R.id.btnMenu);
        btnProfile = (ImageButton) view.findViewById(R.id.btnProfile);
        btnBack = (Button) view.findViewById(R.id.btnBack);
        lblTitle = (TextView) view.findViewById(R.id.lblTitle);
        ivImage = (ImageView) view.findViewById(R.id.ivImage);
        lblContent = (TextView) view.findViewById(R.id.lblContent);
        lblAddress = (TextView) view.findViewById(R.id.lblAddress);
        lblPlace = (TextView) view.findViewById(R.id.lblPlace);
        lblDistance = (TextView) view.findViewById(R.id.lblDistance);
        lblHint = (TextView) view.findViewById(R.id.lblHint);
        lblStart = (TextView) view.findViewById(R.id.lblStart);
        btnBack.setOnClickListener(this);
        btnBack.setText("<    Terug naar overzicht");

        // Category Type Actions
        categoryTypeActions = view.findViewById(R.id.categoryTypeActions);
        btnInputCode = (Button) view.findViewById(R.id.btnInputCode);
        btnPay = (Button) view.findViewById(R.id.btnPay);
        btnInputCode.setOnClickListener(this);
        btnPay.setOnClickListener(this);

        // Get Category Information
        info = SpottzApplication.getInstance().currentItem;
//        info.location.setLatitude(43.208069);//your coords of course
//        info.location.setLongitude(131.942099);


        if (info.type.equals("code")) {
            currentType = TYPE_CODE;

            // Check Paid Status
            if (isUnlocked())
                currentType = TYPE_FREE;
        } else if (info.type.equals("paid")) {
            currentType = TYPE_PAID;

            // Check Paid Status
            if (isUnlocked())
                currentType = TYPE_FREE;
        } else {
            currentType = TYPE_FREE;
        }

        // Update UI
        if (currentType == TYPE_CODE) {

            btnPay.setVisibility(View.GONE);
            btnInputCode.setVisibility(View.VISIBLE);
        } else if (currentType == TYPE_PAID) {
            btnPay.setVisibility(View.VISIBLE);

            btnInputCode.setVisibility(View.GONE);
        } else {
            categoryTypeActions.setVisibility(View.GONE);

        }

        // Here we load questions
        SpottzApplication.getInstance().loadSpots(info.iID);
        lblNavTitle.setText("SPOTTZ:" + info.strTitle);
        lblContent.setText(info.strDesc);
        lblAddress.setText(info.strAddress);
        lblPlace.setText(info.strPlace);

        ivDirection = (ImageView) view.findViewById(R.id.ivDirection);
        ivBackground = (ImageView) view.findViewById(R.id.ivBackground);
//        compassView = (CompassView) view.findViewById(R.id.compass_view);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        Utils.loadImage(this.getActivity(), ivImage, info.getImageURL());

        viewStartDialog = view.findViewById(R.id.viewStart);
        btnStart = (Button) view.findViewById(R.id.btnStart);
        viewStartDialog.setVisibility(View.GONE);
        btnStart.setOnClickListener(this);
        this.refreshDistance();
        this.setFonts();
    }

    private void setFonts() {
        Utils.setBold(btnStart);
        Utils.setBold(lblHint);
        Utils.setMedium(btnBack);
        Utils.setBold(lblTitle);
        Utils.setMedium(lblContent);
        Utils.setSemiBold(lblStart);
        Utils.setMedium(lblPlace);
        Utils.setMedium(lblAddress);
        Utils.setSemiBold(lblDistance);
    }

    @Override
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME);
        SpottzApplication.getInstance().handlerLocationChanged = handler;
    }

    @Override
    public void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onStop() {
        sensorManager.unregisterListener(this);
        super.onStop();

        // Remove Handler
        if (SpottzApplication.getInstance().handlerLocationChanged == handler) {
            SpottzApplication.getInstance().handlerLocationChanged = null;
        }

        if (SpottzApplication.getInstance().handlerSpotsLoaded != null) {
            SpottzApplication.getInstance().handlerSpotsLoaded = null;
        }
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.btnBack) {
            SpottzApplication.getInstance().handlerSpotsLoaded = null;
            SpottzApplication.getInstance().handlerLocationChanged = null;

            MainActivity activity = (MainActivity) getActivity();
            activity.showFragment(Constants.INT_FRMT_CATEGORY_LIST);
        } else if (viewId == R.id.btnStart) {
            this.startQuestion();
        } else if (viewId == R.id.btnInputCode) {
            final Dialog confirmActionDlg = new Dialog(activity);
            confirmActionDlg.setContentView(R.layout.dialog_inputcode);

            View btnNo = confirmActionDlg.findViewById(R.id.btnNo);
            View btnYes = confirmActionDlg.findViewById(R.id.btnYes);
            final EditText edtCode = (EditText) confirmActionDlg.findViewById(R.id.edtCode);

            edtCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        confirmActionDlg.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            });

            // Add No Action
            btnNo.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    confirmActionDlg.dismiss();
                }
            });

            // Add Yes Action
            btnYes.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    String strCode = edtCode.getText().toString().trim();
                    boolean bWrongCode = true;
                    String[] codes = info.codes.split(",");
                    for (String code : codes) {
                        code = code.replaceAll("\r", "");
                        code = code.replaceAll(" ", "");
                        if (code.equals(strCode)) {
                            bWrongCode = false;
                            break;
                        }
                    }

                    if (bWrongCode) {
                        edtCode.setError("Verkeerde code, controleer nogmaals");
                    } else {
                        confirmActionDlg.dismiss();
                        currentType = TYPE_FREE;
                        categoryTypeActions.setVisibility(View.GONE);

                        makeCategoryAsFree();

                        refreshDistance();
                    }
                }
            });

            confirmActionDlg.show();

        } else if (viewId == R.id.btnPay) {
            Intent intent = new Intent(activity, PaymentActivity.class);
            intent.putExtra("category_id", info.iID);
            intent.putExtra("amount", info.price);
            activity.startActivityForResult(intent, REQUEST_PAYMENT);
        }
    }

    void startQuestion() {
        if (SpottzApplication.getInstance().curSpots == null) {
            Utils.showProgressDialog(getActivity(), "Loading...");
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    Utils.dismissDialog();
                    if (SpottzApplication.getInstance().curSpots == null)
                        MessageDialog.showAlarmAlert(getActivity(), "Don't exist spot data");
                    else
                        gotoRouteScreen();
                }
            };
            SpottzApplication.getInstance().handlerSpotsLoaded = handler;
        } else {
            this.gotoRouteScreen();
        }

    }

    private void gotoRouteScreen() {
        if (SpottzApplication.getInstance().curSpots == null || SpottzApplication.getInstance().curSpots.arrQuestoins.size() < 1) {
            MessageDialog.showAlarmAlert(getActivity(), "No exist available question");
            return;
        }

        SpottzApplication.getInstance().handlerSpotsLoaded = null;
        SpottzApplication.getInstance().handlerLocationChanged = null;

        ////////////////////////////////////////////////////////
        SessionManager sessionManager = new SessionManager(getActivity());
        if(!sessionManager.isLoggedIn()) {
            if (!info.type.equals("code")) {
                // show message
                BaseActivity activity = (BaseActivity)getActivity();
                activity.showStartRouteAlertMessage(SpottzApplication.getInstance().startRouteAlertMessage);
                return;
            }
        }
        SpottzApplication.getInstance().startRoute(info.iID);
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null)
            activity.showFragment(Constants.INT_FRMT_ROUTE);
    }

    int counter = 0;


    private void refreshDistance() {
        Location curLocation = SpottzApplication.getInstance().curLocation;

        if (curLocation == null)
            return;

        angle = curLocation.bearingTo(info.location) - 90.f;
        lblDistance.setText(info.getDistanceForDetail(curLocation));
        lblContent.setText(String.format("%f, %f", curLocation.getLatitude(), curLocation.getLongitude()));

        // Check current paid status
        if (currentType != TYPE_FREE)
            return;

        info.calculateDistance(curLocation);
        int distance = (int) curLocation.distanceTo(info.location);
        if (SpottzApplication.testmode)
            counter++;

        int distanceRange = info.alertDistance;

        if (distance < distanceRange/*21*/ || (SpottzApplication.testmode && counter > 1)) {
            if (viewStartDialog.getVisibility() == View.GONE) {
                viewStartDialog.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ORIENTATION:
//                float degree = Math.round(event.values[0]);
//                RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f,
//                        Animation.RELATIVE_TO_SELF, 0.5f);
//                ra.setDuration(210);
//                ra.setFillAfter(true);
//                ivBackground.startAnimation(ra);
//                currentDegree = -degree;
//                {
//                    ivDirection.setRotation(angle - degree);
//                }
                break;

            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(mMatrixR, event.values);
                SensorManager.getOrientation(mMatrixR, mMatrixValues);
                // Use this value in degrees
                float degree = Math.round(Math.toDegrees(mMatrixValues[0]));
                RotateAnimation ra = new RotateAnimation(currentDegree, -degree, Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                ra.setDuration(210);
                ra.setFillAfter(true);
                ivBackground.startAnimation(ra);
                currentDegree = -degree;
            {
                ivDirection.setRotation(angle - degree);
            }
            break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PAYMENT && resultCode == Activity.RESULT_OK) {
            Toast.makeText(activity, "Uw betaling is succesvol", Toast.LENGTH_SHORT).show();

            currentType = TYPE_FREE;
            categoryTypeActions.setVisibility(View.GONE);

            makeCategoryAsFree();
            refreshDistance();
        }
    }

    private boolean isUnlocked() {
        String unlockedCategories = settings.getString("unlocked_categories", "");
        if (TextUtils.isEmpty(unlockedCategories)) {
            return false;
        } else {
            String[] unlockedIds = unlockedCategories.split(",");
            String categoryId = info.iID + "";
            for (String itemId : unlockedIds) {
                if (categoryId.equals(itemId)) {
                    return true;
                }
            }

            return false;
        }
    }

    private void makeCategoryAsFree() {
        String unlockedCategories = settings.getString("unlocked_categories", "");
        if (TextUtils.isEmpty(unlockedCategories)) {
            unlockedCategories += info.iID;
        } else {
            unlockedCategories += "," + info.iID;
        }
        editor.putString("unlocked_categories", unlockedCategories).commit();
    }
}
