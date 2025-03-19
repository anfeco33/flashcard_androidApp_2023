package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.activity.EditTopic;
import com.example.myapplication.activity.EditVocabulary;
import com.example.myapplication.activity.VocabularyManagement;
import com.example.myapplication.model.Vocabulary;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.BitSet;
import java.util.List;
import java.util.Locale;

public class VocabularyAdapter extends ArrayAdapter<Vocabulary> {
    private Context context;
    private List<Vocabulary> vocabularies;

    public VocabularyAdapter(Context context, List<Vocabulary> vocabularies) {
        super(context, 0, vocabularies);
        this.context = context;
        this.vocabularies = vocabularies;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_vocabulary, parent, false);
        }

        TextView tv_word = convertView.findViewById(R.id.tv_word);
        TextView tv_definition = convertView.findViewById(R.id.tv_definition);
        Button btn_edit = convertView.findViewById(R.id.btn_edit);

        Vocabulary vocabulary = vocabularies.get(position);
        tv_word.setText(vocabulary.getWord());
        tv_definition.setText(vocabulary.getDefinition());

        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditVocabulary.class);
                intent.putExtra("word", vocabulary.getWord());
                getContext().startActivity(intent);
            }
        });

        return convertView;
    }
}
