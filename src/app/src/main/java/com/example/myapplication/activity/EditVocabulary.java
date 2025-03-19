package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.adapter.TopicAdapter;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class EditVocabulary extends AppCompatActivity {
    private EditText et_word, et_definition;
    private Button btn_save;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_vocabulary);

        et_word = findViewById(R.id.et_word);
        et_definition = findViewById(R.id.et_definition);
        btn_save = findViewById(R.id.btn_save);

        db = FirebaseFirestore.getInstance();
        CollectionReference wordRef = db.collection("words");

        String word = getIntent().getStringExtra("word");

        wordRef.whereEqualTo("word", word).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    String word = document.getString("word");
                    String definition = document.getString("definition");
                    et_word.setText(word);
                    et_definition.setText(definition);
                }
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                wordRef.whereEqualTo("word", word).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String wordId = document.getId();
                            wordRef.document(wordId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    String word = et_word.getText().toString();
                                    String definition = et_definition.getText().toString();
                                    wordRef.document(wordId).update("word", word);
                                    wordRef.document(wordId).update("definition", definition);
                                    Toast.makeText(EditVocabulary.this, "Lưu thành công", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(EditVocabulary.this, VocabularyManagement.class);
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}