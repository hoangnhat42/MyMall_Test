package com.project.scan_on;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

public class MyNetworkReceiver extends BroadcastReceiver {
    private final String TAG = "MyNetworkReciver";
    public static boolean isActive=false;
    @Override
    public void onReceive(Context context, Intent intent) {
        isActive = isOnline(context);
        Activity activity = App.mActivity ; // Getting Current Activity
        if (!isActive) {
            Toast.makeText(activity, "No Internet Connection", Toast.LENGTH_SHORT).show();
          // DBqueries.clearData();
        }
    }
    //returns internet connection
    public boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());

        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }
}
