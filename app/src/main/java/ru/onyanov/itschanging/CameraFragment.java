/***
 * Copyright (c) 2015-2016 CommonsWare, LLC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.onyanov.itschanging;

import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.LinkedList;

import de.greenrobot.event.EventBus;
import ru.onyanov.camera.CameraController;
import ru.onyanov.camera.CameraEngine;
import ru.onyanov.camera.CameraFragmentContract;
import ru.onyanov.camera.CameraView;
import ru.onyanov.camera.PictureTransaction;
import ru.onyanov.camera.ZoomStyle;

/**
 * Fragment for displaying a camera preview, with hooks to allow
 * you (or the user) to take a picture.
 */
public class CameraFragment extends Fragment implements CameraFragmentContract {
    private static final String TAG = "CameraFragment";
    private static final String ARG_OUTPUT = "output";
    private static final String ARG_UPDATE_MEDIA_STORE = "updateMediaStore";
    //private static final String ARG_SIZE_LIMIT = "sizeLimit";
    private static final String ARG_ZOOM_STYLE = "zoomStyle";
    private static final String ARG_MASK = "previewMask";
    //private static final int PINCH_ZOOM_DELTA = 20;
    private CameraController ctlr;
    private ViewGroup previewStack;
    private FloatingActionButton fabPicture;
    private View progress;
    //private boolean mirrorPreview = false;
    //private ScaleGestureDetector scaleDetector;
    //private boolean inSmoothPinchZoom = false;

    public static CameraFragment newPictureInstance(Uri output,
                                                    boolean updateMediaStore,
                                                    ZoomStyle zoomStyle,
                                                    String mask) {
        CameraFragment f = new CameraFragment();
        Bundle args = new Bundle();

        args.putParcelable(ARG_OUTPUT, output);
        args.putBoolean(ARG_UPDATE_MEDIA_STORE, updateMediaStore);
        args.putString(ARG_MASK, mask);
        args.putSerializable(ARG_ZOOM_STYLE, zoomStyle);
        f.setArguments(args);

        return (f);
    }

