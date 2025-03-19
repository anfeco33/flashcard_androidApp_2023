package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.R;
import com.example.myapplication.activity.EditTopic;
import com.example.myapplication.model.Folder;
import com.example.myapplication.model.Topic;

import java.util.List;

public class FolderAdapter extends ArrayAdapter<Folder> {
    private Context context;
    private List<Folder> folders;

    public FolderAdapter(Context context, List<Folder> folders) {
        super(context, 0, folders);
        this.context = context;
        this.folders = folders;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false);
        }

        TextView tv_folderName = convertView.findViewById(R.id.tv_folderName);
        Button btn_edit = convertView.findViewById(R.id.btn_edit);

        Folder folder = folders.get(position);
        tv_folderName.setText(folder.getName());
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditTopic.class);
                intent.putExtra("topicName", folder.getName());
                getContext().startActivity(intent);
            }
        });

        return convertView;
    }
}
