package com.example.smarthomegesturecontrol;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smarthomegesturecontrol.permissions.OnPermission;
import com.example.smarthomegesturecontrol.permissions.Permission;
import com.example.smarthomegesturecontrol.permissions.XXPermissions;

import org.conscrypt.Conscrypt;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.widget.Toast.makeText;

public class PracticeGestureActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private Camera mCamera = null;
    private int mPreviewHeight;
    private int mPreviewWidth;
    private static String mediaFileName = null;

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private SurfaceView mySurfaceView = null;
    private SurfaceHolder mySurfaceHolder = null;
    private int mySurfaceViewLayoutWidth = 0;

    private boolean myIsRecording = false;

    private static int CAMERA_RIGHT_ORIENTATION = 0;
    private static MediaRecorder myRecorder;
    private static String gestureNameToPractice;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice_gesture);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        HashMap<String, String> gestureNameAndValue = new HashMap<>();

        gestureNameAndValue.put("Select a Gesture", "selectGesture");
        gestureNameAndValue.put("Turn On Lights", "lightsOn");
        gestureNameAndValue.put("Turn Off Lights", "lightsOff");
        gestureNameAndValue.put("Turn On Fan", "fanOn");
        gestureNameAndValue.put("Turn Off Fan", "fanOff");
        gestureNameAndValue.put("Increase Fan Speed", "fanUp");
        gestureNameAndValue.put("Decrease Fan Speed", "fanDown");
        gestureNameAndValue.put("Set Thermostat to specified temperature", "setThermo");
        gestureNameAndValue.put("0", "num0");
        gestureNameAndValue.put("1", "num1");
        gestureNameAndValue.put("2", "num2");
        gestureNameAndValue.put("3", "num3");
        gestureNameAndValue.put("4", "num4");
        gestureNameAndValue.put("5", "num5");
        gestureNameAndValue.put("6", "num6");
        gestureNameAndValue.put("7", "num7");
        gestureNameAndValue.put("8", "num8");
        gestureNameAndValue.put("9", "num9");

        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            releaseMediaRecorder();
            releaseCamera();
        });

        Intent intent = getIntent();
        gestureNameToPractice = gestureNameAndValue.get(intent.getStringExtra("gesture_name"));

        start();
    }

    private void start() {
        mCamera = getCameraInstance();
        CAMERA_RIGHT_ORIENTATION = getRightCameraDisplayOrientation(this, findFrontFacingCamera(), mCamera);
        mCamera.setDisplayOrientation(CAMERA_RIGHT_ORIENTATION);

        // Surface view
        mySurfaceView = findViewById(R.id.camera_surface_view);
        mySurfaceHolder = mySurfaceView.getHolder();
        mySurfaceHolder.addCallback(this);
        mySurfaceViewLayoutWidth = mySurfaceView.getLayoutParams().width;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        // Record Button on click listener
        findViewById(R.id.btnRecord).setOnClickListener(v -> {
            checkMyPermission();
        });

        // Upload button listener
        findViewById(R.id.btnUpload).setOnClickListener(v -> {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(this);
            }
            progressDialog.setMessage("Uploading the video");
            progressDialog.show();

            postRequest();
        });
    }

    private void checkMyPermission() {
        XXPermissions.with(this)
                .permission(Permission.Group.STORAGE)
                .permission(Permission.CAMERA)
                .permission(Permission.RECORD_AUDIO)
                .request(new OnPermission() {
                    @Override
                    public void hasPermission(List<String> granted, boolean all) {
                        if (all) {
                            if (myIsRecording) {
                                stopRecording();
                            } else {
                                // initialize video camera
                                if (prepareVideoRecorder()) {
                                    // Camera is available and unlocked, MediaRecorder is prepared,
                                    // now you can start recording
                                    myRecorder.start();

                                    // inform the user that recording has started
                                    Toast.makeText(getApplicationContext(), "Started recording", Toast.LENGTH_SHORT).show();
                                    myIsRecording = true;
                                } else {
                                    // prepare didn't work, release the camera
                                    releaseMediaRecorder();
                                    // inform user
                                }
                            }
                        } else {
                            makeText(getApplicationContext(), "Obtain permission successfully, some permissions are not granted normally", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void noPermission(List<String> denied, boolean quick) {
                        if (quick) {
                            Toast.makeText(getApplicationContext(), "Permanently denied permission. Please grant permission manually", Toast.LENGTH_SHORT).show();

                            XXPermissions.gotoPermissionSettings(PracticeGestureActivity.this);
                        } else {
                            Toast.makeText(getApplicationContext(), "You can't play and record video without permission", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Stop Recording
     */
    private void stopRecording() {
        myRecorder.stop(); // stop the recording
        Toast.makeText(this, "Recording complete", Toast.LENGTH_SHORT).show();
        myIsRecording = false;
        releaseMediaRecorder(); // release the MediaRecorder object
        mCamera.lock(); // take camera access back from MediaRecorder

        // inform theuser that recording has stoppeddwLÒ
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters) {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width < size.height) {
                continue; // we are only interested in landscape variants
            }

            if (result == null) {
                result = size;
            } else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result = size;
                }
            }
        }

        return (result);
    }

    private boolean prepareVideoRecorder() {
        myRecorder = new MediaRecorder();

        // This will call when the video will be stoped after reach maximum time set for the video
        myRecorder.setOnInfoListener((mr, what, extra) -> {
            if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                stopRecording();
            }
        });

        // Step 1: Unlock and set camera to MediaRecorder and it's orientation, this orientation will be used for the stored video
        mCamera.unlock();
        myRecorder.setCamera(mCamera);
        myRecorder.setOrientationHint(CAMERA_RIGHT_ORIENTATION + 180);

        // Step 2: Set sources
        myRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        myRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
//        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
        // Customise your profile based on a pre-existing profile
        CamcorderProfile profile = CamcorderProfile.get(CameraInfo.CAMERA_FACING_FRONT, CamcorderProfile.QUALITY_HIGH);
        myRecorder.setProfile(profile);

        mediaFileName = getOutputMediaFile(MEDIA_TYPE_VIDEO).toString();
        // Step 4: Set output file
        myRecorder.setOutputFile(mediaFileName);
        myRecorder.setMaxDuration(5000); // 50 seconds

        // recorder.setMaxFileSize(500000000); // Approximately 500 megabytes
        myRecorder.setVideoSize(mPreviewWidth, mPreviewHeight);

        // Step 5: Set the preview output
        myRecorder.setPreviewDisplay(mySurfaceHolder.getSurface());

        // Step 6: Prepare configured MediaRecorder
        try {
            myRecorder.prepare();
        } catch (IllegalStateException | IOException e) {
            Toast.makeText(getApplicationContext(), "exception: " + e.getMessage(), Toast.LENGTH_LONG).show();
            releaseMediaRecorder();
            return false;
        }

        return true;
    }

    private void releaseMediaRecorder() {
        if (myRecorder != null) {
            myRecorder.reset(); // clear recorder configuration
            myRecorder.release(); // release the recorder object
            myRecorder = null;
            mCamera.lock(); // lock camera for later use
        }
    }

    /**
     * Release camera on stop, so that other resources can start using it.
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(findFrontFacingCamera()); // attempt to get a Camera instance
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    private static int findFrontFacingCamera() {
        int cameraId = 0;
        boolean cameraFront;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                cameraFront = true;
                break;
            }
        }

        return cameraId;
    }

    public static int getRightCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        CameraInfo info = new CameraInfo();
        camera.getCameraInfo(cameraId, info);

        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;

        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setRecordingHint(true);
        Camera.Size size = getBestPreviewSize(parameters);
        mCamera.setParameters(parameters);

        // resize the view to the specified surface view width in layout
        int newHeight = size.height / (size.width / mySurfaceViewLayoutWidth);
        mySurfaceView.getLayoutParams().height = newHeight;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mPreviewHeight = mCamera.getParameters().getPreviewSize().height;
        mPreviewWidth = mCamera.getParameters().getPreviewSize().width;

        mCamera.stopPreview();
        try {
            mCamera.setPreviewDisplay(mySurfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (myIsRecording) {
            stopRecording();
        }
        releaseMediaRecorder();
        releaseCamera();
    }

    /**
     * Create a file Uri for saving an image or video
     */
    private static Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /**
     * Create a File for saving an image or video
     */
    private static File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                System.out.println("MyCameraApp" + "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;

        mediaFileName = gestureNameToPractice + "_PRACTICE_" + timeStamp + "_zheng" + ".mp4";
        String mediaDirectory = mediaStorageDir.getPath();

        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaDirectory + File.separator + "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaDirectory + File.separator + mediaFileName);
        } else {
            return null;
        }

        return mediaFile;
    }

    public void postRequest() {
        Security.insertProviderAt(Conscrypt.newProvider(), 1);

        String[] dirarray = mediaFileName.split("/");
        String file_name = dirarray[6];

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file_name, RequestBody.create(MediaType.parse("video/mp4"), new File(mediaFileName)))
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://192.168.0.35:5000/upload")
                .post(requestBody)
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) {
                runOnUiThread(() -> {
                    progressDialog.dismiss();

                    try {
                        String response_body = response.body().string();
                        System.out.println(response_body);
                        Toast.makeText(getApplicationContext(), response_body, Toast.LENGTH_LONG).show();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Intent gotoMainActivity = new Intent(PracticeGestureActivity.this, MainActivity.class);
                        startActivity(gotoMainActivity);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                e.printStackTrace();

                call.cancel();

                runOnUiThread(() -> {
                    progressDialog.dismiss();

                    Toast.makeText(getApplicationContext(), "Something went wrong:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

}