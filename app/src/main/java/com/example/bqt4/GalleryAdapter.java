package com.example.bqt4;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import java.io.Serializable;
import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {
    private Context context;
    private List<ImageModel> images;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ImageModel image, ImageView sharedImageView);
    }

    public GalleryAdapter(Context context, List<ImageModel> images, OnItemClickListener listener) {
        this.context = context;
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GalleryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.photo_item, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GalleryViewHolder holder, int position) {
        ImageModel image = images.get(position);


        // Enchance image loading with Glide
        RequestOptions requestOptions = new RequestOptions()
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .fitCenter();

            Glide.with(context)
                    .load(image.getPath())
                    .apply(requestOptions)
                    .thumbnail(0.5f)
                    .into(holder.imageView);




        holder.imageView.setTransitionName("image_" + position);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        GalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ImageModel image = images.get(position);

                    Intent intent = new Intent(context, ImageDetailActivity.class);
                    intent.putExtra("image_path", image.getPath());
                    intent.putExtra("position", position);
                    intent.putExtra("images", (Serializable) images);

                    // Create shared element transition
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            (Activity) context,
                            imageView,
                            "shared_image_transition"
                    );


                    // Ensure context is an instance of Activity
                    if ( context instanceof Activity) {
                        ((Activity) context).startActivity(intent, options.toBundle());
                    }
                }
            });
        }
    }
}
