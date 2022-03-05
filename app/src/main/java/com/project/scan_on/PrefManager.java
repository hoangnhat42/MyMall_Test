package com.project.scan_on;

/**
 * Created by kapil on 20/01/17.
 */
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;


public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;


    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "referral";
    private static final String GEO_POINT = "latitude";
    private static final String NOW_TIME = "nowtime";


    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public long getNowTime() {
        return pref.getLong(NOW_TIME, -1);

    }

    public String getGeoPoint() {
        return pref.getString(GEO_POINT, "");
    }


    public void setDate(long timestamp) {
        editor.putLong(NOW_TIME, timestamp);
        editor.commit();

    }


    public void setGeoPoint(GeoPoint geoPoint) {
        Gson gson = new Gson();
        String json = gson.toJson(geoPoint);
        editor.putString(GEO_POINT, json);
        editor.commit();

    }






}