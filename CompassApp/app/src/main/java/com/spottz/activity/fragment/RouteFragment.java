package com.spottz.activity.fragment;

import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.spottz.R;
import com.spottz.activity.MainActivity;
import com.spottz.app.SpottzApplication;
import com.spottz.constant.Constants;
import com.spottz.custom.compass.CompassView;
import com.spottz.model.QuestionModel;
import com.spottz.util.Utils;


public class RouteFragment extends Fragment implements SensorEventListener{

    private ImageButton btnMenu, btnProfile;

    private TextView lblStep, lblDistance;
    private TextView lblTitle;
    private QuestionModel questionInfo;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == Constants.INT_UPDATED_LOCATION_CHANGED) {
                refreshDistance();
            }
        }
    };
    private Sensor sensor;
    private SensorManager sensorManager;
    //private CompassView compassView;
    private float currentDegree = 0f;
    private ImageView ivDirection;
    private float angle;
    private ImageView ivBackground;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compass, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        questionInfo = SpottzApplication.getInstance().getCurrentQuestion();
        btnMenu = (ImageButton) view.findViewById(R.id.btnMenu);
        btnProfile = (ImageButton) view.findViewById(R.id.btnProfile);

        lblStep = (TextView)view.findViewById(R.id.lblStep);
        lblDistance = (TextView)view.findViewById(R.id.lblDistance);
        lblTitle = (TextView)view.findViewById(R.id.lblTitle);
        int currentQuestionIdx = SpottzApplication.getInstance().iCurrentSpot;
        lblStep.setText(String.format("%d van %d", currentQuestionIdx + 1, SpottzApplication.getInstance().curSpots.arrQuestoins.size()));
        lblTitle.setText(questionInfo.strSpotName);

        //compassView = (CompassView) view.findViewById(R.id.compass_view);
        angle = SpottzApplication.getInstance().curLocation.bearingTo(questionInfo.location) - 90.f;
        ivBackground = (ImageView) view.findViewById(R.id.ivBackground);
        ivDirection = (ImageView) view.findViewById(R.id.ivDirection);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        angle = SpottzApplication.getInstance().curLocation.bearingTo(questionInfo.location) - 90;
        questionInfo.startTime = System.currentTimeMillis();
        this.refreshDistance();
        this.setFonts();
    }

    private void setFonts() {
        Utils.setMedium(lblStep);
        Utils.setBold(lblDistance);
        Utils.setMedium(lblTitle);
    }

    @Override
    public void onResume() {
        super.onResume();
        SpottzApplication.getInstance().handlerLocationChanged = handler;
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
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
    }

    int counter = 0;
    private void refreshDistance() {
        Location curLocation = SpottzApplication.getInstance().curLocation;
        angle = SpottzApplication.getInstance().curLocation.bearingTo(questionInfo.location) - 90.f;
        lblDistance.setText(questionInfo.getDistanceForDetail(curLocation));

        if (SpottzApplication.testmode)
            counter ++;

        if (questionInfo.isReachNearby(curLocation) || (SpottzApplication.testmode && counter > 1)) {
            SpottzApplication.getInstance().handlerLocationChanged = null;
            MainActivity activity = (MainActivity) getActivity();
            activity.showFragment(Constants.INT_FRMT_QUESTION);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        switch (sensorType) {
            case Sensor.TYPE_ORIENTATION:
                float degree = event.values[0];
                if (Math.abs(currentDegree - degree) > 1) {
                    //compassView.setRotate(degree);
                    ivBackground.setRotation(-degree);
                    currentDegree = degree;
                    ivDirection.setRotation(angle - degree);
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
