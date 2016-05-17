package ru.onyanov.itschanging.models;

/**
 * Created by danilonanov on 14.05.16.
 */
public class Project {

    int id;

    String title;

    public Project(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
}
