package com.spottz.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.spottz.R;
import com.spottz.activity.fragment.CategoryDetailFragment;
import com.spottz.activity.fragment.CategoryFragment;
import com.spottz.activity.fragment.QuestionFragment;
import com.spottz.activity.fragment.ResultViewFragment;
import com.spottz.activity.fragment.RouteFragment;
import com.spottz.activity.fragment.ScoreFragment;
import com.spottz.activity.fragment.ShareFragment;
import com.spottz.app.SpottzApplication;
import com.spottz.constant.Constants;
import com.spottz.net.NetClient;
import com.spottz.util.Utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class PaymentActivity extends BaseActivity {

    int categoryId;
    float price;

    //String returnUrl = "http://www.spottz.nl";
    String returnUrl = "http://spottz.payment.success";
    String returnUrlEncode;

    private Toolbar toolbar;
    private TextView lblTitle;

    WebView webView;
    ImageView ivConnectionError;
    boolean bFailedToLoad = false;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        Intent intent = getIntent();
        categoryId = intent.getIntExtra("category_id", -1);
        price = intent.getFloatExtra("amount", 0);
        if (categoryId == -1) {
            finish();
            return;
        }

        try {
            returnUrlEncode = URLEncoder.encode(returnUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String strAmount = String.format("%.02f", price);
        strAmount = strAmount.replaceAll(",", ".");
        //String paymentUrl = String.format("http://abilee.nl/spottz/payment.php?amount=%s&order=%d&return_url=%s", strAmount, System.currentTimeMillis(), returnUrlEncode);
        String paymentUrl = String.format("https://spottz.eu/spottz/payment.php?amount=%s&order=%d&return_url=%s", strAmount, System.currentTimeMillis(), returnUrlEncode);

        // Show Url
        final TextView tvUrl = (TextView) findViewById(R.id.tvUrl);
        tvUrl.setText(paymentUrl);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        setSupportActionBar(toolbar);


        lblTitle = (TextView) toolbar.findViewById(R.id.lblTitle);
        Utils.setExtraBold(lblTitle);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setTitle("");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        ivConnectionError = (ImageView) findViewById(R.id.ivConnectionError);

        webView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        //webSettings.setBuiltInZoomControls(true);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith(returnUrl) || (TextUtils.isEmpty(returnUrlEncode) && url.startsWith(returnUrlEncode))) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    view.loadUrl(url);
                    tvUrl.setText(url);
                }
                return true;
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                hideProgressDialog();
                webView.setVisibility(View.GONE);
                ivConnectionError.setVisibility(View.VISIBLE);
                bFailedToLoad = true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                hideProgressDialog();

                if (!bFailedToLoad) {
                    webView.setVisibility(View.VISIBLE);
                    ivConnectionError.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                ivConnectionError.setVisibility(View.GONE);
                bFailedToLoad = false;
                showProgressDialog();
            }
        });

        webView.setBackgroundColor(Color.WHITE);
        webView.loadUrl(paymentUrl);
        webView.requestFocus();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

    }

    private void showProgressDialog() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressDialog() {
        progressBar.setVisibility(View.GONE);
    }
}
