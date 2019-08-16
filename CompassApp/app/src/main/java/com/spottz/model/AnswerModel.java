package com.spottz.model;

import org.json.JSONObject;

/**
 * Created by lionking on 5/20/17.
 */

public class AnswerModel {
    public String strTitle;
    public boolean bRight;

    public AnswerModel(JSONObject objtmp) {
        if (objtmp == null)
            return;
        strTitle = objtmp.optString("title");
        bRight = objtmp.optInt("selected") > 0;
    }

}
