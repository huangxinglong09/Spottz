package com.spottz.model;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by iMac on 8/22/16.
 */
public class LocalScoreModel
{
    public int score;
    public String date;
    public int catid;

    public LocalScoreModel(JSONObject obj) {
        if (obj != null) {
            score = obj.optInt("score");
            catid = obj.optInt("category_id");
            date = obj.optString("date");
        }
        else {
            score = 0;
            catid = 0;
            date = "";
        }
    }

    public void initValue(int score, int catid, String date) {
        this.score = score;
        this.catid = catid;
        this.date = date;
    }
}
