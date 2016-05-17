package ru.onyanov.itschanging.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Dummy adapter to prevent error messages like:
 * "RecyclerView: No adapter attached; skipping layout"
 * See http://stackoverflow.com/a/35942983/1358847
 */
public class EmptyRecyclerAdapter extends RecyclerView.Adapter {
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
