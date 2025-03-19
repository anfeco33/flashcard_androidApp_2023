package com.example.myapplication.Dao;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.myapplication.entinies.CourseEntity;

import java.util.List;

@Dao
public interface CourseDao {
    @Insert
    void insertCourse(CourseEntity course);

    @Query("SELECT * FROM courses")
    List<CourseEntity> getAllCourses();
    @Query("SELECT * FROM courses")
    LiveData<List<CourseEntity>> getAllCoursesLiveData();
}
