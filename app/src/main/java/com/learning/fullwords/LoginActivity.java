package com.learning.fullwords;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.fullauth.api.enums.OauthExpiryType;
import com.fullauth.api.exception.TokenResponseException;
import com.fullauth.api.model.oauth.OauthAccessToken;
import com.fullauth.api.service.FullAuthOauthService;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
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

public class LoginActivity extends AppCompatActivity{

    Button loginBtn;

    ProgressDialog mProgressDialog;
    private static final String TAG = "LoginActivity";
    private int REQ_CODE = 101;
    private String mToken;
    boolean isConnected = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPrefEditor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginBtn = findViewById(R.id.login);
        sharedPreferences = getSharedPreferences(CommonUtils.PREF,Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(CommonUtils.EMAIL,"");
        Log.d("EmailPrefLogin",name);
        if(sharedPreferences != null){
            if(name != null && !"".equalsIgnoreCase(name)) {
                //land to main activity
                Intent lIntent = new Intent(LoginActivity.this, HomeActivity.class);
                lIntent.putExtra(CommonUtils.ACCESS_TOKEN, getSharedPreferences(CommonUtils.PREF,Context.MODE_PRIVATE).getString(CommonUtils.ACCESS_TOKEN,""));
                lIntent.putExtra(CommonUtils.EMAIL,getSharedPreferences(CommonUtils.PREF,Context.MODE_PRIVATE).getString(CommonUtils.EMAIL,""));
                startActivity(lIntent);
                finish();
            }
        }
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
                    mProgressDialog.show();
                    Intent lGoogleAccountIntent= AccountPicker.newChooseAccountIntent(null, null, new String[]{"com.google"}, false, null, null, null, null);
                    lGoogleAccountIntent.putExtra("overrideTheme", 1);
                    lGoogleAccountIntent.putExtra("overrideCustomTheme", 0);

                    startActivityForResult(lGoogleAccountIntent, REQ_CODE);
                    Log.d(TAG, "googleSignIn: ");
                } else{
                    Toast.makeText(getApplicationContext(),"Check your internet connection", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int pRequestCode, int pResultCode, Intent pIntent) {
        super.onActivityResult(pRequestCode, pResultCode, pIntent);

        if (pRequestCode == REQ_CODE) {
            mProgressDialog.cancel();
            Log.d(TAG, "onActivityResult:  google signin");
            if (pIntent != null) {
                String lUserEmail = pIntent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                String lAccontType = pIntent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                Log.d(TAG, "onActivityResult: email " + lUserEmail + " account type " + lAccontType);
                sharedPrefEditor = sharedPreferences.edit();
                sharedPrefEditor.putString(CommonUtils.EMAIL,lUserEmail);
                sharedPrefEditor.commit();
                Account lAccout = new Account(lUserEmail, lAccontType);
                new GoogleSignInAsyncTask().execute(lAccout);
            }
        }
    }

    class GoogleSignInAsyncTask extends AsyncTask<Account, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Synchronizing");
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(Account... pAccount) {

            /**
             * getting google Account Access Token Using GoogleAuthUtil
             */
            Account lAccount = pAccount[0];
            String scope = "oauth2:profile email";
            String lAccessToken = null;
            try {

                //get the access token for the user from the google
                mToken = GoogleAuthUtil.getToken(getApplicationContext(), lAccount, scope);
                Log.d(TAG, "doInBackground: access token " + mToken);
                FullAuthOauthService authService = FullAuthOauthService.builder()
                        .authDomain("fullcreative")
                        .clientId("29354-59055802f154a13d1893f89828768af1")
                        .clientSecret("mwjGbrCJQO7MfODvw0_4UQA5Qb161bQIi9cwHA6Y")
                        .build();

                Set<String> scopes = new HashSet<>();
                scopes.add("awapis.fullaccess");

                try {
                    OauthAccessToken token;
                    token = authService.requestAccessTokenForGoogleToken(mToken, scopes, OauthAccessType.OFFLINE);

                    //lAccessToken = token.getAccessToken();
                    String lRefreshToken = token.getRefreshToken();
                    Log.i("OAuthLoginTask", "refresh token - " + lRefreshToken);
                    token = authService.refreshAccessToken(token.getRefreshToken() , OauthExpiryType.LONG);
                    lAccessToken = token.getAccessToken();
                    Log.i("OAuthLoginTask", "FullAuth Token - " + lAccessToken);
                    sharedPrefEditor = sharedPreferences.edit();
                    sharedPrefEditor.putString(CommonUtils.ACCESS_TOKEN,lAccessToken);
                    sharedPrefEditor.putString(CommonUtils.REFRESH_TOKEN,lAccessToken);
                    sharedPrefEditor.commit();
                } catch (TokenResponseException e) {
                    e.printStackTrace();
                } catch (Exception e){
                    e.printStackTrace();
                }

            } catch (UserRecoverableAuthException e) {
                startActivityForResult(e.getIntent(), REQ_CODE);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return lAccessToken;
        }

        @Override
        protected void onPostExecute(String pResult) {
            super.onPostExecute(pResult);
            mProgressDialog.cancel();
            Intent lIntent = new Intent(LoginActivity.this, HomeActivity.class);
            lIntent.putExtra(CommonUtils.ACCESS_TOKEN, pResult);
            lIntent.putExtra(CommonUtils.EMAIL,getSharedPreferences(CommonUtils.PREF,Context.MODE_PRIVATE).getString(CommonUtils.EMAIL,""));
            startActivity(lIntent);
            finish();
            Log.d(TAG, "onPostExecute: " + pResult);
        }
    }
}
