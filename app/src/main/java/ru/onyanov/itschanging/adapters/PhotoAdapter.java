package ru.onyanov.itschanging.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ru.onyanov.itschanging.ProjectActivity;
import ru.onyanov.itschanging.R;
import ru.onyanov.itschanging.realmObjects.Photo;

/**
 * Adapter for photos at project screen
 */
public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {

    private final List<Photo> photos;
    private final String projectDir;
    private static ProjectActivity.PhotoItemListener listener;

    public PhotoAdapter(List<Photo> photos, String projectDir, ProjectActivity.PhotoItemListener photoItemListener) {
        this.photos = photos;
        this.projectDir = projectDir;
        listener = photoItemListener;
    }

    public void addPhoto(Photo newPhoto) {
        photos.add(newPhoto);
        notifyDataSetChanged();
    }

    public void removePhoto(int photoId) {
        for (Photo photo : new ArrayList<>(photos)) {
            if (photo.getId() == photoId) {
                photos.remove(photo);
            }
        }
        notifyDataSetChanged();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View mRootView;
        private final TextView mTextView;
        public ImageView mImageView;

        public ViewHolder(View v) {
            super(v);

            mRootView = v;
            mImageView = (ImageView) v.findViewById(R.id.image);
            mTextView = (TextView) v.findViewById(R.id.position);

            v.setOnClickListener(listener);
        }

        public void setPhotoId(int photoId) {
            mRootView.setTag(photoId);
        }

    }

    // Create new views (invoked by the layout manager)
    @Override
    public PhotoAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.photo_item, parent, false);
        //TODO set the view's size, margins, paddings and layout parameters
        return new ViewHolder(v);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Photo photo = photos.get(position);
        holder.setPhotoId(photo.getId());
        final File file = new File(projectDir, photo.getFile());

        final ImageView v = holder.mImageView;
        v.post(new Runnable() {
            @Override
            public void run() {
                int size = v.getWidth();
                Log.d("Height", "" + size);

                Picasso.with(holder.mImageView.getContext())
                        .load(file)
                        .resize(size, size)
                        .centerCrop()
                        .into(holder.mImageView);
            }

        });

        holder.mTextView.setText(holder.mImageView.getContext().getString(R.string.image_number, position + 1));


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return photos.size();
    }

}
