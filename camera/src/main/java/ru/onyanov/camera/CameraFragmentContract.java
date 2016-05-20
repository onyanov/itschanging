package ru.onyanov.camera;

/**
 * Created by onaynov.dn on 20.05.2016.
 */
public interface CameraFragmentContract {
    void setController(CameraController ctrl);

    void setMirrorPreview(boolean isMirror);

    void performCameraAction();
}
