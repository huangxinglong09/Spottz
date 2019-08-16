package com.spottz.activity.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.RequestParams;
import com.spottz.R;
import com.spottz.activity.MainActivity;
import com.spottz.app.SpottzApplication;
import com.spottz.constant.Constants;
import com.spottz.model.QuestionModel;
import com.spottz.net.BaseJsonRes;
import com.spottz.net.NetClient;
import com.spottz.util.MessageDialog;
import com.spottz.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ResultViewFragment extends Fragment implements View.OnClickListener {

    private TextView lblTotalScore;
    private Button btnFinish;
    private TextView lblTotal, lblCorrect, lblWrong, lblTime;
    private TextView lblTitle;
    private TextView lblShareDesc;

    int totalScore = 0;
    long starttime = 0;
    long endtime = 0;
    int questionScore = 0;
    int timeScore = 0;

    NetClient netClient = new NetClient(null);

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Utils.dismissDialog();
            if (msg.what == 0x00) {
                gotoShareScreen();
            }
        }
    };
    private Bitmap imageBitmap;
    private EditText txtEmail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_result, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        lblTitle = (TextView)view.findViewById(R.id.lblTitle);
        lblTotalScore = (TextView)view.findViewById(R.id.lblTotalScore);
        lblTotal = (TextView)view.findViewById(R.id.lblTotal);
        lblCorrect = (TextView)view.findViewById(R.id.lblCorrect);
        lblWrong = (TextView)view.findViewById(R.id.lblWrong);
        lblTime = (TextView)view.findViewById(R.id.lblTime);
        lblShareDesc = (TextView)view.findViewById(R.id.lblShareDesc);
        btnFinish = (Button) view.findViewById(R.id.btnFinish);
        btnFinish.setOnClickListener(this);
        txtEmail = (EditText)view.findViewById(R.id.txtEmail);

        int count = SpottzApplication.getInstance().curSpots.arrQuestoins.size();
        lblTotal.setText(String.format("Aantal spottz: %d", count));

        ArrayList<QuestionModel> questions = SpottzApplication.getInstance().curSpots.arrQuestoins;
        int totalsec = 0, correct = 0, wrong = 0;
        for (QuestionModel item : questions) {
            if (starttime == 0) {
                starttime = item.startTime;
            }

            int timesec = item.getDuration();
            totalsec += timesec;
            if (item.isCorrectAnswer()) {
                correct += 1;
            }

            totalScore += item.getScore();
            questionScore += item.getScore();
            endtime = item.endTime;
        }

        timeScore = SpottzApplication.getInstance ().currentItem.getTimeScore(totalsec);
        totalScore += timeScore;
        wrong = count - correct;

        //lblCorrect.setText(String.format("Aantal juist: %d", correct));
        //lblWrong.setText(String.format("Aantal onjuist: %d", wrong));
        lblCorrect.setText(String.format("%d", correct));
        lblWrong.setText(String.format("%d", wrong));

        int hour = totalsec / 3600;
        int min = (totalsec % 3600) / 60;
        int sec1 = (totalsec % 60);
        if (sec1 > 0) {
            min = min + 1;
        }

        if (totalsec > 3600) {
            lblTime.setText(String.format("Tijd: %d Hr, %d Min", hour, min));  //Time spend
        }
        else {
            lblTime.setText(String.format("Tijd: %d Min", min));
        }

        lblTotalScore.setText(String.format("Score: %d", totalScore));

        SpottzApplication.getInstance().saveScore(SpottzApplication.getInstance().currentItem.iID, totalScore, endtime);

        lblShareDesc.setText(SpottzApplication.getInstance().currentItem.strShareContent);
        lblTitle.setText("RESULTAAT");
        btnFinish.setText("Score delen");

        this.setFonts();
    }

    private void setFonts() {
        Utils.setSemiBold(lblTitle);
        Utils.setBold(lblTotalScore);
        Utils.setSemiBold(lblTotal);
        Utils.setSemiBold(lblCorrect);
        Utils.setSemiBold(lblWrong);
        Utils.setSemiBold(lblTime);
        Utils.setMedium(lblShareDesc);
        Utils.setBold(btnFinish);
    }


    @Override
    public void onClick(View v) {

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Check Permissions Now
            // Callback onRequestPermissionsResult interceptado na Activity MainActivity
            ActivityCompat.requestPermissions(this.getActivity(),
                    new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    80);
        } else {
//            String strEmail = txtEmail.getText().toString().trim();
//            if (strEmail.length() < 1) {
//                Utils.showLongToast(getActivity(), "Please intput email to get game result");
//                return;
//            }
//            else if (!Utils.isValidEmail(strEmail)) {
//                Utils.showLongToast(getActivity(), "Please input correct email");
//                return;
//            }

            dispatchTakePictureIntent();

//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//                startActivityForResult(takePictureIntent, Constants.TYPE_PHOTO_PICK_FROM_CAMERA);
//            }
        }
    }

    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    String mCurrentPhotoPath;
    private File createImageFile() throws IOException {
        // Create an image file name
        String imageFileName = "tmp_saved";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName + "_",  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this.getActivity(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, Constants.TYPE_PHOTO_PICK_FROM_CAMERA);
            }
        }
    }

    private Bitmap getPic() {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        int scale = 2;
        if (photoW < 700) {
            scale = 1;
        }
        else if (photoW > 2000){
            scale = 3;
        }
        // Determine how much to scale down the image
        int scaleFactor = Math.min(scale, scale);
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(mCurrentPhotoPath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);

            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    bitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    bitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    bitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) return;

        if (requestCode == Constants.TYPE_PHOTO_PICK_FROM_CAMERA) {
//            Bundle extras = data.getExtras();
            Bitmap bitmap = this.getPic();
            try {
                uploadImage(bitmap);
                SpottzApplication.getInstance().bitmap = bitmap;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String scaleImage(Bitmap bitmap) throws IOException {
        imageBitmap = bitmap;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 70, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    private void uploadImage(Bitmap bitmap) throws IOException {
        String file = this.scaleImage(bitmap);
        Calendar startdate = Calendar.getInstance();
        startdate.setTimeInMillis(starttime);

        Calendar enddate = Calendar.getInstance();
        enddate.setTimeInMillis(endtime);


        SimpleDateFormat dateformatter = new SimpleDateFormat("yyyy-MM-dd");
        String strStartDate = dateformatter.format(startdate.getTime());
        String strEndDate = dateformatter.format(enddate.getTime());

        dateformatter = new SimpleDateFormat("HH:mm:ss");
        String strStartTime = dateformatter.format(startdate.getTime());
        String strEndTime = dateformatter.format(enddate.getTime());


        RequestParams params = new RequestParams();
        params.put("image", file);
        params.put("id", SpottzApplication.getInstance().strID);
        params.put("device_token", SpottzApplication.getInstance().strUDID);
        params.put("category_id", String.valueOf(SpottzApplication.getInstance().currentItem.iID));
        params.put("startdate", strStartDate);
        params.put("starttime", strStartTime);
        params.put("enddate", strEndDate);
        params.put("endtime", strEndTime);
        params.put("total_score", String.valueOf(totalScore));
        params.put("questions_score", String.valueOf(questionScore));
        params.put("time_score", String.valueOf(timeScore));
        params.put("name", "Android__" + txtEmail.getText().toString().trim());

        Utils.showProgressDialog(getActivity(), "Uploading...");
        netClient.post(Utils.getAppURL("/lappapi/put/score/"), params, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {
                Utils.dismissDialog();
                JSONObject json = (JSONObject)response;
                if (json.has("link")) {
                    SpottzApplication.getInstance().strShareLink = json.optString("link");
                }

                gotoShareScreen();
            }

            @Override
            public void onMyFailure(int status) {
                Utils.dismissDialog();
                Message msg = handler.obtainMessage(0x100);
                handler.sendMessage(msg);
            }
        });
    }

    private void gotoShareScreen() {
        MainActivity activity = (MainActivity) getActivity();
        activity.showFragment(Constants.INT_FRMT_SHARE);
        SpottzApplication.getInstance().bitmap = imageBitmap;
    }
}
