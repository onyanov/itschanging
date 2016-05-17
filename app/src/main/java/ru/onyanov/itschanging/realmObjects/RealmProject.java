package ru.onyanov.itschanging.realmObjects;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RealmProject extends RealmObject {

    private String title;

    private boolean active;

    /**
     * The type of the project. One of: regular|simple|auto
     */
    private int type;

    /**
     * The type of mask displaying. One of: alpha|invert-extra|extra|outline.
     * TODO realise these different types usage.
     */
    private int maskType;

    @PrimaryKey
    private int id;


    public RealmProject() {
        super();
    }

    public RealmProject(String title, int type) {
        super();
        this.title = title;
        this.type = type;
        active = true;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getMaskType() {
        return maskType;
    }

    public void setMaskType(int maskType) {
        this.maskType = maskType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
