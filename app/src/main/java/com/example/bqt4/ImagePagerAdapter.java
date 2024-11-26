package com.example.bqt4;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.List;

public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
    private final Context context;
    private final List<ImageModel> images;

    public ImagePagerAdapter(Context context, List<ImageModel> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create a PhotoView directly instead of inflating the full layout
        PhotoView photoView = new PhotoView(context);
        photoView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return new ImageViewHolder(photoView);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageModel image = images.get(position);

        // Configure PhotoView
        holder.photoView.setMaximumScale(10.0f);  // Increased max scale
        holder.photoView.setMediumScale(5.0f);    // Increased medium scale
        holder.photoView.setMinimumScale(1.0f);

        // Load image using Glide
        Glide.with(context)
                .load(image.getPath())
                .into(holder.photoView);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        PhotoView photoView;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            this.photoView = (PhotoView) itemView;
        }
    }
}