package com.example.smarthomegesturecontrol;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smarthomegesturecontrol.permissions.OnPermission;
import com.example.smarthomegesturecontrol.permissions.Permission;
import com.example.smarthomegesturecontrol.permissions.XXPermissions;

import java.util.List;

public class WatchGestureActivity extends AppCompatActivity {

    private static String GESTURE_TO_PLAY;
    private VideoView mVideoView;
    private String gestureSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_gesture);

        mVideoView = findViewById(R.id.gestureVideo);

        // get the value from intent object received from mainActivity
        Intent intent = getIntent();
        gestureSelected = intent.getStringExtra("gesture_name");
        GESTURE_TO_PLAY = "h_" + gestureSelected.replaceAll(" ", "_").toLowerCase();

        findViewById(R.id.practice_id).setOnClickListener(v -> checkMyPermission());
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializePlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mVideoView.pause();
        }
    }

    private void initializePlayer() {
        Uri videoUri = getMedia(GESTURE_TO_PLAY);
        mVideoView.setVideoURI(videoUri);
        mVideoView.start();
    }

    private Uri getMedia(String mediaName) {
        return Uri.parse("android.resource://" + getPackageName() + "/raw/" + mediaName);
    }

    private void releasePlayer() {
        mVideoView.stopPlayback();
    }

    public void replayVideo(View view) {
        initializePlayer();
    }

    public void checkMyPermission() {
        XXPermissions.with(this)
                .permission(Permission.Group.STORAGE)
                .permission(Permission.CAMERA)
                .permission(Permission.RECORD_AUDIO)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean all) {
                        if (all) {
                            Intent practiceGestureActivityIntent = new Intent(WatchGestureActivity.this, PracticeGestureActivity.class);
                            practiceGestureActivityIntent.putExtra("gesture_name", gestureSelected);
                            startActivity(practiceGestureActivityIntent);
                        } else {
                            Toast.makeText(getApplicationContext(), "Obtain permission successfully, some permissions are not granted normally", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            Toast.makeText(getApplicationContext(), "Permanently denied permission. Please grant permission manually", Toast.LENGTH_SHORT).show();

                            XXPermissions.gotoPermissionSettings(WatchGestureActivity.this);
                        } else {
                            Toast.makeText(getApplicationContext(), "You can't play and record video without permission", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}