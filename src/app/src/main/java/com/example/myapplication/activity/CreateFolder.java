package com.example.myapplication.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.myapplication.R;
import com.example.myapplication.model.Folder;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class CreateFolder extends AppCompatActivity {
    private EditText et_folderName;
    private Button btn_create;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_folder);

        et_folderName = findViewById(R.id.et_folderName);
        btn_create = findViewById(R.id.btn_create);

        db = FirebaseFirestore.getInstance();
        CollectionReference folderRef = db.collection("folders");

        btn_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String folderName = et_folderName.getText().toString();
                if (TextUtils.isEmpty(folderName)) {
                    Toast.makeText(CreateFolder.this, "Bạn chưa nhập tên thư mục", Toast.LENGTH_SHORT).show();
                    return;
                }

                String folderId = folderRef.document().getId();
                Folder folder = new Folder(folderName);
                folderRef.document(folderId).set(folder);
                Intent intent = new Intent(CreateFolder.this, FolderMananager.class);
                startActivity(intent);
            }
        });
    }
}