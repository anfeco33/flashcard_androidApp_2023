package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.model.Topic;
import com.example.myapplication.model.Vocabulary;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateTopic extends AppCompatActivity {
    private EditText et_topicName;
    private Button btn_create;
    private Spinner spn_displayMode;

    private FirebaseFirestore db;
    private CollectionReference topicsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_topic);

        et_topicName = findViewById(R.id.et_topicName);
        btn_create = findViewById(R.id.btn_create);
        spn_displayMode = findViewById(R.id.spn_displayMode);
        List<String> items = new ArrayList<>();
        items.add("Private");
        items.add("Public");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_displayMode.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        topicsRef = db.collection("topics");

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String topicName = et_topicName.getText().toString();
                String displayMode = spn_displayMode.getSelectedItem().toString();

                if (TextUtils.isEmpty(topicName)) {
                    Toast.makeText(CreateTopic.this, "Bạn chưa nhập tên chủ đề", Toast.LENGTH_SHORT).show();
                    return;
                }

                String topicId = topicsRef.document().getId();
                Topic topic = new Topic(topicName, "0", "", displayMode);
                topicsRef.document(topicId).set(topic);
                Intent intent = new Intent(CreateTopic.this, AddWord.class);
                intent.putExtra("topicId", topicId);
                startActivity(intent);
            }
        });
    }

}
