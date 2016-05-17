package ru.onyanov.itschanging.utils;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.Locale;
import java.util.Random;

/**
 * Provides static methods for using file system
 */
public class StorageUtil {
    private static final String TAG = "StorageUtil";

    public static final String IMAGE_NAME_FORMAT = "image_%03d.jpg";

    private static final String APP_VIDEO_FOLDER = "its_changing";

    //TODO deleteProject this
    public static String getImageName(int i) {
        return String.format(Locale.US, IMAGE_NAME_FORMAT, i);
    }


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
}
