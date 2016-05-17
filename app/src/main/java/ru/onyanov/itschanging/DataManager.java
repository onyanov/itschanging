package ru.onyanov.itschanging;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import ru.onyanov.itschanging.realmObjects.Photo;
import ru.onyanov.itschanging.realmObjects.RealmProject;
import ru.onyanov.itschanging.utils.StorageUtil;

public class DataManager {

    private static final String TAG = "DataManager";

    public static final int TYPE_REGULAR = 0;
    //public static final int TYPE_SIMPLE = 1;

    public static final int MAX_PROJECTS_SIZE = 3;
    public static final int MAX_PHOTO_SIZE = 24;

    public static RealmResults<RealmProject> getProjects() {
        return getRealm().where(RealmProject.class).findAllAsync();
    }

    public static void deleteProject(int projectId) {
        //TODO temp directory and project directory will stay. Finish them.
        //noinspection ResultOfMethodCallIgnored
        new File(StorageUtil.getImageDirectoryPath(projectId)).delete();

        getRealm().beginTransaction();
        RealmProject project = getProject(projectId);
        project.deleteFromRealm();
        getRealm().commitTransaction();
    }

    public static RealmProject getProject(long projectId) {
        RealmProject project = getRealm().where(RealmProject.class).equalTo("id", projectId).findFirst();
        getRealm().close();
        return project;
    }

    public static RealmProject createProject(String title) {
        getRealm().beginTransaction();
        RealmProject project = getRealm().createObject(RealmProject.class); // Create a new object
        int nextID = getRealm().where(RealmProject.class).max("id").intValue() + 1;
        project.setId(nextID);
        project.setTitle(title);
        getRealm().commitTransaction();
        return project;
    }

    public static Photo createPhoto(String file, RealmProject project) {
        getRealm().beginTransaction();
        Photo photo = getRealm().createObject(Photo.class); // Create a new object
        int nextID = getRealm().where(Photo.class).max("id").intValue() + 1;
        photo.setId(nextID);
        photo.setFile(file);
        photo.setProject(project);
        getRealm().commitTransaction();
        return photo;
    }

    public static List<Photo> getPhotos(int projectId) {
        RealmResults<Photo> result2 = getRealm().where(Photo.class)
                .equalTo("project.id", projectId)
                .findAll();

        ArrayList<Photo> arrayList = new ArrayList<>();
        for (int i = result2.size(); i > 0; i--) {
            arrayList.add(result2.get(i-1));
        }

        return arrayList;
    }

    public static Photo getFirstPhoto(int projectId) {
        return getRealm().where(Photo.class)
                .equalTo("project.id", projectId)
                .findFirst();
    }

    private static Realm getRealm() {
        return Realm.getDefaultInstance();
    }

    public static File[] getPhotoUrls(int projectId) {
        String projectDir = StorageUtil.getImageDirectoryPath(projectId);
        List<Photo> photos = getPhotos(projectId);
        File[] urls = new File[photos.size()];
        for (int i = 0; i < photos.size(); i++) {
            urls[i] = new File(projectDir, photos.get(i).getFile());
        }
        return urls;
    }



    public static void deletePhoto(int photoId) {
        getRealm().beginTransaction();
        Photo photo = getPhoto(photoId);
        // TODO delete file
        photo.deleteFromRealm();
        getRealm().commitTransaction();
    }

    private static Photo getPhoto(int photoId) {
        return getRealm().where(Photo.class)
                .equalTo("id", photoId)
                .findFirst();
    }

    public static String[] getPhotoNames(int projectId) {
        List<Photo> photos = getPhotos(projectId);
        String[] photoNames = new String[photos.size()];

        for(int i = 0; i < photos.size(); i++) {
            photoNames[i] = photos.get(i).getFile();
        }
        Log.d(TAG, "getPhotoNames: " + Arrays.toString(photoNames));
        return photoNames;
    }

}
