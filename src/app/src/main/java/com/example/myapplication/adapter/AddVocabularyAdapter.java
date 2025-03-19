package com.example.myapplication.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.entinies.VocabularyEntity;

import java.util.List;

public class AddVocabularyAdapter extends RecyclerView.Adapter<AddVocabularyAdapter.AddTopicViewHolder>{
    private List<VocabularyEntity> vocabularies;
    private OnItemLongClickListener longClickListener;
    private int selectedPosition = -1;



    public AddVocabularyAdapter(List<VocabularyEntity> dataList) {
        this.vocabularies = dataList;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }
    public void setSelectedPosition(int position) {
        this.selectedPosition = position;
    }
    @NonNull
    @Override
    public AddTopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.topic, parent, false);
        return new AddTopicViewHolder(itemView, AddVocabularyAdapter.this);
    }

    @Override
    public void onBindViewHolder(@NonNull AddTopicViewHolder holder, int position) {
        VocabularyEntity vocabularyEntity = vocabularies.get(position);
        holder.bind(vocabularyEntity);
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.LTGRAY);
        } else {
            holder.itemView.setBackgroundColor(Color.WHITE);
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int adapterPosition = holder.getAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    if (longClickListener != null) {
                        holder.itemView.setBackgroundColor(Color.LTGRAY);
                        setSelectedPosition(adapterPosition);
                        longClickListener.onItemLongClick(view, adapterPosition);
                    }
                    return true;
                }
                return false;
            }
        });
    }
    public void clearSelectedPosition() {
        int previousSelectedPosition = selectedPosition;
        selectedPosition = -1;
        if (previousSelectedPosition != -1) {
            notifyItemChanged(previousSelectedPosition);
        }
    }

    @Override
    public int getItemCount() {
        return vocabularies.size();
    }

    public class AddTopicViewHolder extends RecyclerView.ViewHolder {
        private EditText editText1 ,editText2;
        private AddVocabularyAdapter adapter;

        public AddTopicViewHolder(View itemView, AddVocabularyAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            editText1 = itemView.findViewById(R.id.terminology);
            editText2 = itemView.findViewById(R.id.define);

        }
        public void bind(VocabularyEntity item) {
            editText1.setText(item.getTerminology());
            editText2.setText(item.getDefine());
        }

    }
    public interface OnItemLongClickListener {
        void onItemLongClick(View view, int position);
    }
}
