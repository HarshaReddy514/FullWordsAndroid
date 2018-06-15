package com.learning.fullwords;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import Utils.CommonUtils;

public class SettingsActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPrefEditor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        TextView userMailTV = findViewById(R.id.userMailId);
        sharedPreferences = getSharedPreferences(CommonUtils.PREF, Context.MODE_PRIVATE);
        String userMail = sharedPreferences.getString(CommonUtils.EMAIL,"");
        userMailTV.setText(userMail);
    }
}
