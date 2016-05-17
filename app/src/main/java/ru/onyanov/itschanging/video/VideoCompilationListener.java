package ru.onyanov.itschanging.video;

/**
 * Listener for video encoding process
 */
public interface VideoCompilationListener {

    /**
     * Called when video file was successfully generated
     * @param filePath Absolute path to video file
     */
    void onVideoReady(String filePath);

    /**
     * Called when there was some error during encoding
     */
    void onFailure();

}
