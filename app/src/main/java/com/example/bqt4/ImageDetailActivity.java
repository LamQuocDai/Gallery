package com.example.bqt4;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class ImageDetailActivity extends AppCompatActivity {
    private ViewPager2 viewPager2;
    private List<ImageModel> images;
    private int currentPosition;
    private GestureDetector gestureDetector;
    private boolean isThreeFingerTouch = false;
    private MotionEvent firstTouchEvent = null;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_detail);

        // Find ViewPager2
        viewPager2 = findViewById(R.id.viewPager);

        // Disable default swipe behavior
        viewPager2.setUserInputEnabled(false);

        // Find Back Button
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Get intent extras
        Intent intent = getIntent();
        currentPosition = intent.getIntExtra("position", 0);
        images = (List<ImageModel>) intent.getSerializableExtra("images");

        // Setup adapter
        ImagePagerAdapter adapter = new ImagePagerAdapter(this, images);
        viewPager2.setAdapter(adapter);

        // Set initial position
        viewPager2.setCurrentItem(currentPosition);

        // Add page change callback to update current position
        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
            }
        });

        // Set page transformer for animation
        viewPager2.setPageTransformer(new DepthPageTransformer());

        // Set back button click listener
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Handle back navigation
            }
        });

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();

        // Only process three-finger touches
        if (pointerCount != 3) {
            return super.dispatchTouchEvent(event);
        }

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_POINTER_DOWN:
                // Ensure exactly 3 fingers are down
                if (pointerCount == 3) {
                    isThreeFingerTouch = true;
                    firstTouchEvent = MotionEvent.obtain(event);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (isThreeFingerTouch && firstTouchEvent != null) {
                    float diffX = event.getX() - firstTouchEvent.getX();
                    float diffY = event.getY() - firstTouchEvent.getY();

                    // Ensure horizontal movement is dominant and significant
                    if (Math.abs(diffX) > Math.abs(diffY) && Math.abs(diffX) > 100) {
                        if (diffX > 0) {
                            // Right swipe - previous image
                            if (currentPosition > 0) {
                                viewPager2.setCurrentItem(currentPosition - 1, true);
                                resetTouchState();
                                return true;
                            }
                        } else {
                            // Left swipe - next image
                            if (currentPosition < images.size() - 1) {
                                viewPager2.setCurrentItem(currentPosition + 1, true);
                                resetTouchState();
                                return true;
                            }
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                resetTouchState();
                break;
        }

        return true;
    }

    private void resetTouchState() {
        isThreeFingerTouch = false;
        if (firstTouchEvent != null) {
            firstTouchEvent.recycle();
            firstTouchEvent = null;
        }
    }

    class DepthPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.75f;

        @Override
        public void transformPage(@NonNull View view, float position) {
            int pageWidth = view.getWidth();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);
            } else if (position <= 0) { // [-1,0]
                // Use the default slide transition when moving to the left page
                view.setAlpha(1f);
                view.setTranslationX(0f);
                view.setTranslationZ(0f);
                view.setScaleX(1f);
                view.setScaleY(1f);
            } else if (position <= 1) { // (0,1]
                // Fade the page out.
                view.setAlpha(1 - position);

                // Counteract the default slide transition
                view.setTranslationX(pageWidth * -position);
                // Move it behind the left page
                view.setTranslationZ(-1f);

                // Scale the page down (between MIN_SCALE and 1)
                float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }

}