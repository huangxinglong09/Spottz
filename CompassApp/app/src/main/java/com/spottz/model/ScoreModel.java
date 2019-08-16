package com.spottz.model;


import org.json.JSONObject;

/**
 * Created by iMac on 8/22/16.
 */
public class ScoreModel
{
    public int score, id;
    public String imageurl;

    public ScoreModel(JSONObject dict) {
        if (dict != null) {
            score = dict.optInt("score");
            id = dict.optInt("id");
            imageurl = dict.optString("image");
        }
    }

    public String getScoreToString(){
        return String.valueOf(score);
    }

    public String getImageURL() {
        if (imageurl == null) {
            return "";
        }

        return imageurl;
    }


}
