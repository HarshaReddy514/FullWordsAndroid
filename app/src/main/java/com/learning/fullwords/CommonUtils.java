package com.learning.fullwords;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CommonUtils {

    public static final String ACCESS_TOKEN = "access_token";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String EMAIL = "email";
    public static final String PREF = "MyPreferences";
    public static final String WORDS_LIST = "words_list";

    public static final String FULL_AUTH_DOMAIN = "fullcreative";
    public static final String CLIENT_ID = "29354-59055802f154a13d1893f89828768af1";
    public static final String CLIENT_SECRET = "mwjGbrCJQO7MfODvw0_4UQA5Qb161bQIi9cwHA6Y";

    //api urls
    public static final String AW_FEEDS_API = "https://api.anywhereworks.com/api/v1/feed";
    public static final String ADD_WORD_API = "https://full-learn.appspot.com/api/v1/words";
    public static final String ME_API = "https://full-learn.appspot.com/api/v1/words/user/me";


    public static boolean isNull(Object obj) {
        if (obj != null) {
            return false;
        }
        return true;
    }

    public static boolean isEmptyString(String string) {
        if (string != null && !("".equalsIgnoreCase(string.trim())) && string.trim().length() > 0
                && !"null".equalsIgnoreCase(string.trim())) {
            return false;
        }
        return true;
    }

    public static boolean isEmptyStringArray(String[] stringArray) {
        if (stringArray == null || stringArray.length < 1) {
            return true;
        }
        return false;
    }

    public static boolean isEmptyList(ArrayList<?> list) {
        if (list == null || list.isEmpty() || list.size() < 1) {
            return true;
        }
        return false;
    }

    public static boolean isEmptyList(List<?> list) {
        if (list == null || list.isEmpty() || list.size() < 1) {
            return true;
        }
        return false;
    }

    public static boolean isEmptyMap(Map<?, ?> map) {
        if (map == null || map.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean isEmptyMap(HashMap<?, ?> hashMap) {
        if (hashMap == null || hashMap.isEmpty()) {
            return true;
        }
        return false;
    }

    public static boolean isEmptyMap(TreeMap<?, ?> treeMap) {
        if (treeMap == null || treeMap.isEmpty()) {
            return true;
        }
        return false;
    }

    public static void showToast(Context context, String msg) {
        Toast.makeText(context,msg,Toast.LENGTH_LONG).show();
    }

    public static void savePreferences(SharedPreferences sharedPreferences, Map<String, String> preferencesMap) {
        SharedPreferences.Editor sharedPrefEditor = sharedPreferences.edit();
        for(Map.Entry<String,String> entry : preferencesMap.entrySet()){
            sharedPrefEditor.putString(entry.getKey(),entry.getValue());
        }
        sharedPrefEditor.commit();
    }
}
