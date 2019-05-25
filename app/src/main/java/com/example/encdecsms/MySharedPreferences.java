package com.example.encdecsms;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Dell on 11/28/2017.
 */

public class MySharedPreferences {
    public static SharedPreferences mSharedPrefrences;
    public static MySharedPreferences mInstance;
    public static Context mContext;

    private String SHARED_PREF_NAME = "ST";
    private String DEFAULT = "";

    public MySharedPreferences(){
        mSharedPrefrences = mContext.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
    }
    public static MySharedPreferences getInstance(Context context){
        mContext = context;
        if (mInstance == null){
            mInstance = new MySharedPreferences();
        }
        return mInstance;  
    }
    public void setInfo (String key,String value) {
        mSharedPrefrences.edit().putString(key,value).apply();
    }
    public String getInfo(String key){
        return mSharedPrefrences.getString(key,DEFAULT);
    }
    public void setLogin (String value) {
        mSharedPrefrences.edit().putString("login",value).apply();
    }
    public String isLogin(){
        return mSharedPrefrences.getString("login","0");
    }


}
