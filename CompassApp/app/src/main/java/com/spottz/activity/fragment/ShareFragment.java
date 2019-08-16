package com.spottz.activity.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.spottz.R;
import com.spottz.activity.MainActivity;
import com.spottz.app.SpottzApplication;
import com.spottz.constant.Constants;
import com.spottz.model.CategoryModel;
import com.spottz.util.MessageDialog;
import com.spottz.util.Utils;

import java.io.File;

public class ShareFragment extends Fragment implements View.OnClickListener {

    private Button btnShare;
    private ImageView ivPreview;

    private String strShareURL;
    private ShareLinkContent content;
    CallbackManager callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_share, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnShare = (Button)view.findViewById(R.id.btnShare);
        btnShare.setOnClickListener(this);

        strShareURL = SpottzApplication.getInstance().strShareLink;
        ivPreview = (ImageView)view.findViewById(R.id.ivPreview);
        if (SpottzApplication.getInstance().bitmap != null) {
            ivPreview.setImageBitmap(SpottzApplication.getInstance().bitmap);
        }

        callbackManager = CallbackManager.Factory.create();
        content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse(strShareURL))
                .build();
        this.setFonts();
        CategoryModel categoryModel = SpottzApplication.getInstance().currentItem;
        if(categoryModel != null){
            if(categoryModel.status == 0)
            {
                btnShare.setVisibility(View.GONE);
            }
        }
    }

    private void setFonts() {
        Utils.setBold(btnShare);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        ShareDialog shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {

            @Override
            public void onSuccess(Sharer.Result result) {
                gotoHomme();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                MessageDialog.showAlarmAlert(getActivity(), "Facebook Share Error");
            }
        });
        shareDialog.show(content, ShareDialog.Mode.FEED);
    }

    void gotoHomme() {
        MainActivity activity = (MainActivity) getActivity();
        activity.showFragment(Constants.INT_FRMT_CATEGORY_LIST);
    }
}


