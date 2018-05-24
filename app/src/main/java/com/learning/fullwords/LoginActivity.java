package com.learning.fullwords;

import android.accounts.Account;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.fullauth.api.enums.OauthAccessType;
import com.fullauth.api.model.oauth.OauthAccessToken;
import com.fullauth.api.service.FullAuthOauthService;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

import helper.HttpConnection;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    Button loginBtn;

    ProgressDialog mProgressDialog;
    private static final String TAG = "LoginActivity";
    private int REQ_CODE = 101;
    private String mToken;
    boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginBtn = findViewById(R.id.login);
        mProgressDialog = new ProgressDialog(this, R.style.Theme_AppCompat_Dialog);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Signing in");

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo != null){
            isConnected = networkInfo.isConnectedOrConnecting();
        }

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isConnected){

                    Intent lGoogleBrowserIntent = new Intent("android.intent.action.VIEW");
                    lGoogleBrowserIntent.setData(Uri.parse("https://access.anywhereworks.com/o/oauth2/auth?response_type=code&client_id=29354-cf1ea9b7f06d4f002c7c6f04e2bef92d&redirect_uri=https://fullwordsandroid.aw.com/&scope=awapis.identity,awapis.users.read&access_type=offline"));
                    startActivity(lGoogleBrowserIntent);
                    finish();

                } else{
                    Toast.makeText(getApplicationContext(),"Check your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    protected void onResume() {
        super.onResume();
        Uri lUri = getIntent().getData();
        if (lUri != null && lUri.toString().contains("code")) {
            Log.d(TAG, "onResume: code - " + lUri.getQueryParameter("code"));
            new FullAccessAsyncTask().execute(new String[] {
                    lUri.getQueryParameter("code")
            });
        }
        Log.d(TAG, "onResume: " + getIntent().getData());
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    class FullAccessAsyncTask extends AsyncTask<String, Void, Boolean> {
        String mAccessToken;
        String mEmailID;
        String mImageID;
        ProgressDialog mProgressDialog;
        String mRefreshToken;
        String mUserID;
        String mUserName;

        FullAccessAsyncTask() {}

        protected void onPreExecute() {
            super.onPreExecute();
            this.mProgressDialog = new ProgressDialog(LoginActivity.this, R.style.Theme_AppCompat);
            this.mProgressDialog.setProgressStyle(0);
            this.mProgressDialog.setMessage("Signing in");
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.show();
        }

        protected Boolean doInBackground(String...pCode) {
            try {
                String lHttpRequestBody = "client_id=29354-cf1ea9b7f06d4f002c7c6f04e2bef92d&client_secret=O1bj1ciZqJSXOUi6EtNLBiq19YRdHC2TIzDp6VdO&redirect_uri=https://fullwordsandroid.aw.com/&code=" + pCode[0] + "&grant_type=authorization_code";
                HashMap < String, String > lHttpHeader = new HashMap();
                lHttpHeader.put("Content-Type", "application/x-www-form-urlencoded");
                String lAccessTokenRespponse = HttpConnection.getHttpResponse("https://access.anywhereworks.com/o/oauth2/v1/token", "POST", lHttpRequestBody, lHttpHeader);
                JSONObject lJsonObject = new JSONObject(lAccessTokenRespponse);
                this.mAccessToken = lJsonObject.get("access").toString();
                this.mRefreshToken = lJsonObject.get("refresh_token").toString();
                int lExpriresTime = ((Integer) lJsonObject.get("expires_in")).intValue();
                LoginActivity.this.getSharedPreferences("shared_preference", 0).edit().putString("access_token", mAccessToken).commit();
                Log.d(LoginActivity.TAG, "doInBackground: accessToken response " + lAccessTokenRespponse);
                Log.d(LoginActivity.TAG, "doInBackground: accessToken " + this.mAccessToken);
                Log.d(LoginActivity.TAG, "doInBackground: RefreshToken " + this.mRefreshToken);
                Log.d(LoginActivity.TAG, "doInBackground: expireTime" + lExpriresTime);
                HashMap<String, String> lHttpHeader2 = new HashMap<>();
                lHttpHeader2.put("Content-Type", "application/json");
                lHttpHeader2.put("Authorization", "BEARER " + this.mAccessToken);
                String lMyChallengeDetailsResponse = "";
                lMyChallengeDetailsResponse = HttpConnection.getHttpResponse("https://api.anywhereworks.com/api/v1/user/me", "GET", "nobody", lHttpHeader2);
                if (lMyChallengeDetailsResponse != null) {
                    JSONObject lMyChallengeJson = new JSONObject(lMyChallengeDetailsResponse);
                    if (!((Boolean) lMyChallengeJson.get("ok")).booleanValue()) {
                        return Boolean.valueOf(false);
                    }
                    JSONObject lUserDeatilsJSon = lMyChallengeJson.getJSONObject("data").getJSONObject("user");
                    this.mUserID = lUserDeatilsJSon.getString("id");
                    Log.d(LoginActivity.TAG, "doInBackground: user id " + this.mUserID);
                    if (lUserDeatilsJSon.has("lastName")) {
                        this.mUserName = lUserDeatilsJSon.getString("firstName") + " " + lUserDeatilsJSon.getString("lastName");
                    } else {
                        this.mUserName = lUserDeatilsJSon.getString("firstName");
                    }
                    this.mEmailID = lUserDeatilsJSon.getString("login");
                    this.mImageID = lUserDeatilsJSon.getString("photoId");
                    LoginActivity.this.getSharedPreferences("shared_preference", 0).edit().putString("account_id", lUserDeatilsJSon.getString("accountId")).commit();
                    Log.d(LoginActivity.TAG, "doInBackground: id " + this.mUserID + " name  " + this.mUserName + " email " + this.mEmailID + " photo id " + this.mImageID);
                    Log.d(LoginActivity.TAG, "doInBackground: my challenge Details" + lMyChallengeDetailsResponse);
                }
                return Boolean.valueOf(true);
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(LoginActivity.TAG, "doInBackground: json exception");
                return false;
            }
        }

        protected void onPostExecute(Boolean pIsErrorOccurred) {
            super.onPostExecute(Boolean.valueOf(!pIsErrorOccurred.booleanValue()));
            this.mProgressDialog.dismiss();
            if (pIsErrorOccurred.booleanValue()) {
                LoginActivity.this.getSharedPreferences("shared_preference", 0).edit().putString("user_id", this.mUserID).putString("user_name", this.mUserName).putString("email", this.mEmailID).putString("user_image", this.mImageID).putString("refresh_token", this.mRefreshToken).putBoolean("isUserLoggedIn", true).commit();
//                LoginActivity.this.startActivity(new Intent(LoginActivity.this, HomeScreenActivity.class));
//                LoginActivity.this.finish();
                return;
            }
            this.mProgressDialog.dismiss();
        }
    }
}
