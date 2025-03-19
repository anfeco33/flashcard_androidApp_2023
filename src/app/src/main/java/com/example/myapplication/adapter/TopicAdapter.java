package com.example.myapplication.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.activity.EditTopic;
import com.example.myapplication.model.Topic;
import com.example.myapplication.model.Vocabulary;

import java.util.List;

public class TopicAdapter extends ArrayAdapter<Topic> {
    private Context context;
    private List<Topic> topics;

    public TopicAdapter(Context context, List<Topic> topics) {
        super(context, 0, topics);
        this.context = context;
        this.topics = topics;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_topic, parent, false);
        }

        TextView tv_topicName = convertView.findViewById(R.id.tv_topicName);
        TextView tv_wordCount = convertView.findViewById(R.id.tv_wordCount);
        Button btn_edit = convertView.findViewById(R.id.btn_edit);

        Topic topic = topics.get(position);
        tv_topicName.setText(topic.getName());
        tv_wordCount.setText(topic.getWordCount());
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), EditTopic.class);
                intent.putExtra("topicName", topic.getName());
                getContext().startActivity(intent);
            }
        });

        return convertView;
    }
}
