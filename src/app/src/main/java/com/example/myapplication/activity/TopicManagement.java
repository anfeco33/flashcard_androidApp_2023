package com.example.myapplication.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.adapter.TopicAdapter;
import com.example.myapplication.adapter.VocabularyAdapter;
import com.example.myapplication.model.Topic;
import com.example.myapplication.model.Vocabulary;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class TopicManagement extends AppCompatActivity {
    private Toolbar toolbar;
    private ListView lv_Topic;
    private TopicAdapter topicAdapter;
    private List<Topic> topics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic_management);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lv_Topic = findViewById(R.id.lv_Topic);
        topics = new ArrayList<>();
        topicAdapter = new TopicAdapter(this, topics);
        lv_Topic.setAdapter(topicAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference topicsRef = db.collection("topics");
        topicsRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    String topicName = (String) document.getData().get("name");
                    String wordCount = (String) document.getData().get("wordCount");
                    Topic topic = new Topic(topicName, wordCount);
                    topics.add(topic);
                }
                topicAdapter.notifyDataSetChanged();
            }
        });

        lv_Topic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Topic selectedTopic = topicAdapter.getItem(position);
                if (selectedTopic != null) {
                    String topicName  = selectedTopic.getName();
                    topicsRef.whereEqualTo("name", topicName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {
                            for (QueryDocumentSnapshot document : querySnapshot) {
                                String topicId = document.getId();
                                Intent intent = new Intent(TopicManagement.this, VocabularyManagement.class);
                                intent.putExtra("topicId", topicId);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        });

        lv_Topic.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(TopicManagement.this);
                builder.setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa chủ đề này không?")
                        .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Topic selectedTopic = topicAdapter.getItem(i);
                                if (selectedTopic != null) {
                                    String name = selectedTopic.getName();
                                    topicsRef.whereEqualTo("name", name).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot querySnapshot) {
                                            for (QueryDocumentSnapshot document : querySnapshot) {
                                                String topicId = document.getId();
                                                topicsRef.document(topicId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void unused) {
                                                        db.collection("words").whereEqualTo("topicId", topicId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                            @Override
                                                            public void onSuccess(QuerySnapshot querySnapshot) {
                                                                for (QueryDocumentSnapshot document : querySnapshot) {
                                                                    String wordId = document.getId();
                                                                    db.collection("words").document(wordId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {

                                                                        }
                                                                    });
                                                                }
                                                            }
                                                        });
                                                        Toast.makeText(TopicManagement.this, "Xóa chủ đề thành công", Toast.LENGTH_SHORT).show();
                                                        topics.remove(selectedTopic);
                                                        topicAdapter.notifyDataSetChanged();
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
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu_topic, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_add) {
            Intent intent = new Intent(TopicManagement.this, CreateTopic.class);
            startActivity(intent);
            return true;
        }

        if (id == R.id.menu_export) {
            Intent intent = new Intent(TopicManagement.this, ExportCSV.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}