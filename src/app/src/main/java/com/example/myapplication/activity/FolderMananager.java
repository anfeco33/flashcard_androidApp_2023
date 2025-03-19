package com.example.myapplication.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.adapter.FolderAdapter;
import com.example.myapplication.model.Folder;
import com.example.myapplication.model.Topic;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class FolderMananager extends AppCompatActivity {
    private Toolbar toolbar;
    private ListView lv_folder;
    private List<Folder> folders;
    private FirebaseFirestore db;
    private FolderAdapter folderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_mananager);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lv_folder = findViewById(R.id.lv_folder);
        folders = new ArrayList<>();
        folderAdapter = new FolderAdapter(this, folders);
        lv_folder.setAdapter(folderAdapter);

        db = FirebaseFirestore.getInstance();
        CollectionReference folderRef = db.collection("folders");
        folderRef.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                    String folderName = (String) document.getData().get("name");
                    Folder folder = new Folder(folderName);
                    folders.add(folder);
                }
                folderAdapter.notifyDataSetChanged();
            }
        });

        lv_folder.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Folder selectedFolder = folderAdapter.getItem(i);
                if (selectedFolder != null) {
                    String folderName = selectedFolder.getName();
                    folderRef.whereEqualTo("name", folderName).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot querySnapshot) {
                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                String folderId = document.getId();
                                Intent intent = new Intent(FolderMananager.this, TopicsOfFolder.class);
                                intent.putExtra("folderId", folderId);
                                startActivity(intent);
                            }
                        }
                    });
                }
            }
        });

        lv_folder.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FolderMananager.this);
                builder.setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc chắn muốn xóa chủ đề này không?")
                        .setPositiveButton("Xóa", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Folder selectedFolder = folderAdapter.getItem(i);
                                if (selectedFolder != null) {
                                    String name = selectedFolder.getName();
                                    folderRef.whereEqualTo("name", name).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot querySnapshot) {
                                            for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                                                String folderId = document.getId();
                                                db.collection("topics").whereEqualTo("folderId", folderId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onSuccess(QuerySnapshot querySnapshot) {
                                                        for (QueryDocumentSnapshot document : querySnapshot) {
                                                            String topicId = document.getId();
                                                            folderRef.document(folderId).delete();
                                                            db.collection("topics").whereEqualTo("folderId", folderId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                @Override
                                                                public void onSuccess(QuerySnapshot querySnapshot) {
                                                                    db.collection("topics").document(topicId).update("folderId", "").addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void unused) {
                                                                            Toast.makeText(FolderMananager.this, "Xóa chủ đề thành công", Toast.LENGTH_SHORT).show();
                                                                            folders.remove(selectedFolder);
                                                                            folderAdapter.notifyDataSetChanged();
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        }
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
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_add) {
            Intent intent = new Intent(FolderMananager.this, CreateFolder.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}