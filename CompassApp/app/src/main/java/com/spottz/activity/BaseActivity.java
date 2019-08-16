package com.spottz.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.spottz.R;
import com.spottz.app.SpottzApplication;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public  void showStartRouteAlertMessage(String htmlText) {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_confirm, null);

                final AlertDialog errorDlg = new AlertDialog.Builder(this)
                        .setView(dialogView)
                        .setCancelable(false)
                        .create();

        WebView webView = dialogView.findViewById(R.id.webView);
        webView.loadData(htmlText, "text/html", null);
                dialogView.findViewById(R.id.confirm_dlg_ok).setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        errorDlg.dismiss();
                    }
        });

        errorDlg.show();
        errorDlg.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
    }
}
