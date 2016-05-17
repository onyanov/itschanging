package ru.onyanov.itschanging;

import android.content.Intent;
import android.os.Bundle;
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

import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import ru.onyanov.itschanging.realmObjects.RealmProject;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private ProgressBar progressView;
    private ProjectAdapter projectAdapter;
    private View emptyView;

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
                int height = width / 760 * 435;
                Picasso.with(getApplicationContext())
                        .load(R.drawable.flowers)
                        .resize(width, height)
                        .centerInside()
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
                    Intent intent = new Intent(MainActivity.this, ProjectFormActivity.class);
                    startActivity(intent);
                }
            });
            if (projectCount >= DataManager.MAX_PROJECTS_SIZE) {
                fab.hide();
            } else {
                fab.show();
            }
        }

    }
}
