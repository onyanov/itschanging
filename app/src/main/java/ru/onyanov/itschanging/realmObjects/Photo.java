package ru.onyanov.itschanging.realmObjects;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Photo extends RealmObject {

    @PrimaryKey
    private int id;

    private String file;

    private RealmProject project;


    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public RealmProject getProject() {
        return project;
    }

    public void setProject(RealmProject project) {
        this.project = project;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
