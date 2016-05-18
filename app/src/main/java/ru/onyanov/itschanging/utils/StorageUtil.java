package ru.onyanov.itschanging.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Random;

/**
 * Provides static methods for using file system
 */
public class StorageUtil {
    private static final String TAG = "StorageUtil";

    private static final String APP_VIDEO_FOLDER = "its_changing";

    public static final int MY_PERMISSIONS_REQUEST_STORAGE = 1;

    public static String getImageDirectoryPath(int projectId) {
        return getProjectDir(projectId, "images");
    }

    public static String getTempDirectoryPath(int projectId) {
        return getProjectDir(projectId, "tmp");
    }

    private static String getProjectDir(int projectId, String dirName) {
        File projectsDir = new File(getRootDirectory(), "projects");
        File projectDir = new File(projectsDir, getProjectName(projectId));
        File directoryPath = new File(projectDir, dirName);
        if (!directoryPath.mkdirs()) {
            Log.i(TAG, "Directory " + directoryPath.getAbsolutePath() + " already exists");
        }
        return directoryPath.getAbsolutePath() + "/";
    }

    private static File getRootDirectory() {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), APP_VIDEO_FOLDER);
        if (!file.mkdirs()) {
            Log.i(TAG, "Directory " + file.getAbsolutePath() + " already exists");
        }
        return file;
    }

    private static String getProjectName(int projectId) {
        return "project_" + projectId;
    }

    /**
     *  Checks if external storage is available for read and write
     *  TODO integrate these methods

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    // Checks if external storage is available to at least read
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
    }
    */

    public static String getVideoPath(int projectId) {
        String videoFileName = getVideoFileName(projectId);
        File file = getVideoStorageDir(APP_VIDEO_FOLDER);
        File videoFile = new File(file.getAbsolutePath() + "/" + videoFileName);
        return videoFile.getAbsolutePath();
    }

    private static String getVideoFileName(int projectId) {
        String projectName = getProjectName(projectId);
        return projectName + ".mp4";
    }

    private static File getVideoStorageDir(String albumName) {
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_MOVIES), albumName);
        if (!file.mkdirs()) {
            Log.i(TAG, "Directory " + file.getAbsolutePath() + " already exists");
        }
        return file;
    }

    public static File generateFileName(String dir) {
        char[] chars = "abcdefghijklmnopqrstuvwxyz".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        sb.append(".jpg");
        return new File(dir, sb.toString());
    }

    public static boolean checkStoragePermission(Activity activity) {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(activity, "We need filesystem access", Toast.LENGTH_SHORT).show();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST_STORAGE);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            return true;
        }
        return false;
    }
}
