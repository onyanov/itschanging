package ru.onyanov.itschanging;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.io.File;

import ru.onyanov.itschanging.realmObjects.Photo;
import ru.onyanov.itschanging.realmObjects.RealmProject;
import ru.onyanov.itschanging.utils.BlurTransformation;
import ru.onyanov.itschanging.utils.StorageUtil;

public class ProjectFormActivity extends AppCompatActivity {
    private static final String TAG = "ProjectFormActivity";
    public static final String FIELD_PROJECT_ID = "project_id";
    private EditText projectName;
    private int projectId;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_form);

        if (savedInstanceState != null) {
            projectId = savedInstanceState.getInt(FIELD_PROJECT_ID);
        } else {
            projectId = getIntent().getIntExtra(FIELD_PROJECT_ID, 0);
        }

        setUpToolbar();
        projectName = (EditText) findViewById(R.id.project_name);
        imageView = (ImageView) findViewById(R.id.image);
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_action_close);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    private void saveProject() {
        String title = projectName.getText().toString();
        DataManager.renameProject(projectId, title);
    }

    private int createProject() {
        String title = projectName.getText().toString();
        RealmProject project = DataManager.createProject(title);
        return project.getId();
    }

    private void startProjectActivity(int projectId) {
        Intent intent = new Intent(this, ProjectActivity.class);
        intent.putExtra(ProjectActivity.PARAM_ID, projectId);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt(FIELD_PROJECT_ID, projectId);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        projectId = savedInstanceState.getInt(FIELD_PROJECT_ID);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (projectId > 0) {
            RealmProject project = DataManager.getProject(projectId);
            projectName.setText(project.getTitle());
            Editable editable = projectName.getText();
            Selection.setSelection(editable, editable.length());

            imageView.post(new Runnable() {
                @Override
                public void run() {
                    Photo photo = DataManager.getFirstPhoto(projectId);
                    String projectDir = StorageUtil.getImageDirectoryPath(projectId);
                    File file = new File(projectDir, photo.getFile());

                    int width = imageView.getWidth();
                    int height = imageView.getHeight();
                    Log.d(TAG, "width " + width + ", height " + height);

                    Picasso.with(getApplicationContext())
                            .load(file)
                            .transform(new BlurTransformation(getApplicationContext(), 30))
                            .resize(width, height)
                            .centerCrop()
                            .into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {
                                    //TODO show prevent hidden interface
                                }

                                @Override
                                public void onError() {
                                    //TODO decide what if bad file
                                }
                            });
                }

            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_project_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            Intent intent = new Intent(ProjectFormActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        } else if (id == R.id.action_save) {
            if (projectId > 0) {
                saveProject();
            } else {
                projectId = createProject();
            }
            startProjectActivity(projectId);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
