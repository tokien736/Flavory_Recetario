package com.example.prueba;

import java.io.Serializable;
import java.util.List;

public class Recipe implements Serializable {
    private String id;
    private String title;
    private String description;
    private String thumbnail;
    private String category;
    private String details;
    private List<String> gallery;

    // Constructor vacío necesario para Firebase
    public Recipe() {
    }

    public Recipe(String id, String title, String description, String thumbnail, String details, List<String> gallery, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.thumbnail = thumbnail;
        this.details = details;
        this.gallery = gallery;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public List<String> getGallery() {
        return gallery;
    }

    public void setGallery(List<String> gallery) {
        this.gallery = gallery;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
