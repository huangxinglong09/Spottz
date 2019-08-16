package com.spottz.model;

import android.location.Location;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by iMac on 8/22/16.
 */
public class CategoryModel {
    private final boolean scoremode;
    public int iID;

    // For the Category Type Actions
    //public String type = "free";
    public String type = "free";
    public float price  = 0;
    public String codes;

    public String strTitle, strShortDesc, strDesc, strAddress, strPlace;
    public ArrayList<String> arrImages = new ArrayList<>();
    public ArrayList<RangeModel> arrRanges = new ArrayList<>();
    public double lat, lng;
    public int alertDistance;
    public Location location;
    public int distance = 0;
    public String strShareContent;
    public boolean bCompleted = false;
    public int score;
    public int status;
    public String time;
    public JSONObject object;


    public CategoryModel(JSONObject objtmp) {
        this(objtmp, false);
    }

    public CategoryModel(JSONObject objtmp, boolean scoremode) {
        this.scoremode = scoremode;
        this.initWithJsonData(objtmp);
    }

    private void initWithJsonData(JSONObject object) {
        this.object = object;
        if (object == null)
            return;

        if (this.scoremode) {
            iID = object.optInt("id");
            strTitle = object.optString("title");
            return;
        }

        iID = object.optInt("id");

        // ----- Category Type Actions -------------------------------------------------------
        type = object.optString("type");
        if (object.has("amount")) {
            price = (float) object.optDouble("amount");
            price = ((int) (price * 100)) / 100.0f;
        }
        codes = object.optString("codes");
        if (TextUtils.isEmpty(type)) {
            type = "free";
        } else {
            type = type.toLowerCase();
            if (!type.equals("free") && !type.equals("paid") && !type.equals("code")) {
                type = "free";
            }
        }

        /*if (type.equals("paid") && price <= 0) {
            type = "free";
        }*/

        if (type.equals("code") && TextUtils.isEmpty(codes)) {
            type = "free";
        }
        // ----------------------------------------------------------------------------------

        strTitle = object.optString("title");
        strShortDesc = object.optString("short_content");
        strDesc = object.optString("content");
        strAddress = object.optString("address");
        strPlace = object.optString("place");
        lat = object.optDouble("lat");
        lng = object.optDouble("lng");
        status = object.optInt("status");
        alertDistance = object.optInt("alert_distance");
        if (alertDistance <= 0)
            alertDistance = 21;

        strShareContent = object.optString("score_delen");

        location = new Location("");//provider name is unnecessary
        location.setLatitude(lat);//your coords of course
        location.setLongitude(lng);

        arrImages.clear();
        JSONArray images = object.optJSONArray("image");
        int idx;
        if (images != null) {
            for (idx = 0; idx < images.length(); idx++) {
                String objtmp = images.optString(idx);
                arrImages.add(objtmp);
            }
        }

        arrRanges.clear();
        JSONArray array = object.optJSONArray("ranges");
        if (array != null) {
            for (idx = 0; idx < array.length(); idx++) {
                JSONObject objtmp = array.optJSONObject(idx);
                if (objtmp.length() < 3)
                    continue;

                boolean bInserted = false;
                RangeModel model = new RangeModel(objtmp);
                for (int i = 0; i < arrRanges.size(); i++) {
                    RangeModel tmp = arrRanges.get(i);
                    if (tmp.fRange > model.fRange) {
                        bInserted = true;
                        arrRanges.add(i, model);
                        break;
                    }
                }

                if (!bInserted)
                    arrRanges.add(model);
            }
        }
    }

    public int getTimeScore(int time) {
        for (RangeModel model : arrRanges) {
            if (model.fRange <= time && time <= model.tRange)
                return model.point;
        }

        return 0;
    }

    public String getImageURL() {
        if (arrImages.size() < 1)
            return "";

        return arrImages.get(0);
    }

    public void calculateDistance(Location curloc) {
        distance = 0;
        if (curloc == null) {
            return;
        }

        distance = (int) curloc.distanceTo(this.location);
    }

    public String getDistance(Location curLocation) {
        if (curLocation == null || lat == Double.NaN || lng == Double.NaN)
            return String.format("Afstand tot start: Unknown");

        int distance = (int) location.distanceTo(curLocation);
        if (distance > 10000)
            return String.format("Afstand tot start: %d Km", (int) (distance / 1000));
        else
            return String.format("Afstand tot start: %d M", distance);
    }

    public String getDistanceForDetail(Location curLocation) {
        if (curLocation == null || lat == Double.NaN || lng == Double.NaN)
            return String.format("Unknown");

        int distance = (int) location.distanceTo(curLocation);
        if (distance > 10000)
            return String.format("%d Kilometer", (int) (distance / 1000));
        else
            return String.format("%d Meter", distance);
    }

    public boolean isReachNearby(Location curLocation) {
        if (curLocation == null)
            return false;

        int distance = (int) curLocation.distanceTo(this.location);
        return (distance < 21);
    }
}
