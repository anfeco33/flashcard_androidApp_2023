package com.example.myapplication.entinies;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;
import java.util.List;
@Entity(tableName = "courses")
public class CourseEntity implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String tittle;
    private String description;

    protected CourseEntity(Parcel in) {
        id = in.readInt();
        tittle = in.readString();
        description = in.readString();
        vocabularies = in.readArrayList(VocabularyEntity.class.getClassLoader());
    }

    public static final Creator<CourseEntity> CREATOR = new Creator<CourseEntity>() {
        @Override
        public CourseEntity createFromParcel(Parcel in) {
            return new CourseEntity(in);
        }

        @Override
        public CourseEntity[] newArray(int size) {
            return new CourseEntity[size];
        }
    };

    public List<VocabularyEntity> getVocabularies() {
        return vocabularies;
    }

    public void setVocabularies(List<VocabularyEntity> vocabularies) {
        this.vocabularies = vocabularies;
    }

    private List<VocabularyEntity> vocabularies;
    private Date created;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CourseEntity(){}


    public String getTittle() {
        return tittle;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public void setTittle(String tittle) {
        this.tittle = tittle;
    }

    public List<VocabularyEntity> getCards() {
        return vocabularies;
    }

    public void setCards(List<VocabularyEntity> vocabularies) {
        this.vocabularies = vocabularies;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(tittle);
        dest.writeString(description);
        dest.writeList(vocabularies);
    }
}
