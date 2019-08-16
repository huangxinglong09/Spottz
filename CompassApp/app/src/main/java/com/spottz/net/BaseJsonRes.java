package com.spottz.net;

import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.spottz.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;


public abstract class BaseJsonRes extends AsyncHttpResponseHandler {

    public BaseJsonRes()
    {
    }

	@Override
    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody)
    {
		try {
            String str = new String(responseBody).trim();
            Log.e("=============", str);

            if (str.charAt(0) == '[') {
                JSONArray response = new JSONArray(str);
                onMySuccess(response);
            }
            else {
                JSONObject response = new JSONObject(str);
                if (response.has("error_code")) {
                    onMyFailure(-1);
                }
                else {
                    onMySuccess(response);
                }
            }

		} catch (JSONException e) {
			onMyFailure(-1);
            e.printStackTrace();
		}
	}

    @Override
    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error)
    {
        if (responseBody != null)
            Log.e("=============", new String(responseBody));
        Utils.dismissDialog();
        onMyFailure(-1);
    }


	public abstract void onMySuccess(Object response);

	public abstract void onMyFailure(int status);
}
