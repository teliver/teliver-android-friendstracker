package com.friendstrkcustomer;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.teliver.sdk.core.Teliver;


public class Application extends android.app.Application {


    private SharedPreferences preferences;

    private SharedPreferences.Editor editor;

    @Override
    public void onCreate() {
        super.onCreate();
        Teliver.init(this,"teliver_key");
        preferences = getSharedPreferences(getString(R.string.app_name),MODE_PRIVATE);
        editor = preferences.edit();
        editor.apply();
    }

    public void storeBoolean(String key,boolean value){
        editor.putBoolean(key,value);
        editor.commit();
    }

    public boolean getBoolean(String key){
        return preferences.getBoolean(key,false);
    }

    public boolean checkPermission(Activity context){
            if (Build.VERSION.SDK_INT >= 23) {
                if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(context, android.Manifest.permission.ACCESS_COARSE_LOCATION))
                        ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                                4);
                    else {
                        ActivityCompat.requestPermissions(context, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION}, 4);
                    }
                } else return true;
            } else return true;
            return false;
        }

}