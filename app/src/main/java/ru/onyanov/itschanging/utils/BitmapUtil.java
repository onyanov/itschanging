package ru.onyanov.itschanging.utils;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Provides static methods for manipulating with bitmaps
 */
public class BitmapUtil {

    public static Bitmap scaleBitmapDown(Bitmap bitmap, int size) throws NullPointerException {
        int realWidth = bitmap.getWidth();
        int realHeight = bitmap.getHeight();
        int newWidth, newHeight;

        if (realWidth > realHeight) {
            newWidth = size;
            newHeight = realHeight * size / realWidth;
            if (newHeight % 2 > 0) newHeight--;
        } else {
            newHeight = size;
            newWidth = realWidth * size / realHeight;
            if (newWidth % 2 > 0) newWidth--;
        }
        bitmap = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
        return bitmap;
    }

    public static void writeBitmapToFile(Bitmap bitmap, File file) {
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
