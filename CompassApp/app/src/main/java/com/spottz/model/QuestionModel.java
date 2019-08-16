package com.spottz.model;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by iMac on 8/22/16.
 */
public class QuestionModel
{
    public int iID;
    public ArrayList<AnswerModel> arrAnswers = new ArrayList<>();
    public ArrayList<String> arrImages = new ArrayList<>();
    public int iCompanyID;
    public String strCompanyImage, strCompanyName, strSpotName, strContent, strQuestion;
    public double lat, lng;
    public Location location;
    public int alertDistance = 21;
    public int iCorrectAnswerPoint, iWrongAnswerPoint;
    public String strCorrectAnswer, strWrongAnswer;
    public long startTime, endTime;
    public int iSelectedAnswer;


    public QuestionModel(JSONObject objtmp) {
        this.initWithJsonData(objtmp);
    }

    public void initWithJsonData(JSONObject object)
    {
        if (object == null)
            return;

        iID = object.optInt("id");
        iCompanyID = object.optInt("company_id");
        strCompanyName = object.optString("company_name");
        strSpotName = object.optString("name");
        lat = object.optDouble("lat");
        lng = object.optDouble("lng");
        location = new Location("");//provider name is unnecessary
        location.setLatitude(lat);//your coords of course
        location.setLongitude(lng);

        alertDistance = object.optInt("alert_distance");
        if (alertDistance <= 0)
            alertDistance = 21;

        strContent = object.optString("content");
        strQuestion = object.optString("question");

        int idx;
        arrAnswers.clear();
        JSONArray answers = object.optJSONArray("antwoord");
        if (answers != null) {
            for (idx = 0; idx < answers.length(); idx++) {
                AnswerModel data = new AnswerModel(answers.optJSONObject(idx));
                arrAnswers.add(data);
            }
        }

        strCorrectAnswer = object.optString("correct_answer");
        strWrongAnswer = object.optString("wrong_answer");

        iCorrectAnswerPoint = object.optInt("correct_answer_nr_of_points");
        iWrongAnswerPoint = object.optInt("wrong_answer_nr_of_points");

        arrImages.clear();
        JSONArray images = object.optJSONArray("image");
        if (images != null) {
            for (idx = 0; idx < images.length(); idx++) {
                String objtmp = images.optString(idx);
                arrImages.add(objtmp);
            }
        }

        strCompanyImage = object.optString("companyimage");
    }

    public String getDistanceForDetail(Location curLocation) {
        if (curLocation == null || lat == Double.NaN || lng == Double.NaN)
            return String.format("Unknown");

        int distance = (int)location.distanceTo(curLocation);
        if (distance > 10000)
            return String.format("%d Kilometer", (int)(distance / 1000));
        else
            return String.format("%d Meter", distance);
    }

    public String getImageURL() {
        if (arrImages.size() < 1)
            return "";

        return arrImages.get(0);
    }

    public boolean isReachNearby(Location curLocation) {
        int distance = (int) location.distanceTo(curLocation);
        return distance < alertDistance;
    }

    public int getDuration() {
        return (int)(endTime - startTime) / 1000;
    }

    public boolean isCorrectAnswer() {
        if (iSelectedAnswer < arrAnswers.size()) {
            AnswerModel item = arrAnswers.get(iSelectedAnswer);
            return item.bRight;
        }

        return false;
    }

    public int getScore() {
        if (this.isCorrectAnswer())
            return iCorrectAnswerPoint;

        return iWrongAnswerPoint;
    }

}
