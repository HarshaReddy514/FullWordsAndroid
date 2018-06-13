package com.learning.fullwords;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowWordActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    String wordToShow, meaningToShow, createdOn, sourceToShow;
    long createdOnMillis;
    TextView wordTV, meaningTV, sourceTV ,dateTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_word);

        wordTV = findViewById(R.id.word_show_tv);
        meaningTV = findViewById(R.id.meaning_show_tv);
        dateTV = findViewById(R.id.date_show_tv);
        sourceTV = findViewById(R.id.src_tv_show);

        Intent intent = getIntent();
        String wordId = intent.getStringExtra("wordSelected");
        System.out.println("WORDID :: " + wordId);
        sharedPreferences = getSharedPreferences(CommonUtils.PREF, Context.MODE_PRIVATE);
        String wordsMapStr = sharedPreferences.getString(CommonUtils.WORDS_LIST,"");
        System.out.println("WORDLIST :: " + wordsMapStr);
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
        };
        try{
            HashMap<String,Object> responseMap = mapper.readValue(wordsMapStr,typeRef);
            HashMap<String,Object> resp = (HashMap<String, Object>) responseMap.get("data");
            List<HashMap<String,Object>> listOfWords = (List<HashMap<String, Object>>) resp.get("words");
            if(!CommonUtils.isEmptyList(listOfWords)) {
                for(int i=0; i< listOfWords.size(); i++) {
                    if (wordId.equalsIgnoreCase(listOfWords.get(i).get("id").toString())) {
                        wordToShow = listOfWords.get(i).get("word").toString();
                        meaningToShow = listOfWords.get(i).get("desc").toString();
                        createdOnMillis = (Long)listOfWords.get(i).get("createdAt");
                        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
                        createdOn = df.format(createdOnMillis);
                        sourceToShow = listOfWords.get(i).get("src").toString();
                    }
                }
            }
            wordTV.setText(wordToShow);
            meaningTV.setText("Meaning : " + meaningToShow);
            sourceTV.setText("Source : " + sourceToShow);
            dateTV.setText("Added on "+ createdOn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