    /**
     * Standard fragment entry point.
     *
     * @param savedInstanceState State of a previous instance
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
        //scaleDetector = new ScaleGestureDetector(getActivity(), scaleListener);
    }

    /**
     * Standard lifecycle method, passed along to the CameraController.
     */
    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);

        if (ctlr != null) {
            ctlr.start();
        }
    }

    @Override
    public void onHiddenChanged(boolean isHidden) {
        super.onHiddenChanged(isHidden);

        if (!isHidden) {
            ActionBar ab = getActivity().getActionBar();

            if (ab != null) {
                /*
                ab.setBackgroundDrawable(getActivity()
                        .getResources()
                        .getDrawable(ru.onyanov.camera.R.drawable.cwac_cam2_action_bar_bg_transparent));
                ab.setTitle("");
*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ab.setDisplayHomeAsUpEnabled(false);
                } else {
                    ab.setDisplayShowHomeEnabled(false);
                    ab.setHomeButtonEnabled(false);
                }
            }

            if (fabPicture != null) {
                fabPicture.setEnabled(true);
                //fabSwitch.setEnabled(true);
            }
        }
    }

    /**
     * Standard lifecycle method, for when the fragment moves into
     * the stopped state. Passed along to the CameraController.
     */
    @Override
    public void onStop() {
        if (ctlr != null) {
            ctlr.stop();
        }

        EventBus.getDefault().unregister(this);

        super.onStop();
    }

    /**
     * Standard lifecycle method, for when the fragment is utterly,
     * ruthlessly destroyed. Passed along to the CameraController,
     * because why should the fragment have all the fun?
     */
    @Override
    public void onDestroy() {
        if (ctlr != null) {
            ctlr.destroy();
        }

        super.onDestroy();
    }

    /**
     * Standard callback method to create the UI managed by
     * this fragment.
     *
     * @param inflater Used to inflate layouts
     * @param container Parent of the fragment's UI (eventually)
     * @param savedInstanceState State of a previous instance
     * @return the UI being managed by this fragment
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.cwac_cam2_fragment, container, false);

        String maskPath = getArguments().getString(ARG_MASK);
        if (maskPath != null) {
            final ImageView maskView = (ImageView) v.findViewById(R.id.cwac_cam2_mask);
            final File maskFileFinal = new File(maskPath);


            Picasso.with(getActivity())
                    .load(maskFileFinal)
                    .fit()
                    .centerCrop()
                    .into(maskView);


            //TODO replace with Picasso
            /*
            maskView.post(new Runnable() {
                @Override
                public void run() {
                    int width = maskView.getWidth();
                    int height = maskView.getHeight();

                    Log.d(TAG, "run: mask with " + width + " x " + height);

                    Picasso.with(getActivity())
                            .load(maskFileFinal)
                            .resize(width, height)
                            .centerCrop()
                            .into(maskView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    Log.d(TAG, "onSuccess: " + maskFileFinal);
                                }

                                @Override
                                public void onError() {
                                    Log.d(TAG, "onError: " + maskFileFinal);
                                }
                            });
                }
            });
            */
            //setImageFromFile(maskView, maskFile);
        }

        previewStack = (ViewGroup) v.findViewById(R.id.cwac_cam2_preview_stack);

        progress = v.findViewById(R.id.cwac_cam2_progress);
        fabPicture = (FloatingActionButton) v.findViewById(R.id.cwac_cam2_picture);


        fabPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performCameraAction();
            }
        });

        /*
        fabSwitch = (FloatingActionButton) v.findViewById(R.id.cwac_cam2_switch_camera);
        fabSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.setVisibility(View.VISIBLE);
                fabSwitch.setEnabled(false);
                ctlr.switchCamera();
            }
        });
        */

        //changeMenuIconAnimation((FloatingActionMenu) v.findViewById(R.id.cwac_cam2_settings));

        onHiddenChanged(false); // hack, since this does not get
        // called on initial display

        fabPicture.setEnabled(false);
        //fabSwitch.setEnabled(false);

        if (ctlr != null && ctlr.getNumberOfCameras() > 0) {
            prepController();
        }

        return (v);
    }

    private void setImageFromFile(ImageView imageView, File file) {
        String photoPath = file.getAbsolutePath();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);

        imageView.setImageBitmap(bitmap);
    }

    /**
     * @return the CameraController this fragment delegates to
     */
    @SuppressWarnings("unused")
    public CameraController getController() {
        return (ctlr);
    }

    /**
     * Establishes the controller that this fragment delegates to
     *
     * @param ctlr the controller that this fragment delegates to
     */
    @Override
    public void setController(CameraController ctlr) {
        this.ctlr = ctlr;
    }


    /**
     * Indicates if we should mirror the preview or not. Defaults
     * to false.
     *
     * @param mirror true if we should horizontally mirror the
     *               preview, false otherwise
     */
    @Override
    public void setMirrorPreview(boolean mirror) {
        //Do nothing at this version
        //this.mirrorPreview = mirror;
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(CameraController.ControllerReadyEvent event) {
        if (event.isEventForController(ctlr)) {
            prepController();
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CameraEngine.OpenedEvent event) {

        if (event.exception == null) {
            progress.setVisibility(View.GONE);
            //fabSwitch.setEnabled(true);
            fabPicture.setEnabled(true);
            //zoomSlider = (SeekBar) getView().findViewById(R.id.cwac_cam2_zoom);

            /*
            if (ctlr.supportsZoom()) {
                if (getZoomStyle() == ZoomStyle.PINCH) {
                    previewStack.setOnTouchListener(
                            new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    return (scaleDetector.onTouchEvent(event));
                                }
                            });
                } else if (getZoomStyle() == ZoomStyle.SEEKBAR) {
                    zoomSlider.setVisibility(View.VISIBLE);
                    zoomSlider.setOnSeekBarChangeListener(seekListener);
                }
            } else {
                previewStack.setOnTouchListener(null);
                zoomSlider.setVisibility(View.GONE);
            }
            */
        } else {
            getActivity().finish();
        }
    }

    /*
    public void onEventMainThread(CameraEngine.SmoothZoomCompletedEvent event) {
        inSmoothPinchZoom = false;
        zoomSlider.setEnabled(true);
    }*/

    @Override
    public void performCameraAction() {
        takePicture();
    }

    private void takePicture() {
        Uri output = getArguments().getParcelable(ARG_OUTPUT);

        PictureTransaction.Builder b = new PictureTransaction.Builder();

        if (output != null) {
            b.toUri(getActivity(), output,
                    getArguments().getBoolean(ARG_UPDATE_MEDIA_STORE, false));
        }

        fabPicture.setEnabled(false);
        //fabSwitch.setEnabled(false);
        ctlr.takePicture(b.build());
    }

    private void prepController() {
        LinkedList<CameraView> cameraViews = new LinkedList<>();
        CameraView cv = (CameraView) previewStack.getChildAt(0);

        //cv.setMirror(mirrorPreview);
        cameraViews.add(cv);

        for (int i = 1; i < ctlr.getNumberOfCameras(); i++) {
            cv = new CameraView(getActivity());
            cv.setVisibility(View.INVISIBLE);
            //cv.setMirror(mirrorPreview);
            previewStack.addView(cv);
            cameraViews.add(cv);
        }

        ctlr.setCameraViews(cameraViews);
    }

    /*
    // based on https://goo.gl/3IUM8K

    private void changeMenuIconAnimation(final FloatingActionMenu menu) {
        AnimatorSet set = new AnimatorSet();
        final ImageView v = menu.getMenuIconView();
        ObjectAnimator scaleOutX = ObjectAnimator.ofFloat(v, "scaleX", 1.0f, 0.2f);
        ObjectAnimator scaleOutY = ObjectAnimator.ofFloat(v, "scaleY", 1.0f, 0.2f);
        ObjectAnimator scaleInX = ObjectAnimator.ofFloat(v, "scaleX", 0.2f, 1.0f);
        ObjectAnimator scaleInY = ObjectAnimator.ofFloat(v, "scaleY", 0.2f, 1.0f);

        scaleOutX.setDuration(50);
        scaleOutY.setDuration(50);

        scaleInX.setDuration(150);
        scaleInY.setDuration(150);
        scaleInX.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                v.setImageResource(menu.isOpened()
                        ? R.drawable.cwac_cam2_ic_action_settings
                        : R.drawable.cwac_cam2_ic_close);
                // yes, that seems backwards, but it works
                // presumably, opened state not yet toggled
            }
        });

        set.play(scaleOutX).with(scaleOutY);
        set.play(scaleInX).with(scaleInY).after(scaleOutX);
        set.setInterpolator(new OvershootInterpolator(2));
        menu.setIconToggleAnimatorSet(set);
    }
*/

    /*
    private ZoomStyle getZoomStyle() {
        ZoomStyle result = (ZoomStyle) getArguments().getSerializable(ARG_ZOOM_STYLE);

        if (result == null) {
            result = ZoomStyle.NONE;
        }

        return (result);
    }
    */

    /*
    private ScaleGestureDetector.OnScaleGestureListener scaleListener =
            new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public void onScaleEnd(ScaleGestureDetector detector) {
                    float scale = detector.getScaleFactor();
                    int delta;

                    if (scale > 1.0f) {
                        delta = PINCH_ZOOM_DELTA;
                    } else if (scale < 1.0f) {
                        delta = -1 * PINCH_ZOOM_DELTA;
                    } else {
                        return;
                    }

                    if (!inSmoothPinchZoom) {
                        if (ctlr.changeZoom(delta)) {
                            inSmoothPinchZoom = true;
                        }
                    }
                }
            };
*/

    /*
    private SeekBar.OnSeekBarChangeListener seekListener =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar,
                                              int progress,
                                              boolean fromUser) {
                    if (fromUser) {
                        if (ctlr.setZoom(progress)) {
                            seekBar.setEnabled(false);
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // no-op
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // no-op
                }
            };
            */
}