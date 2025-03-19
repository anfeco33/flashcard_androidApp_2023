package com.example.myapplication.activity;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;
import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExportCSV extends AppCompatActivity {
    private Spinner spn_topic;
    private Button btn_export;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_csv);

        spn_topic = findViewById(R.id.spn_topic);
        btn_export = findViewById(R.id.btn_export);

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
                ArrayAdapter<String> adapter = new ArrayAdapter<>(ExportCSV.this, android.R.layout.simple_spinner_item, items);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spn_topic.setAdapter(adapter);
            }
        });

        btn_export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String topicName = spn_topic.getSelectedItem().toString();
                StringBuilder csvData = new StringBuilder();

                topicRef.whereEqualTo("name", topicName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        for (QueryDocumentSnapshot document : querySnapshot) {
                            String topicId = document.getId();

                            db.collection("words").whereEqualTo("topicId", topicId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                @Override
                                public void onSuccess(QuerySnapshot querySnapshot) {
                                    for (QueryDocumentSnapshot document :querySnapshot) {
                                        String word = document.getString("word");
                                        String definition = document.getString("definition");
                                        csvData.append(word).append(",").append(definition).append("\n");

                                        File file = new File(ExportCSV.this.getExternalFilesDir(null), topicName + ".csv");

                                        try {
                                            FileWriter fileWriter = new FileWriter(file);
                                            fileWriter.append(csvData.toString());
                                            fileWriter.flush();
                                            fileWriter.close();
                                            Toast.makeText(ExportCSV.this, "CSV file saved: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
