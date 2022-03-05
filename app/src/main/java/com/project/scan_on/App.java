package com.project.scan_on;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;


import com.project.scan_on.Helper.LocaleManager;
import com.project.scan_on.Helper.Utility;

import java.io.IOException;

public class App extends Application {

    public static final String TAG = "MobileSoution";
    public static LocaleManager localeManager;
    static Activity mActivity;
    MyNetworkReceiver mNetworkReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Utility.bypassHiddenApiRestrictions();



        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {
                mNetworkReceiver = new MyNetworkReceiver();

            }

            @Override
            public void onActivityStarted(Activity activity) {
                mActivity = activity;
//                AsyncTask.execute(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            TrueTime.build().initialize();
//                        } catch (IOException e) {
//                            mActivity.runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                              //      Toast.makeText(activity, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
//                                }
//                            });
//                        }
//                    }
//                });

            }

            @Override
            public void onActivityResumed(Activity activity) {
                mActivity = activity;



                registerNetworkBroadcastForNougat();
            }

            @Override
            public void onActivityPaused(Activity activity) {
                mActivity = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    unregisterReceiver(mNetworkReceiver);
                }
                Log.d("MyApplicationTest", "onActivityResumed: un-registered");
            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });


    }
    private void registerNetworkBroadcastForNougat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            registerReceiver(mNetworkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    @Override
    protected void attachBaseContext(Context base) {
        localeManager = new LocaleManager(base);
        super.attachBaseContext(localeManager.setLocale(base));
        Log.d(TAG, "attachBaseContext");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        localeManager.setLocale(this);
        Log.d(TAG, "onConfigurationChanged: " + newConfig.locale.getLanguage());
    }
}
