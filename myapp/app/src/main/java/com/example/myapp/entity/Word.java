package com.example.myapp.entity;

public class Word {
    private Integer id;
    private Integer frequency;
    private String word;
    private String definition;
    private String variant;
    private String topic;
    private Integer remembered;

    public Word() {
    }

    public Word(int id, int frequency, String word, String definition, String variant, String topic, int remembered) {
        this.id = id;
        this.frequency = frequency;
        this.word = word;
        this.definition = definition;
        this.variant = variant;
        this.topic = topic;
        this.remembered = remembered;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
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

    public String getVariant() {
        return variant;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getRemembered() {
        return remembered;
    }

    public void setRemembered(int remembered) {
        this.remembered = remembered;
    }
} 