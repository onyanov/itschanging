package ru.onyanov.itschanging;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import ru.onyanov.itschanging.realmObjects.RealmProject;

public class ProjectFormActivity extends AppCompatActivity {

    private FloatingActionButton buttonCreate;
    private EditText projectName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_form);

        projectName = (EditText) findViewById(R.id.project_name);
        buttonCreate = (FloatingActionButton) findViewById(R.id.fab);

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int projectId = createProject();
                startProjectActivity(projectId);
            }
        });

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
}
