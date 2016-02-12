package com.ingemark.stream.controller;

public class ConfigDto {
    public long id;
    public String key, value;

    public ConfigDto(long id, String key, String value) {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public ConfigDto() {
    }
}