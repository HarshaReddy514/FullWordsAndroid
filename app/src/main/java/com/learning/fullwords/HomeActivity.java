package com.learning.fullwords;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.CommonUtils;
import helper.HttpConnection;

public class HomeActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor sharedPrefEditor;
    CustomAdapter adapter;
    ListView listView;
    ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        listView = findViewById(R.id.words_list);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setCancelable(false);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addWordActivity = new Intent(getApplicationContext(), AddWordActivity.class);
                startActivity(addWordActivity);
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView wordTV =  view.findViewById(R.id.word_tv);
                String wordSelected = wordTV.getText().toString();
                Intent intent = new Intent(getApplicationContext(),ShowWordActivity.class);
                intent.putExtra("wordSelected",wordSelected.toLowerCase());
                startActivity(intent);
            }
        });

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = false;
        if(networkInfo != null){
            isConnected = networkInfo.isConnectedOrConnecting();
        }
        if(isConnected){
            new GetWordsAsyncTask().execute();
        } else{
            CommonUtils.showToast(getApplicationContext(),"Check your internet connection");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_words, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.profile:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.sign_out:
                logout();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void logout() {
        sharedPreferences = getSharedPreferences(CommonUtils.PREF, Context.MODE_PRIVATE);
        sharedPrefEditor = sharedPreferences.edit();
        sharedPrefEditor.clear();
        sharedPrefEditor.commit();
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private class GetWordsAsyncTask extends AsyncTask<String,Void,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog.setMessage("Synchronizing words...");
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... strings) {
            sharedPreferences = getSharedPreferences(CommonUtils.PREF, Context.MODE_PRIVATE);
            String accessToken = sharedPreferences.getString(CommonUtils.ACCESS_TOKEN,"");
            HashMap<String, String> reqHeaders = new HashMap<>();
            reqHeaders.put("Authorization","Bearer " + accessToken);
            long endMilliSeconds = new Date().getTime();
            String response = HttpConnection.getHttpResponse(CommonUtils.ME_API+"?endTime="+endMilliSeconds+"&startTime=1526899085748","GET","",reqHeaders);
            sharedPrefEditor = sharedPreferences.edit();
            sharedPrefEditor.putString(CommonUtils.WORDS_LIST,response.toString());
            sharedPrefEditor.commit();
            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            mProgressDialog.cancel();
            try {
                buildWords(response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("onPostExecute", response);
        }
    }

    private void buildWords(String response) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
        };
        HashMap<String,Object> responseMap = mapper.readValue(response,typeRef);
        HashMap<String,Object> resp = (HashMap<String, Object>) responseMap.get("data");
        List<HashMap<String,String>> listOfWords = (List<HashMap<String, String>>) resp.get("words");
        String word, meaning;
        ArrayList<WordsData> wordsData = new ArrayList<>();
        if(!CommonUtils.isEmptyList(listOfWords)) {
            for(int i=0; i< listOfWords.size(); i++){
                word = listOfWords.get(i).get("word");
                meaning = listOfWords.get(i).get("desc");
                wordsData.add(new WordsData(word,meaning));
            }
            adapter = new CustomAdapter(wordsData, getApplicationContext());
            listView.setAdapter(adapter);
        } else{
            CommonUtils.showToast(getApplicationContext(),"No words");
        }
    }
}
