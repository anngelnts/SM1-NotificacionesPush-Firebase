package com.desarrollo.notificacionespush.models;

public class News {

    private int id;
    private String title;
    private String summary;
    private String urlImage;

    public News(int id, String title, String summary, String urlImage) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.urlImage = urlImage;
    }

    public News() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUrlImage() {
        return urlImage;
    }

    public void setUrlImage(String urlImage) {
        this.urlImage = urlImage;
    }
}
