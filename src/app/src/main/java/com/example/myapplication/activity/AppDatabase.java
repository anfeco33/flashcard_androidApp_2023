package com.example.myapplication.activity;


import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.myapplication.Dao.CourseDao;
import com.example.myapplication.Dao.VocabularyDao;
import com.example.myapplication.entinies.CourseEntity;
import com.example.myapplication.entinies.VocabularyEntity;
import com.example.myapplication.onverters.Converters;

@Database(entities = {VocabularyEntity.class, CourseEntity.class}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;
    public abstract VocabularyDao vocabularyDao();
    public abstract CourseDao courseDao();
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "app_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}