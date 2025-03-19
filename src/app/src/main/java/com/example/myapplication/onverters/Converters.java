package com.example.myapplication.onverters;

import androidx.room.TypeConverter;

import com.example.myapplication.entinies.VocabularyEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

public class Converters {
    @TypeConverter
    public static List<VocabularyEntity> fromString(String value) {
        Type listType = new TypeToken<List<VocabularyEntity>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromList(List<VocabularyEntity> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
