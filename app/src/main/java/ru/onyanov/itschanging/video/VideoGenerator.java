package ru.onyanov.itschanging.video;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.FFmpegExecuteResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.util.Locale;

import ru.onyanov.itschanging.DataManager;
import ru.onyanov.itschanging.utils.BitmapUtil;
import ru.onyanov.itschanging.utils.StorageUtil;

public class VideoGenerator {

    private static final String TAG = "VideoGenerator";

    private static final int MAX_IMAGE_SIDE = 500;

    private final Context context;

    private final VideoCompilationListener listener;

    private final String[] files;

    private String inputDirectory;

    private String tempDirectory;

    private String output;

    public VideoGenerator(int projectId, Context context, VideoCompilationListener listener) {
        this.context = context;
        this.listener = listener;

        tempDirectory = StorageUtil.getTempDirectoryPath(projectId);
        inputDirectory = StorageUtil.getImageDirectoryPath(projectId);
        output = StorageUtil.getVideoPath(projectId);
        files = DataManager.getPhotoNames(projectId);
    }

    /**
     * Runs video file encoding
     * TODO in another thread!!!
     */
    public void generate() {
        createTempImages();
        installFFmpeg();
        encode();
    }


    /**
     * Copy images to temp directory with some rules:
     * TODO all images should be same width and height
     * TODO images should not be too big to prevent OutOfMemoryException on weak devices
     * TODO don't recreate files if they already exist
     * TODO check files for availability and readability
     * TODO check filesystem for writing and free space
     */
    private void createTempImages() {
        for (String fileName : files) {
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeFile(inputDirectory + "/" + fileName, bmOptions);
            Bitmap scaledBitmap = BitmapUtil.scaleBitmapDown(bitmap, MAX_IMAGE_SIDE);
            File file = new File(tempDirectory + fileName);
            BitmapUtil.writeBitmapToFile(scaledBitmap, file);
        }
    }

    private void encode() {
        try {
            FFmpeg.getInstance(context).execute(constructCommand(), new FFmpegExecuteResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onProgress(String message) {
                    //TODO use message for display progress
                }

                @Override
                public void onFailure(String message) {
                    Log.d(TAG, "onFailure: " + message);
                    listener.onFailure();
                }

                @Override
                public void onSuccess(String message) {
                    listener.onVideoReady(output);
                }

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            Log.e(TAG, "encode: error", e);
            listener.onFailure();
        }
    }


    private void installFFmpeg() {
        FFmpeg ffmpeg = FFmpeg.getInstance(context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {}

                @Override
                public void onFailure() {
                    listener.onFailure();
                }

                @Override
                public void onSuccess() {}

                @Override
                public void onFinish() {}
            });
        } catch (FFmpegNotSupportedException e) {
            e.printStackTrace();
            listener.onFailure();
        }
    }


    /**
     * Cross-fade transition
     * see http://superuser.com/a/834035
     */
    private String[] constructCommand() {
        String patternFilter = "[%1$d:v][%2$d:v]blend=all_expr='A*(if(gte(T,0.5),1,T/0.5))+B*(1-(if(gte(T,0.5),1,T/0.5)))'[b%3$dv];";
        int sum = files.length * 2 - 1;
        int n;

        StringBuilder filters = new StringBuilder("-filter_complex ");
        StringBuilder concat = new StringBuilder("[0:v]");
        StringBuilder sb = new StringBuilder("-y ");

        for (int i = 0; i < files.length; i++) {
            n = i + 1;
            sb.append(String.format("-loop 1 -t 1 -i %1$s%2$s ", tempDirectory, files[i]));
            if (n < files.length) {
                filters.append(String.format(patternFilter, n, i, n));
                concat.append(String.format(Locale.US, "[b%1$dv][%2$d:v]", n, n));
            }
        }
        sb.append(filters);
        sb.append(concat);
        sb.append("concat=n=");
        sb.append(sum);
        sb.append(":v=1:a=0,format=yuv420p[v] -map [v] ");
        sb.append(output);

        return sb.toString().split(" ");
    }

    /**
     * Вызов ffmpeg для смены кадров без анимации
     */
    //private String singleCommand = "-y -framerate 1/2 -f image2 -i " + tempDirectory + "/" + StorageUtil.IMAGE_NAME_FORMAT + " -pix_fmt yuv420p -r 30 -c:v libx264 " + output;

}
