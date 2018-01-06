package com.example.nathanphan.googlemapstest;


import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by luca on 18/1/2016.
 */
public class AppInfo {

    private static AppInfo instance = null;
    private static final String LOCATION_NAME = "location";
    private static String UPLOAD_NAME ="upload";


    protected AppInfo() {
        // Exists only to defeat instantiation.
    }

    // Here are some values we want to keep global.
    public String sharedString;


    private Context my_context;

    public static AppInfo getInstance(Context context) {
        if(instance == null) {
            instance = new AppInfo();
            instance.my_context = context;
            SharedPreferences settings = context.getSharedPreferences(MainActivity.MY_LOCATION, 0);
            instance.sharedString = settings.getString(LOCATION_NAME, null);
        }
        return instance;
    }

    public void setLocationName(String c) {
        instance.sharedString = c;
        SharedPreferences settings = my_context.getSharedPreferences(MainActivity.MY_LOCATION, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(LOCATION_NAME, c);
        editor.commit();
    }


}
