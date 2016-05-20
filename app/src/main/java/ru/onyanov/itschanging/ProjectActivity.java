package ru.onyanov.itschanging;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;

import ru.onyanov.camera.Facing;
import ru.onyanov.camera.FocusMode;
import ru.onyanov.camera.ZoomStyle;
import ru.onyanov.itschanging.adapters.PhotoAdapter;
import ru.onyanov.itschanging.helpers.GridSpacingItemDecoration;
import ru.onyanov.itschanging.realmObjects.Photo;
import ru.onyanov.itschanging.realmObjects.RealmProject;
import ru.onyanov.itschanging.utils.StorageUtil;
import ru.onyanov.itschanging.video.VideoCompilationListener;
import ru.onyanov.itschanging.video.VideoGenerator;

public class ProjectActivity extends AppCompatActivity implements VideoCompilationListener {

    private static final int REQUEST_PORTRAIT_FFC = 1;
    public static final String PARAM_ID = "param_id";
    private static final String TAG = "ProjectActivity";
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 1;
    private File maskFile;
    private int projectId;
    private RealmProject project;
    private String projectDir;
    private RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private PhotoAdapter mAdapter;
    private String videoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            projectId = savedInstanceState.getInt(PARAM_ID);
        } else {
            projectId = getIntent().getIntExtra(PARAM_ID, 0);
        }

        setContentView(R.layout.app_bar_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }


        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);


        // use a linear layout manager
        int spanCount = getResources().getInteger(R.integer.photo_columns);
        mLayoutManager = new GridLayoutManager(this, spanCount);
        mRecyclerView.setLayoutManager(mLayoutManager);

        Log.d(TAG, "onCreate: " + spanCount);

        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.spacing);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacingInPixels, false));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(PARAM_ID, projectId);
        //TODO save destinationPath
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        projectId = savedInstanceState.getInt(PARAM_ID);
        //TODO load destinationpath
    }

    @Override
    protected void onResume() {
        super.onResume();

        project = DataManager.getProject(projectId);

        if (project != null) {
            getSupportActionBar().setTitle(project.getTitle());

            projectDir = StorageUtil.getImageDirectoryPath(projectId);
            List<Photo> photos = DataManager.getPhotos(projectId);
            Log.d(TAG, "photos size = " + photos.size());
            if (photos.size() > 0) {
                maskFile = new File(projectDir, photos.get(0).getFile());
            }

            mAdapter = new PhotoAdapter(photos, projectDir, new PhotoItemListener());
            mRecyclerView.setAdapter(mAdapter);

            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            fab.setOnClickListener(onClickListener);
            if (photos.size() >= DataManager.MAX_PHOTO_SIZE) {
                fab.hide();
            }

        } else {
            Toast.makeText(ProjectActivity.this, R.string.bad_project, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_project, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent intent = new Intent(ProjectActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.action_play) {
            runSlideShow(0);
        } else if(id == R.id.action_video) {
            tryToGenerateVideo();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ProjectFormActivity.class);
            intent.putExtra(ProjectFormActivity.FIELD_PROJECT_ID, projectId);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_delete) {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle(R.string.project_delete_confirm_title);
            adb.setMessage(R.string.project_delete_confirm_message);
            adb.setPositiveButton(R.string.action_delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        DataManager.deleteProject(projectId);
                        finish();
                    }
                }
            );
            adb.setNegativeButton(R.string.action_cancel, null);
            adb.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void runSlideShow(int position) {
        Intent intent = new Intent(this, SlideshowActivity.class);
        intent.putExtra(SlideshowActivity.FIELD_PROJECT_ID, projectId);
        intent.putExtra(SlideshowActivity.FIELD_PHOTO_ID, position);
        startActivity(intent);
    }

    private void tryToGenerateVideo() {
        if (checkStoragePermission()) {
            generateVideo();
        }
    }

    private boolean checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                Toast.makeText(this, "We need filesystem access", Toast.LENGTH_SHORT).show();

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                // permission was granted, yay! Do the
// contacts-related task you need to do.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    generateVideo();
                }
                else {
                    Log.d(TAG, "onRequestPermissionsResult: We really need filesystem access");
                    //Toast.makeText(MainActivity.this, "We really need filesystem access", Toast.LENGTH_SHORT).show();
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                break;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void generateVideo() {
        new VideoGenerator(projectId, this, this).generate();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (destinationPath == null) {
                //TODO Where's the file?!
                Toast.makeText(ProjectActivity.this, R.string.bad_shot, Toast.LENGTH_SHORT).show();
            } else {
                Photo newPhoto = DataManager.createPhoto(destinationPath.getName(), project);
                mAdapter.addPhoto(newPhoto);
            }
        }
    }

    private void shareVideo() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("video/*");
        File media = new File(videoPath);
        Uri uri = Uri.fromFile(media);
        share.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(share, getString(R.string.video_choose_share)));
    }

    @Override
    public void onVideoReady(String filePath) {
        videoPath = filePath;
        Toast.makeText(ProjectActivity.this, "Video is ready. See " + filePath, Toast.LENGTH_SHORT).show();
        try {
            //videoView.setVideoPath(videoPath);
            //videoView.setMediaController(new MediaController(ProjectActivity.this));
            //videoView.requestFocus(0);
            //videoView.start();
        } catch (Exception e) {
            Toast.makeText(ProjectActivity.this, R.string.video_error, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onFailure() {
        Toast.makeText(ProjectActivity.this, R.string.ffmpeg_error, Toast.LENGTH_SHORT).show();
    }

    private File destinationPath;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            tryMakePhoto();
        }
    };

    private void tryMakePhoto() {
        if (checkStoragePermission()) {
            makePhoto();
        }
    }

    private void makePhoto() {
        String maskFileName = maskFile == null ? null : maskFile.getAbsolutePath();
        destinationPath = StorageUtil.generateFileName(projectDir);

        Intent i = new CameraActivity.IntentBuilder(ProjectActivity.this)
                .skipConfirm()
                .focusMode(FocusMode.CONTINUOUS)
                .facing(Facing.BACK)
                .to(destinationPath.getAbsoluteFile())
                .mask(maskFileName)
                .debug()
                .zoomStyle(ZoomStyle.PINCH)
                .updateMediaStore()
                .build();

        startActivityForResult(i, REQUEST_PORTRAIT_FFC);
    }

    public class PhotoItemListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final int photoId = (int) v.getTag();
            final android.support.v7.app.AlertDialog.Builder dialogBuilder = new android.support.v7.app.AlertDialog.Builder(ProjectActivity.this);

            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.photo_dialog, null);
            dialogBuilder.setView(dialogView);

            TextView nameView = (TextView) dialogView.findViewById(R.id.name);
            nameView.setText("Фото 5 из 16");


            final android.support.v7.app.AlertDialog alertDialog = dialogBuilder.create();

            dialogView.findViewById(R.id.action_delete).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (alertDialog != null) {
                        alertDialog.dismiss();
                        mAdapter.removePhoto(photoId);
                    }
                    DataManager.deletePhoto(photoId);
                }
            });

            alertDialog.show();
        }
    }
}
