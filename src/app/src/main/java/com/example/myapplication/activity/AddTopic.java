package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddTopic extends AppCompatActivity {
    private Spinner spn_topic;
    private Button btn_add;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_topic);

        spn_topic = findViewById(R.id.spn_topic);
        btn_add = findViewById(R.id.btn_add);
        List<String> items = new ArrayList<>();

        db = FirebaseFirestore.getInstance();
        CollectionReference topicRef = db.collection("topics");

        topicRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    String topicName = document.getString("name");
                    items.add(topicName);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(AddTopic.this, android.R.layout.simple_spinner_item, items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spn_topic.setAdapter(adapter);
            }
        });

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String topicName = spn_topic.getSelectedItem().toString();

                topicRef.whereEqualTo("name", topicName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        String folderId = getIntent().getStringExtra("folderId");
                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            String topicId = document.getId();
                            topicRef.document(topicId).update("folderId", folderId).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(AddTopic.this, "Thêm chủ đề thành công", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(AddTopic.this, TopicsOfFolder.class);
                                    intent.putExtra("folderId", folderId);
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