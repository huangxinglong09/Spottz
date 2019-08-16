package com.spottz.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.RequestParams;
import com.spottz.R;
import com.spottz.app.SpottzApplication;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.R.attr.typeface;


public class Utils implements OnTouchListener
{
	private static String PREFIX_URL ="https://kompasspel.spottz.nl";
    //private static String PREFIX_URL ="https://www.spottz.eu";
    //private static String PREFIX_URL ="http://192.168.6.220";

	public static final String API_TOKEN = "56cb1d4bfe423ca482d107aa95312850";
    //private static String PREFIX_URL ="https://kompasspel.spottz.nl";

    private static Utils objAction;
    private static ProgressDialog mProgressDialog;
    private static Toast mToast;
    private static Rect rectScreen = new Rect();
    private static int iStatusBarHeight;
    private static SimpleDateFormat defaultDateFormat = new SimpleDateFormat("MM/dd/yyyy");
    private static SimpleDateFormat defaultDateTimeFormat = new SimpleDateFormat("MM/dd/yyyy h:mm a");
    private static SimpleDateFormat defaultTimeFormat = new SimpleDateFormat("h:mm a");

    private static ArrayList<String> arrAgeRange, arrBloodType, arrGender;
    private static Typeface typefaceExoBold, typefaceExoExtraBold, typefaceMedium, typefaceRegular, typefaceSemiBold;

    public Utils()
    {
        
    }

    public static void loadCustomFont(Context context) {
        AssetManager am = context.getApplicationContext().getAssets();
        typefaceExoBold = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Exo2-Bold.ttf"));
        typefaceExoExtraBold = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Exo2-ExtraBold.ttf"));
        typefaceMedium = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Exo2-Medium.ttf"));
        typefaceRegular = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Exo2-Regular.ttf"));
        typefaceSemiBold = Typeface.createFromAsset(am, String.format(Locale.US, "fonts/%s", "Exo2-SemiBold.ttf"));
    }

    public static void setSemiBold(TextView label) {
        if (label == null) return;
        label.setTypeface(typefaceSemiBold);
    }

    public static void setExtraBold(TextView label) {
        if (label == null) return;
        label.setTypeface(typefaceExoExtraBold);
    }

    public static void setBold(TextView label) {
        if (label == null) return;
        label.setTypeface(typefaceExoBold);
    }

    public static void setMedium(TextView label) {
        if (label == null) return;
        label.setTypeface(typefaceMedium);
    }

    public static void setRegular(TextView label) {
        if (label == null) return;
        label.setTypeface(typefaceRegular);
    }

    public static boolean isValidEmail(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    public static ArrayList<String> getGenderType() {
        if (arrGender == null)
        {
            arrGender = new ArrayList<String>();
            arrGender.add("Male");
            arrGender.add("Female");
        }

        return arrGender;
    }

    public static ArrayList<String> getBloodType() {
        if (arrBloodType == null)
        {
            arrBloodType = new ArrayList<String>();
            arrBloodType.add("A+");
            arrBloodType.add("A-");
            arrBloodType.add("B+");
            arrBloodType.add("B-");
            arrBloodType.add("AB+");
            arrBloodType.add("AB-");
            arrBloodType.add("O+");
            arrBloodType.add("O-");
        }

        return arrBloodType;
    }

    public static ArrayList<String> getAgeRange()
    {
        if (arrAgeRange == null)
        {
            arrAgeRange = new ArrayList<String>();
            for (int idx = 15; idx < 100; idx++)
                arrAgeRange.add(String.valueOf(idx));
        }

        return arrAgeRange;
    }

    public static void showConfirmDialog(Context context, int msgid, DialogInterface.OnClickListener listener)
    {
        showConfirmDialog(context, context.getString(msgid), listener);
    }

    public static void showConfirmDialog(Context context, String message, DialogInterface.OnClickListener listener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        TextView msg = new TextView(context);
        msg.setText(message);
        msg.setPadding(100, 120, 100, 120);
        msg.setTextColor(context.getResources().getColor(R.color.black));
        msg.setGravity(Gravity.CENTER);
        msg.setTextSize(18);
        builder.setView(msg);
        builder.setCancelable(false);
        builder.setPositiveButton(context.getString(R.string.s_ok), listener);
        builder.setNegativeButton(context.getString(R.string.s_cancel), null);
        AlertDialog dialog = builder.show();

        Button b = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
        if(b != null)
            b.setTextColor(context.getResources().getColor(R.color.black));

        b = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
        if(b != null)
            b.setTextColor(context.getResources().getColor(android.R.color.holo_red_light));
    }

	public static void hideKeyboard(Activity activity)
	{
	    InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
	    //Find the currently focused view, so we can grab the correct window token from it.
	    View view = activity.getCurrentFocus();
	    //If no view currently has focus, create a new one, just so we can grab a window token from it
	    if(view == null)
	        view = new View(activity);

	    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
	}
	
    public static int getScreenWidth(Activity activity) {
        if (rectScreen.width() < 1)
            objAction.initScreenInfo(activity);

        return rectScreen.width();
    }

    private static void initScreenInfo(Activity activity)
    {
        Window window = activity.getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectScreen);
        int statusBarTop = rectScreen.top;
        int contentViewTop = window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        iStatusBarHeight = Math.abs(statusBarTop - contentViewTop);
    }

