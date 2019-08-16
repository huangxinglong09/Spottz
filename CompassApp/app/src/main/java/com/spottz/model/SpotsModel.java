package com.spottz.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by iMac on 8/22/16.
 */
public class SpotsModel
{
    public ArrayList<QuestionModel> arrQuestoins = new ArrayList<>();

    public SpotsModel(JSONArray array) {
        if (array.length() < 1)
            return;

        arrQuestoins.clear();
        int idx;
        JSONObject objtmp;
        for (idx = 0; idx < array.length(); idx++) {
            objtmp = array.optJSONObject(idx);
            if (!objtmp.has("id"))
                continue;

            QuestionModel info = new QuestionModel(objtmp);
            if (info.arrAnswers.size() < 1)
                continue;

            arrQuestoins.add(info);
        }
    }
}
