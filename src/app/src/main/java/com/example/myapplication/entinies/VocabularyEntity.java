package com.example.myapplication.entinies;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocabulary")
public class VocabularyEntity implements Parcelable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String terminology;
    private String define;

    protected VocabularyEntity(Parcel in) {
        id = in.readInt();
        terminology = in.readString();
        define = in.readString();
    }

    public VocabularyEntity(){}
    public static final Creator<VocabularyEntity> CREATOR = new Creator<VocabularyEntity>() {
        @Override
        public VocabularyEntity createFromParcel(Parcel in) {
            return new VocabularyEntity(in);
        }

        @Override
        public VocabularyEntity[] newArray(int size) {
            return new VocabularyEntity[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Card{" +
                "terminology='" + terminology + '\'' +
                ", define='" + define + '\'' +
                '}';
    }

    public String getTerminology() {
        return terminology;
    }

    public void setTerminology(String terminology) {
        this.terminology = terminology;
    }

    public String getDefine() {
        return define;
    }

    public void setDefine(String define) {
        this.define = define;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(terminology);
        dest.writeString(define);
    }
}
