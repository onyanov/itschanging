package ru.onyanov.itschanging;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import ru.onyanov.camera.Facing;
import ru.onyanov.camera.FocusMode;
import ru.onyanov.camera.ZoomStyle;
import ru.onyanov.itschanging.helpers.GridSpacingItemDecoration;
import ru.onyanov.itschanging.realmObjects.RealmProject;
import ru.onyanov.itschanging.utils.StorageUtil;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_PORTRAIT_FFC = 1;
    private RecyclerView recyclerView;
    private ProgressBar progressView;
    private ProjectAdapter projectAdapter;
    private View emptyView;
    private int newProjectId;
    private String imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        emptyView = findViewById(R.id.empty_view);
        progressView = (ProgressBar) findViewById(R.id.progress);
        recyclerView = (RecyclerView) findViewById(R.id.projects_recycler_view);
        tweakRecyclerView();
    }

    private void tweakRecyclerView() {
        int spanCount = getResources().getInteger(R.integer.project_columns);
        LinearLayoutManager mLayoutManager = new GridLayoutManager(this, spanCount);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        int spacing = getResources().getDimensionPixelSize(R.dimen.gridspacing);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, true));

        progressView.setVisibility(View.VISIBLE);
        final RealmResults<RealmProject> projects = DataManager.getProjects();

        projectAdapter = new ProjectAdapter(this, projects);
        recyclerView.setAdapter(projectAdapter);

        // set up a Realm change listener
        RealmChangeListener changeListener = new RealmChangeListener() {
            @Override
            public void onChange() {
                // This is called anytime the Realm database changes on any thread.
                // Please note, change listeners only work on Looper threads.
                // For non-looper threads, you manually have to use Realm.refresh() instead.
                projectAdapter.notifyDataSetChanged(); // Update the UI
                progressView.setVisibility(View.GONE);
                onProjectsCountChanged(projects.size());
            }
        };
        // Tell Realm to notify our listener when the customers results
        // have changed (items added, removed, updated, anything of the sort).
        projects.addChangeListener(changeListener);

    }

    private void onProjectsCountChanged(int size) {
        if (size == 0) {
            showEmptyState();
        } else {
            emptyView.setVisibility(View.GONE);
        }
        tweakFAB(size);
        Log.d(TAG, "onProjectsCountChanged: " + size);
    }

    private void showEmptyState() {
        emptyView.setVisibility(View.VISIBLE);


        emptyView.post(new Runnable() {
            @Override
            public void run() {
                ImageView emptyImage = (ImageView) findViewById(R.id.empty_image);
                int width = emptyView.getWidth();
                Picasso.with(getApplicationContext())
                        .load(R.drawable.flowers)
                        .resize(width, 0)
                        .into(emptyImage);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void tweakFAB(int projectCount) {
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    buildProject();
                }
            });
            if (projectCount >= DataManager.MAX_PROJECTS_SIZE) {
                fab.hide();
            } else {
                fab.show();
            }
        }

    }

    private void buildProject() {
        if (StorageUtil.checkStoragePermission(this)) {
            RealmProject project = DataManager.createProject(getString(R.string.project_title_default));
            newProjectId = project.getId();
            String projectDir = StorageUtil.getImageDirectoryPath(newProjectId);
            File destinationPath = StorageUtil.generateFileName(projectDir);
            imageName = destinationPath.getName();

            Intent i = new CameraActivity.IntentBuilder(this)
                    .skipConfirm()
                    .focusMode(FocusMode.CONTINUOUS)
                    .facing(Facing.BACK)
                    .to(destinationPath.getAbsoluteFile())
                    .debug()
                    .zoomStyle(ZoomStyle.PINCH)
                    .updateMediaStore()
                    .build();

            startActivityForResult(i, REQUEST_PORTRAIT_FFC);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        DataManager.createPhoto(imageName, DataManager.getProject(newProjectId));
        Intent intent = new Intent(this, ProjectFormActivity.class);
        intent.putExtra(ProjectFormActivity.FIELD_PROJECT_ID, newProjectId);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case StorageUtil.MY_PERMISSIONS_REQUEST_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                // permission was granted, yay! Do the
// contacts-related task you need to do.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    buildProject();
                } else {
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

    private class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildLayoutPosition(view) == 0) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
        }
    }
}
