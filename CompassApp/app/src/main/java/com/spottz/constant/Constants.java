package com.spottz.constant;

/**
 * Created by lionking on 6/20/17.
 */

public class Constants {

    // 1 second
    public static final int GPS_MIN_TIME = 1000;
    // 1 meter
    public static final int GPS_MIN_DISTANCE = 1;


    /**
     * Loaded Flags
     */
    public static final int INT_STANBY_WHILE_LOADING       = 0x10;
    public static final int INT_LOADED_CATEGORY_LIST       = 0x1001;
    public static final int INT_LOADED_CATEGORY_DETAIL     = 0x1002;
    public static final int INT_LOADED_SCORE               = 0x1008;
    public static final int INT_UPDATED_LOCATION_CHANGED   = 0x1010;

    public static final int INT_STARTROUTETEXT_LOADED      = 0x1080;
    public static final int INT_LOGINTEX_LOADED            = 0x1081;
    public static final int INT_SIGNUPTEXT_LOADED          = 0x1082;
    public static final int INT_SKIPTEXT_LOADED            = 0x1083;

    /**
     * Loaded Check Flag
     */
    public static final int INT_LOADED_CHECK_CATEGORY      = INT_LOADED_CATEGORY_LIST | INT_LOADED_SCORE;
    public static final int INT_LOADED_ALL_DATA = INT_LOADED_CATEGORY_LIST | INT_STARTROUTETEXT_LOADED | INT_LOGINTEX_LOADED | INT_SIGNUPTEXT_LOADED | INT_SKIPTEXT_LOADED;

    /**
     * Current Screen Fragment Flag
     */
    public static final int INT_FRMT_CATEGORY_LIST      = 0x2001;
    public static final int INT_FRMT_CATEGORY_DETAIL    = 0x2002;
    public static final int INT_FRMT_RESULT             = 0x2008;
    public static final int INT_FRMT_ROUTE              = 0x2010;
    public static final int INT_FRMT_QUESTION           = 0x2020;
    public static final int INT_FRMT_SHARE              = 0x2080;
    public static final int INT_FRMT_SCORE              = 0x2100;
    public static final int INT_FRMT_MYROUTE_LIST       = 0x2101;



    public static final int TYPE_PHOTO_PICK_FROM_CAMERA = 0x9000;
}
