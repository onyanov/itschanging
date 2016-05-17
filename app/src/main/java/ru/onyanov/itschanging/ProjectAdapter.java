package ru.onyanov.itschanging;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.RealmResults;
import ru.onyanov.itschanging.realmObjects.Photo;
import ru.onyanov.itschanging.realmObjects.RealmProject;
import ru.onyanov.itschanging.utils.StorageUtil;

public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

    private static final String TAG = "ProjectAdapter";

    private final RealmResults<RealmProject> projects;
    private final Context context;

    public ProjectAdapter(Context context, RealmResults<RealmProject> projects) {
        this.context = context;
        this.projects = projects;
    }

    @Override
    public long getItemId(int position) {
        return projects.get(position).getId();
    }

    @Override
    public int getItemCount() {
        return projects.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.project_item, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final RealmProject project = projects.get(position);
        holder.setProjectId(project.getId());
        final String projectDir = StorageUtil.getImageDirectoryPath(project.getId());
        holder.nameView.setText(project.getTitle());

        final ImageView v = holder.imageView;
        v.post(new Runnable() {
            @Override
            public void run() {
                //TODO set the view's size, margins, paddings and layout parameters
                int width = v.getWidth();
                Log.d(TAG, "run: width=" + width);

                Photo maskPhoto = DataManager.getFirstPhoto(project.getId());
                //File projectDir = DataManager.getProjectDirectory(context, project);

                if (maskPhoto != null) {
                    int height = width * 9 / 16;
                    File file = new File(projectDir, maskPhoto.getFile());

                    Picasso.with(holder.imageView.getContext())
                            .load(file)
                            .resize(width, height)
                            .centerCrop()
                            .into(holder.imageView);
                }
            }

        });
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView nameView;
        ImageView imageView;
        int projectId;

        public ViewHolder(View itemView) {
            super(itemView);

            nameView = (TextView) itemView.findViewById(R.id.name);
            imageView = (ImageView) itemView.findViewById(R.id.image);

            itemView.setOnClickListener(this);
        }

        public void setProjectId(int projectId) {
            this.projectId = projectId;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(context, ProjectActivity.class);
            intent.putExtra(ProjectActivity.PARAM_ID, projectId);
            context.startActivity(intent);
        }
    }

}
