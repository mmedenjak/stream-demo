package com.ingemark.stream.model.db;


import javax.persistence.*;

@Entity
@Table
public class Config {
    @Id
    @GeneratedValue(generator = "optimized-sequence")
    @Column
    public long id;
    @Column(nullable = false)
    public String key, value;

    public Config() {
    }
}
