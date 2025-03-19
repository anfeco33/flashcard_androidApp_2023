package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.model.Vocabulary;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class AddWord extends AppCompatActivity {
    private EditText et_Word, et_definition;
    private Button btn_add;
    private FirebaseFirestore db;
    private CollectionReference wordRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_word);

        et_Word = findViewById(R.id.et_word);
        et_definition = findViewById(R.id.et_definition);
        btn_add = findViewById(R.id.btn_add);
        String topicId = getIntent().getStringExtra("topicId");

        db = FirebaseFirestore.getInstance();
        wordRef = db.collection("words");

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String word = et_Word.getText().toString();
                String definition = et_definition.getText().toString();

                if (TextUtils.isEmpty(word)) {
                    Toast.makeText(AddWord.this, "Vui lòng nhập từ vựng", Toast.LENGTH_SHORT).show();
                    return;
                } else if (TextUtils.isEmpty(definition)) {
                    Toast.makeText(AddWord.this, "Vui lòng nhập nghĩa của từ", Toast.LENGTH_SHORT).show();
                    return;
                }

                Vocabulary vocabulary = new Vocabulary(word, definition, topicId);
                wordRef.add(vocabulary).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        db.collection("topics").document(topicId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                int wordCount = Integer.valueOf((String) documentSnapshot.get("wordCount"));
                                wordCount += 1;
                                db.collection("topics").document(topicId).update("wordCount", String.valueOf(wordCount));
                            }
                        });
                        Toast.makeText(AddWord.this,"Thêm chủ đề thành công", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(AddWord.this, VocabularyManagement.class);
                        intent.putExtra("topicId", topicId);
                        startActivity(intent);
                    }
                });
            }
        });
    }
}