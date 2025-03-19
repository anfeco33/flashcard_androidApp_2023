package com.example.myapplication.activity;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.myapplication.adapter.PrivateStudyListAdapter;
import com.opencsv.CSVReader;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.adapter.VocabularyAdapter;
import com.example.myapplication.model.Topic;
import com.example.myapplication.model.Vocabulary;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.opencsv.exceptions.CsvValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class privateStudyList extends AppCompatActivity {
    private ListView lv_vocabulary;
    private List<Vocabulary> vocabularies;
    private PrivateStudyListAdapter privateStudyListAdapter;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vocabulary_management);

        Toolbar toolbar_privateStudyList = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar_privateStudyList);
        lv_vocabulary = findViewById(R.id.lv_vocabulary);
        vocabularies = new ArrayList<>();
        privateStudyListAdapter = new PrivateStudyListAdapter(this, vocabularies);
        lv_vocabulary.setAdapter(privateStudyListAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("privateStudyList");
        collectionRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    String word = (String) document.getData().get("word");
                    String definition = (String) document.getData().get("definition");
                    Vocabulary vocabulary = new Vocabulary(word, definition);
                    vocabularies.add(vocabulary);
                }
                privateStudyListAdapter.notifyDataSetChanged();
            }
        });

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TextToSpeech", "Language not supported");
                    }
                } else {
                    Log.e("TextToSpeech", "Initialization failed");
                }
            }
        });
        lv_vocabulary.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Vocabulary selectedVocabulary = privateStudyListAdapter.getItem(i);
                if (selectedVocabulary != null) {
                    String word = selectedVocabulary.getWord();
                    playPronunciation(word);
                }
            }
        });

        lv_vocabulary.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(privateStudyList.this);
                builder.setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa từ vựng này không?")
                        .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Vocabulary selectedVocabulary = privateStudyListAdapter.getItem(i);
                                if (selectedVocabulary != null) {
                                    String word = selectedVocabulary.getWord();
                                    collectionRef.whereEqualTo("word", word).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot querySnapshot) {
                                            for (QueryDocumentSnapshot document : querySnapshot) {
                                                String wordId = document.getId();
                                                collectionRef.document(wordId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        Toast.makeText(privateStudyList.this, "Xóa từ vựng thành công", Toast.LENGTH_SHORT).show();
                                                        vocabularies.remove(selectedVocabulary);
                                                        privateStudyListAdapter.notifyDataSetChanged();
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        }).setNegativeButton("Hủy", null).show();
                return true;
            }
        });
    }

    private void playPronunciation(String word) {
        textToSpeech.speak(word, TextToSpeech.QUEUE_FLUSH, null);
    }

}
