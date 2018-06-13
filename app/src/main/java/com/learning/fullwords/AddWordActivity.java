package com.learning.fullwords;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import helper.HttpConnection;

public class AddWordActivity extends AppCompatActivity {

    EditText wordET, meaningET, sourceET;
    SharedPreferences sharedPreferences;
    ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);
        Button saveWord = findViewById(R.id.saveWord);
        wordET = findViewById(R.id.word);
        meaningET = findViewById(R.id.meaning);
        sourceET = findViewById(R.id.source);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);

        saveWord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String word = wordET.getText().toString();
                    String meaning = meaningET.getText().toString();
                    String source = sourceET.getText().toString();
                    if(CommonUtils.isNull(word) || CommonUtils.isEmptyString(word)){
                        CommonUtils.showToast(getApplicationContext(),"Please enter the word");
                    }
                    if(CommonUtils.isNull(meaning) || CommonUtils.isEmptyString(meaning)){
                        CommonUtils.showToast(getApplicationContext(),"Please enter the meaning");
                    }
                    if(CommonUtils.isNull(source) || CommonUtils.isEmptyString(source)){
                        CommonUtils.showToast(getApplicationContext(),"Please enter the source");
                    }
                    if(!CommonUtils.isEmptyString(word) && !CommonUtils.isNull(word) && !CommonUtils.isNull(meaning) && !CommonUtils.isEmptyString(meaning)
                            && !CommonUtils.isNull(source) && !CommonUtils.isEmptyString(source)){
                        new AddWordAsyncTask().execute(word,meaning,source);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    class AddWordAsyncTask extends AsyncTask<String, Void, HashMap>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Updating...");
            mProgressDialog.show();
        }

        @Override
        protected HashMap doInBackground(String... strings) {
            String word = strings[0];
            String meaning = strings[1];
            String source = strings[2];

            String content = "Hey! Here is the new word I found\n\n *Word* : " + word + "\n *Meaning* : " + meaning
                    + "\n *Source* : " + source + "\n #fullwords";
            HashMap<String, Object> respMap = null;
            try {
                sharedPreferences = getSharedPreferences(CommonUtils.PREF, Context.MODE_PRIVATE);
                String accessToken = sharedPreferences.getString(CommonUtils.ACCESS_TOKEN, "");
                //api call to save word
                HashMap<String, String> reqHeaders = new HashMap<>();
                reqHeaders.put("Content-Type", "application/json");
                reqHeaders.put("Authorization", "Bearer " + accessToken);

                HashMap<String, Object> reqBodyMap = new HashMap<>();

                reqBodyMap.put("word", word);
                reqBodyMap.put("desc", meaning);
                reqBodyMap.put("src", source);
                ObjectMapper mapper = new ObjectMapper();

                String reqBodyStr = mapper.writeValueAsString(reqBodyMap);
                String response = HttpConnection.getHttpResponse(CommonUtils.ADD_WORD_API, "POST", reqBodyStr, reqHeaders);
                System.out.println("Response from WORDS :: " + response);
                TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
                };
                respMap = mapper.readValue(response, typeRef);
                if ((boolean) respMap.get("response")) {
                    HashMap<String, String> reqHeadersForAW = new HashMap<>();
                    reqHeadersForAW.put("Content-Type", "application/json");
                    reqHeadersForAW.put("Authorization", "Bearer " + accessToken);

                    HashMap<String, Object> reqBodyMapForAW = new HashMap<>();

                    reqBodyMapForAW.put("content", content);
                    reqBodyMapForAW.put("type", "update");

                    String reqBodyStrAW = mapper.writeValueAsString(reqBodyMapForAW);
                    String respAW = HttpConnection.getHttpResponse(CommonUtils.AW_FEEDS_API, "POST", reqBodyStrAW, reqHeadersForAW);
                    Log.d("Response from AW ", respAW);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return respMap;
        }

        @Override
        protected void onPostExecute(HashMap response) {
            super.onPostExecute(response);
            mProgressDialog.cancel();
            if((boolean)response.get("response")){
                Intent homeIntent = new Intent(getApplicationContext(),HomeActivity.class);
                startActivity(homeIntent);
                finish();
            } else{
                CommonUtils.showToast(getApplicationContext(),response.get("msg").toString());
            }
        }
    }
}
