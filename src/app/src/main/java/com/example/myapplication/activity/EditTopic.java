package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class EditTopic extends AppCompatActivity {
    private EditText et_topicName;
    private Button btn_save;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_topic);

        et_topicName = findViewById(R.id.et_topicName);
        btn_save = findViewById(R.id.btn_save);

        String topicName = getIntent().getStringExtra("topicName");

        db = FirebaseFirestore.getInstance();
        db.collection("topics").whereEqualTo("name", topicName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    String topicName = (String) document.getData().get("name");
                    et_topicName.setText(topicName);
                }
//                for (QueryDocumentSnapshot document : querySnapshot) {
//                    String topicId = document.getId();
//                }
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                db.collection("topics").whereEqualTo("name", topicName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String topicId = document.getId();
                            db.collection("topics").document(topicId).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    String name = et_topicName.getText().toString();
                                    db.collection("topics").document(topicId).update("name", name);
                                }
                            });
                            Toast.makeText(EditTopic.this,"Lưu thành công", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditTopic.this, TopicManagement.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
    }
}