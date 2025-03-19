package com.example.myapplication.repositories;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.example.myapplication.activity.AppDatabase;
import com.example.myapplication.Dao.CourseDao;
import com.example.myapplication.entinies.CourseEntity;

import java.util.List;

public class CourseRepository {
    private CourseDao courseDao;
    private LiveData<List<CourseEntity>> allCourses;

    public CourseRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        courseDao = database.courseDao();
        allCourses = courseDao.getAllCoursesLiveData();
    }

    public LiveData<List<CourseEntity>> getAllCourses() {
        return allCourses;
    }

    public void insertCourse(CourseEntity courseEntity) {
        // Thực hiện việc thêm khóa học vào cơ sở dữ liệu
        new InsertCourseAsyncTask(courseDao).execute(courseEntity);
    }

    private static class InsertCourseAsyncTask extends AsyncTask<CourseEntity, Void, Void> {
        private CourseDao courseDao;

        private InsertCourseAsyncTask(CourseDao courseDao) {
            this.courseDao = courseDao;
        }

        @Override
        protected Void doInBackground(CourseEntity... courseEntities) {
            courseDao.insertCourse(courseEntities[0]);
            return null;
        }
    }
}
