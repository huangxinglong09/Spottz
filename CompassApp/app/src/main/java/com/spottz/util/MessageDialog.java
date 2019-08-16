package com.spottz.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.spottz.app.SpottzApplication;

public class MessageDialog
{
	public static void showAlarmAlert(Context context, String strMessage)
	{
        if (strMessage == null)
            strMessage = "Network Error";

        Utils.showLongToast(context, strMessage);
	}

    public static boolean showErrorAlert(Context context, String strMessage)
    {
        Utils.dismissDialog();
        if (context == null)
            context = SpottzApplication.getInstance().getApplicationContext();

        if (strMessage == null)
            strMessage = "Network Error";

        new AlertDialog.Builder(context)
                .setMessage(strMessage)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
        return true;
    }

//    public static void showServerError(Context context, int status) {
//        String strMessage = null;
//        Utils.showLongToast(context, strMessage);
//    }
}
