package com.example.myapplication.model;

public class User {


    private String name;
    private String password;
    private String level;
    private String avatar;

    public User(String name, String password, String level, String avatar) {
        this.name = name;
        this.password = password;
        this.level = level;
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}
