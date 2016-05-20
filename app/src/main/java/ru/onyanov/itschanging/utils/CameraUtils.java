package ru.onyanov.itschanging.utils;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import ru.onyanov.itschanging.CameraActivity;

/**
 * Created by onaynov.dn on 20.05.2016.
 */
public class CameraUtils {

    /**
     * Tests the app and the device to confirm that the code
     * in this library should work. This is called automatically
     * by other classes (e.g., CameraActivity), and so you probably
     * do not need to call it yourself. But, hey, it's a public
     * method, so call it if you feel like it.
     *
     * The method will throw an IllegalStateException if the
     * environment is unsatisfactory, where the exception message
     * will tell you what is wrong.
     *
     * @param ctxt any Context will do
     */
    public static void validateEnvironment(Context ctxt) {
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            throw new IllegalStateException("App is running on device older than API Level 14");
        }

        PackageManager pm=ctxt.getPackageManager();

        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) &&
                !pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            throw new IllegalStateException("App is running on device that lacks a camera");
        }

        if (ctxt instanceof CameraActivity) {
            try {
                ActivityInfo info=pm.getActivityInfo(((CameraActivity)ctxt).getComponentName(), 0);

                if (info.exported) {
                    throw new IllegalStateException("A CameraActivity cannot be exported!");
                }
            }
            catch (PackageManager.NameNotFoundException e) {
                throw new IllegalStateException("Cannot find this activity!", e);
            }
        }
    }
}
