package com.learning.fullwords;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.fullauth.api.enums.OauthAccessType;
import com.fullauth.api.enums.OauthExpiryType;
import com.fullauth.api.exception.TokenResponseException;
import com.fullauth.api.model.oauth.OauthAccessToken;
import com.fullauth.api.service.FullAuthOauthService;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Utils.CommonUtils;

public class LoginActivity extends AppCompatActivity{

    Button loginBtn;

    ProgressDialog mProgressDialog;
    private static final String TAG = "LoginActivity";
    private int REQ_CODE = 101;
    private String mToken;
    boolean isConnected = false;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPrefEditor;
    Map<String,String> preferencesMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginBtn = findViewById(R.id.login);
        sharedPreferences = getSharedPreferences(CommonUtils.PREF,Context.MODE_PRIVATE);
        String name = sharedPreferences.getString(CommonUtils.EMAIL,"");
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
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Fetching accounts...");

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
                } else{
                    CommonUtils.showToast(getApplicationContext(),"Check your internet connection");
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int pRequestCode, int pResultCode, Intent pIntent) {
        super.onActivityResult(pRequestCode, pResultCode, pIntent);

        if (pRequestCode == REQ_CODE) {
            mProgressDialog.cancel();
            if (pIntent != null) {
                String lUserEmail = pIntent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                String lAccountType = pIntent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
                Log.d(TAG, "onActivityResult: email " + lUserEmail + " account type " + lAccountType);
                preferencesMap.put(CommonUtils.EMAIL,lUserEmail);
                CommonUtils.savePreferences(sharedPreferences,preferencesMap);
                Account lAccout = new Account(lUserEmail, lAccountType);
                new GoogleSignInAsyncTask().execute(lAccout);
            }
        }
    }

    class GoogleSignInAsyncTask extends AsyncTask<Account, Void, HashMap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Signing In...");
            mProgressDialog.show();
        }

        @Override
        protected HashMap<String, String> doInBackground(Account... pAccount) {

            /**
             * getting google Account Access Token Using GoogleAuthUtil
             */
            Account lAccount = pAccount[0];
            String scope = "oauth2:profile email";
            String lAccessToken = null, lRefreshToken;
            HashMap<String,String> response = new HashMap<>();
            try {

                //get the access token for the user from the google
                mToken = GoogleAuthUtil.getToken(getApplicationContext(), lAccount, scope);
                Log.d(TAG, "doInBackground: access token " + mToken);
                FullAuthOauthService authService = FullAuthOauthService.builder()
                        .authDomain(CommonUtils.FULL_AUTH_DOMAIN)
                        .clientId(CommonUtils.CLIENT_ID)
                        .clientSecret(CommonUtils.CLIENT_SECRET)
                        .build();

                Set<String> scopes = new HashSet<>();
                scopes.add("awapis.fullaccess");

                try {
                    OauthAccessToken token;
                    token = authService.requestAccessTokenForGoogleToken(mToken, scopes, OauthAccessType.OFFLINE);
                    token = authService.refreshAccessToken(token.getRefreshToken() , OauthExpiryType.LONG);
                    lAccessToken = token.getAccessToken();
                    lRefreshToken = token.getRefreshToken();
                    preferencesMap.put(CommonUtils.ACCESS_TOKEN,lAccessToken);
                    preferencesMap.put(CommonUtils.REFRESH_TOKEN,lRefreshToken);
                    CommonUtils.savePreferences(sharedPreferences,preferencesMap);
                    response.put("accessToken",lAccessToken);
                } catch (TokenResponseException e) {
                    response.put("msg",e.getMessage());
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
            return response;
        }

        @Override
        protected void onPostExecute(HashMap resp) {
            super.onPostExecute(resp);
            mProgressDialog.cancel();
            if(!CommonUtils.isNull(resp.get("accessToken")) && !CommonUtils.isEmptyString(resp.get("accessToken").toString())){
                String token = resp.get("accessToken").toString();
                Intent lIntent = new Intent(LoginActivity.this, HomeActivity.class);
                lIntent.putExtra(CommonUtils.ACCESS_TOKEN, token);
                lIntent.putExtra(CommonUtils.EMAIL,getSharedPreferences(CommonUtils.PREF,Context.MODE_PRIVATE).getString(CommonUtils.EMAIL,""));
                startActivity(lIntent);
                finish();
                Log.d(TAG, "onPostExecute: " + token);
            } else{
                CommonUtils.showToast(getApplicationContext(),resp.get("msg").toString());
            }
        }
    }
}