    public static int getScreenHeight(Activity activity)
    {
        return objAction.getScreenHeight(activity, true);
    }

    public static int getScreenHeight(Activity activity, boolean bExceptStatusBar)
    {
        if (rectScreen.height() < 1)
            objAction.initScreenInfo(activity);

        if (bExceptStatusBar)
            return rectScreen.height() - iStatusBarHeight;
        return rectScreen.width();
    }

    public static RequestParams getRequestParams()
    {
        RequestParams params = new RequestParams();
        return params;
    }

    @Override
	public boolean onTouch(View v, MotionEvent event)
	{
		switch (event.getAction())
		{
			case MotionEvent.ACTION_DOWN:
			{
				v.getBackground().setColorFilter(0x7Adde0e5,
						PorterDuff.Mode.SRC_ATOP);
				v.invalidate();
				break;
			}
			case MotionEvent.ACTION_UP:
			{
				v.getBackground().clearColorFilter();
				v.invalidate();
				break;
			}
		}
		return false;
	}
	
    public static void setTotalHeightofListView(ListView listView, float itemheight)
    {
        ListAdapter mAdapter = listView.getAdapter();
        int totalHeight = (int)(itemheight * mAdapter.getCount());
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (mAdapter.getCount())) + 2;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static void setViewHeight(View view, int height)
    {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
        view.requestLayout();
    }

    public static final void showProgressDialogChagneText(String message) {
        dismissDialog();
        if (TextUtils.isEmpty(message)) {
            message = "Loading...";
        }

        if (mProgressDialog == null)
            return;

        mProgressDialog.setMessage(message);
    }


    public static final void showProgressDialog(Context context, String message) {
        dismissDialog();
        if (TextUtils.isEmpty(message)) {
            message = "Loading...";
        }

        mProgressDialog = new ProgressDialog(context, R.style.CustomDialog);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(message);
        mProgressDialog.show();
    }

    public static final void dismissDialog()
    {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }


    public static final void showToastMessage(final Activity activity, final String message, boolean bCloseProgress) {
        if (bCloseProgress)
            dismissDialog();
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (mToast != null) {
                    mToast.cancel();
                    mToast = null;
                }
                mToast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
                mToast.show();
            }
        });
    }

    public static void release() {
        mProgressDialog = null;
        mToast = null;
    }

    public static void showLongToast(Context context, int error) {
        String strMessage =  context.getResources().getString(error);
        Utils.showLongToast(context, strMessage);
    }

    public static void showLongToast(Context context, String strMessage)
    {
        Utils.dismissDialog();
        if (context == null)
            context = SpottzApplication.getInstance().getApplicationContext();

        if (strMessage != null && strMessage.length() > 0)
            Toast.makeText(context, strMessage, Toast.LENGTH_LONG).show();
    }

    public static String getDateString(Date date) {
        return defaultDateFormat.format(date);
    }

    public static Calendar getDateFromString(String strDate)
    {
        if (strDate == null || strDate.length() != 10)
            return null;

        try {
            Date date = defaultDateFormat.parse(strDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            return cal;
        } catch (ParseException e) {
        }
        return null;
    }

    private static Calendar getDateAndTimeFromString(String strDate, String strTime)
    {
        Calendar cal = Calendar.getInstance();
        try {

            Date date = defaultDateFormat.parse(strDate);
            cal.setTime(date);

            Date date1 = defaultTimeFormat.parse(strTime);
            cal.set(Calendar.HOUR, date1.getHours());
            cal.set(Calendar.MINUTE, date1.getMonth());
            return cal;
        }catch (Exception e)
        {

        }
        return cal;
    }

    public static boolean checkLocationPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void showPermissionDialog(Activity activity) {
        if (!checkLocationPermission(activity)) {
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    0);
        }
    }

    public static void loadAvatarImage(Context context, ImageView imageView, String imageurl) {
        if (imageurl == null || imageurl.length() < 10) {
            imageView.setImageResource(R.drawable.avatar);
            return;
        }

        Picasso.with(context).load(imageurl).placeholder(R.drawable.avatar).into(imageView);
    }

    public static void loadCategoryThumbImage(Context context, ImageView imageView, String imageurl) {
        if (imageurl == null || imageurl.length() < 10) {
            imageView.setImageResource(R.drawable.default_image);
            return;
        }

        Picasso.with(context).load(imageurl).resize(400, 300).placeholder(R.drawable.default_image).into(imageView);
    }

    public static void loadImage(Context context, ImageView imageView, String imageurl) {
        if (imageurl == null || imageurl.length() < 10) {
            imageView.setImageResource(R.drawable.default_image);
            return;
        }

        Picasso.with(context).load(imageurl).placeholder(R.drawable.default_image).into(imageView);
    }

    public static void loadCropImage(Context context, ImageView imageView, String imageurl) {
        if (imageurl == null || imageurl.length() < 10) {
            imageView.setImageResource(R.drawable.default_image);
            return;
        }

        Picasso.with(context).load(imageurl).placeholder(R.drawable.default_image).into(imageView);
    }

    public static String getAppURL(String substring) {
        return PREFIX_URL + substring;
    }
}
