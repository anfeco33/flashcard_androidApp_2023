package com.example.myapplication.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.entinies.VocabularyEntity;

import java.util.List;

@Dao
public interface VocabularyDao {
    @Insert
    void insert(VocabularyEntity vocabularyEntity);

    @Query("SELECT * FROM vocabulary")
    List<VocabularyEntity> getAllVocabularies();
}