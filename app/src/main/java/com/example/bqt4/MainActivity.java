package com.example.bqt4;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private RecyclerView recyclerView;
    private GalleryAdapter adapter;
    private final List<ImageModel> images = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_all_photos);

        // Initialize ExecutorService for background tasks
        executorService = Executors.newSingleThreadExecutor();

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setHasFixedSize(true);

        // Add scroll listener for endless scrolling
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });



        // Setup SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::refreshGallery);

        checkPermissionAndLoadImages();
    }

    private void checkPermissionAndLoadImages() {
        String[] permissions;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{Manifest.permission.READ_MEDIA_IMAGES};
        } else {
            permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
        }

        if(!hasPermissions(permissions)){
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
        else{
            loadImagesFromGallery();
        }
    }

    private boolean hasPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void loadImagesFromGallery(){
        progressBar.setVisibility(View.VISIBLE);
        executorService.execute(() -> {
            images.clear();
            String[] projection = {
                    MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_ADDED
            };

            Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            try (Cursor cursor = getContentResolver().query(
                    uri,
                    projection,
                    null,
                    null,
                    MediaStore.Images.Media.DATE_ADDED + " DESC"
            )) {
                if (cursor != null) {
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                    int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idColumn);
                        String name = cursor.getString(nameColumn);
                        String path = cursor.getString(dataColumn);

                        File imageFile = new File(path);
                        if (imageFile.exists() && imageFile.canRead()) {
                            images.add(new ImageModel(id, path, name));
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> showError("Error loading images: "+e.getMessage()));
            }
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (adapter == null){
                    adapter = new GalleryAdapter(MainActivity.this, images, (image, sharedImageView) -> {
                        // Handle image click
                        Toast.makeText(this, "Selected: " + image.getName(), Toast.LENGTH_SHORT).show();
                        // You can open the image in full screen here
                    });
                    recyclerView.setAdapter(adapter);
                }
                else{
                    adapter.notifyDataSetChanged();
                }
            });
        });
    }

    private void refreshGallery(){
        loadImagesFromGallery();
    }

    private void showError(String message) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG)
                .setAction("Retry", v -> loadImagesFromGallery())
                .show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadImagesFromGallery();
            } else {
                showPermissionDeniedDialog();
            }
        }
    }



    private void showPermissionDeniedDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Permission Required")
                .setMessage("This app needs access to your photos to display them. Please grant permission in Settings.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    // Open app settings
                    openAppSettings();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gallery_menu, menu);
        return true;
    }

    private void openAppSettings() {
        // Implement opening app settings
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_exit) {
            showExitDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showExitDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Exit Gallery")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Exit", (dialog, which) -> {
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}