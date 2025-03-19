package com.example.myapplication.model;

import java.io.Serializable;

public class Vocabulary implements Serializable {
    private String word;
    private String definition;
    private String topicId;

    public Vocabulary() {
    }

    public Vocabulary(String word, String definition) {
        this.word = word;
        this.definition = definition;
    }

    public Vocabulary(String word, String definition, String topicId) {
        this.word = word;
        this.definition = definition;
        this.topicId = topicId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getTopicId() {
        return topicId;
    }

    public void setTopicId(String topicId) {
        this.topicId = topicId;
    }
}
