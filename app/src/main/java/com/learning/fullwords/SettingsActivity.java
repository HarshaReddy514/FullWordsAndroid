package com.learning.fullwords;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
