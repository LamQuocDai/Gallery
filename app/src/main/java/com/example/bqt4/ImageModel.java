package com.example.bqt4;
import java.io.Serializable;

public class ImageModel implements Serializable {
    private long id;
    private String path;
    private String name;

    public ImageModel(long id, String path, String name) {
        this.id = id;
        this.path = path;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}