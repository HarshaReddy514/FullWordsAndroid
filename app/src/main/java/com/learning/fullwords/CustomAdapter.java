package com.learning.fullwords;

import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import Utils.CommonUtils;

/**
 * Created by harsha on 09/02/17.
 */
public class CustomAdapter extends ArrayAdapter<WordsData> {

    ArrayList<WordsData> wordsDataSet;
    Context mContext;

    public CustomAdapter(ArrayList<WordsData> wordsData, Context applicationContext) {
        super(applicationContext, R.layout.words_list, wordsData);
        this.wordsDataSet = wordsData;
        this.mContext=applicationContext;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        WordsData wordsData = getItem(position);
        ViewHolder viewHolder;
        final View result;
        if (CommonUtils.isNull(convertView)) {

            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.words_list, parent, false);
            viewHolder.word = (TextView) convertView.findViewById(R.id.word_tv);
            viewHolder.meaning = (TextView) convertView.findViewById(R.id.meaning_tv);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }
        viewHolder.word.setText(wordsData.getWord());
        viewHolder.meaning.setText(wordsData.getMeaning());
        return convertView;
    }

    private static class ViewHolder {
        TextView word;
        TextView meaning;
    }
}
