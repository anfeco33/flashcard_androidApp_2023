package com.example.myapplication.viewModel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.myapplication.entinies.CourseEntity;
import com.example.myapplication.repositories.CourseRepository;

import java.util.List;

public class AppDatabase extends AndroidViewModel {
    private CourseRepository repository;
    private LiveData<List<CourseEntity>> allCourses;

    public AppDatabase(Application application) {
        super(application);
        repository = new CourseRepository(application);
        allCourses = repository.getAllCourses();
    }

    public LiveData<List<CourseEntity>> getAllCourses() {
        return allCourses;
    }

    public void insertCourse(CourseEntity courseEntity) {
        repository.insertCourse(courseEntity);
    }
}
