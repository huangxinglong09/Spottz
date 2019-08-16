package com.spottz.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.spottz.R;
import com.spottz.net.BaseJsonRes;
import com.spottz.net.NetClient;
import com.spottz.util.Utils;

import org.json.JSONObject;

public class ContentActivity extends BaseActivity {

    private NetClient netClient;
    private TextView lblTitle;
    private ImageButton btnBack;
    private WebView webView;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_content);

        lblTitle = (TextView)findViewById(R.id.lblTitle);
        btnBack = (ImageButton)findViewById(R.id.btnBack);
        webView = (WebView)findViewById(R.id.webView);
        progress = (ProgressBar)findViewById(R.id.progress);

        netClient = new NetClient(this);
            netClient.get(Utils.getAppURL("/lappapi/get/page/12"), null, new BaseJsonRes() {
            @Override
            public void onMySuccess(Object response) {
                progress.setVisibility(View.INVISIBLE);
                JSONObject obj = (JSONObject) response;
                if (obj.has("title")) {
                    String strTitle = obj.optString("title");
                }

                if (obj.has("content")) {
                    String strContent = obj.optString("content");
                    webView.loadData(strContent, "text/html", null);
                }
            }

            @Override
            public void onMyFailure(int status) {

            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        this.setFonts();
    }

    private void setFonts() {
        Utils.setExtraBold(lblTitle);
    }

    @Override
    public void onBackPressed() {
        this.finish();
        overridePendingTransition(R.anim.slide_re_in, R.anim.slide_re_out);

    }
}
