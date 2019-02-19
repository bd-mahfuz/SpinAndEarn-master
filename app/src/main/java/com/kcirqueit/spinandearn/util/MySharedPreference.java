package com.kcirqueit.spinandearn.util;

import android.content.Context;
import android.content.SharedPreferences;

public class MySharedPreference {

    private static MySharedPreference mySharedPreference;
    private SharedPreferences sharedPreferences;

    public static MySharedPreference getInstance(Context context) {
        if (mySharedPreference == null) {
            mySharedPreference = new MySharedPreference(context);
            return mySharedPreference;
        }

        return mySharedPreference;
    }


    private MySharedPreference(Context context) {
        sharedPreferences = context.getSharedPreferences("MyPref", 0);
    }

    public void saveData(String key, String data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, data);
        editor.apply();
    }

    public String getData(String key) {
        if (sharedPreferences != null) {
            return sharedPreferences.getString(key, "");
        }
        return "";
    }


}
