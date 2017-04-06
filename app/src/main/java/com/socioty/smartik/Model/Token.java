package com.socioty.smartik.Model;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by serhiipianykh on 2017-03-15.
 */

public class Token {

    private final static String sharedPref = "com.socioty.smartik.loginCredentials";
    private final static String tokenPref = "com.socioty.smartik.loginCredentials.token";
    private final static String tokenExpPref = "com.socioty.smartik.loginCredentials.exp";
    private final static String sharedUserId = "com.socioty.smartil.loginCredentuals.id";
    public static Token sToken;
    private String mKey;
    private static String userId;
    private long exp;


    public Token(String key) {
        this.mKey = key;

    }

    public static Token get(Context context) {
        if (sToken == null) {
            sToken = new Token(null);
            sToken.setToken(sToken.getSavedToken(context), sToken.getExp(), context);
        }
        return sToken;
    }

    public static String getToken() {
        return sToken.mKey;
    }

    public static void setToken(String mToken, long exp, Context context) {
        sToken = new Token(mToken);
        if (mToken!=null) {
            sToken.saveToken(mToken, exp, context);
        }
    }

    public static void clearToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(tokenPref).commit();
        sharedPreferences.edit().remove(tokenExpPref).commit();
        sToken = null;
    }


    private long getExp() {
        return this.exp;
    }


    private String getSavedToken(Context context) {

        String jwt = null;
        long date = 0;


        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
        jwt = sharedPreferences.getString(tokenPref,null);
        exp = sharedPreferences.getLong(tokenExpPref, 0);
        userId = sharedPreferences.getString(sharedUserId, null);
        if (jwt == null || jwt.equals("")) return null;

        date = System.currentTimeMillis() /1000;

        if (exp>date) return jwt;
        else return null;
    }

    private void saveToken(String token, long exp, Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(tokenPref,token);
        editor.putLong(tokenExpPref, exp);
        editor.commit();
    }

    public static void setUserId(Context context, String userId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(sharedPref, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(sharedUserId, userId);
        editor.commit();
    }

    public static String getUserId() {
        return userId;
    }
}
