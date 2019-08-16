package com.spottz.model;

import org.json.JSONObject;

/**
 * Created by iMac on 8/22/16.
 */
public class RangeModel
{
    public int point, fRange, tRange;

    public RangeModel(JSONObject objtmp) {
        if (objtmp == null)
            return;

        point = objtmp.optInt("points");
        fRange = objtmp.optInt("range_from");
        tRange = objtmp.optInt("range_to");
    }
}
