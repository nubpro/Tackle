package com.chai.tackle.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.chai.tackle.R;
import com.nguyenhoanglam.imagepicker.model.Image;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class ThumbnailsAdapter extends RecyclerView.Adapter<ThumbnailsAdapter.ThumbnailHolder> {
    LayoutInflater mInflater;
    Context mContext;
    ArrayList<Image> mImages;
    ArrayList<String> mImagePaths;

    public ThumbnailsAdapter(Context context, ArrayList<Image> images) {
        this.mInflater = LayoutInflater.from(context);
        this.mContext = context;
        this.mImages = images;
    }

    class ThumbnailHolder extends RecyclerView.ViewHolder {
        private final ImageView imageItemView;
        final ThumbnailsAdapter mAdapter;

        public ThumbnailHolder(View itemView, ThumbnailsAdapter adapter) {
            super(itemView);

            imageItemView = itemView.findViewById(R.id.image_thumbnail);
            mAdapter = adapter;
        }
    }

    @NonNull
    @Override
    public ThumbnailsAdapter.ThumbnailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View mItemView = mInflater.inflate(R.layout.thumbnails_item, parent, false);

        return new ThumbnailHolder(mItemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailsAdapter.ThumbnailHolder holder, int position) {
        String path = mImages.get(position).getPath();
        Glide.with(mContext).load(path).into(holder.imageItemView);
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    public void setImages(ArrayList<Image> images) {
        mImages.clear();
        if (images != null) {
            mImages.addAll(images);
        }
        notifyDataSetChanged();
    }
}
