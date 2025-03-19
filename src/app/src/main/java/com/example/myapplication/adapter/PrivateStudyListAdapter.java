package com.example.myapplication.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.model.Vocabulary;

import java.util.List;

public class PrivateStudyListAdapter extends ArrayAdapter<Vocabulary> {
    private Context context;
    private List<Vocabulary> vocabularies;

    public PrivateStudyListAdapter(Context context, List<Vocabulary> vocabularies) {
        super(context, 0, vocabularies);
        this.context = context;
        this.vocabularies = vocabularies;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_private_study_list, parent, false);
        }

        TextView tv_word = convertView.findViewById(R.id.tv_word);
        TextView tv_definition = convertView.findViewById(R.id.tv_definition);

        Vocabulary vocabulary = vocabularies.get(position);
        tv_word.setText(vocabulary.getWord());
        tv_definition.setText(vocabulary.getDefinition());

        return convertView;
    }
}
