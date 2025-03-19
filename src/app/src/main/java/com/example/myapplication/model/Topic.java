package com.example.myapplication.model;

public class Topic {
    private String name;
    private String wordCount;
    private String folderId;
    private String displayMode;

    public Topic(String name, String wordCount) {
        this.name = name;
        this.wordCount = wordCount;
    }

    public Topic(String name, String wordCount, String folderId, String displayMode) {
        this.name = name;
        this.wordCount = wordCount;
        this.folderId = folderId;
        this.displayMode = displayMode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWordCount() {
        return wordCount;
    }

    public void setWordCount(String wordCount) {
        this.wordCount = wordCount;
    }

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(String displayMode) {
        this.displayMode = displayMode;
    }
}
