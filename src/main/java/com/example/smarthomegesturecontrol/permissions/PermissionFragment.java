package com.example.smarthomegesturecontrol.permissions;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class PermissionFragment extends Fragment implements Runnable {

    /**
     * The global Handler object
     */
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    /**
     * Requested permission
     */
    private static final String PERMISSION_GROUP = "permission_group";
    /**
     * Request code (automatically generated)
     */
    private static final String REQUEST_CODE = "request_code";
    /**
     * Do you keep asking
     */
    private static final String REQUEST_CONSTANT = "request_constant";
    /**
     * Callback object store
     */
    private final static SparseArray<OnPermission> PERMISSION_ARRAY = new SparseArray<>();
    /**
     * Whether it has already been called back, to avoid duplicate callbacks caused by simultaneous requests for installation permissions and hover Windows
     */
    private boolean mCallback;

    public static PermissionFragment newInstance(ArrayList<String> permissions, boolean constant) {
        PermissionFragment fragment = new PermissionFragment();
        Bundle bundle = new Bundle();
        int requestCode;
        // Request code random generation, to avoid random generation of the previous request code, must be circular judgment
        do {
            // The Studio compiled APK request code must be less than 65536
            // The APK request code compiled by Eclipse must be less than 256
            requestCode = new Random().nextInt(255);
        } while (PERMISSION_ARRAY.get(requestCode) != null);
        bundle.putInt(REQUEST_CODE, requestCode);
        bundle.putStringArrayList(PERMISSION_GROUP, permissions);
        bundle.putBoolean(REQUEST_CONSTANT, constant);
        fragment.setArguments(bundle);

        return fragment;
    }

    public void prepareRequest(Activity activity, OnPermission callback) {
        // Adds the current request code and object to the collection
        PERMISSION_ARRAY.put(getArguments().getInt(REQUEST_CODE), callback);
        activity.getFragmentManager().beginTransaction().add(this, activity.getClass().getName()).commitAllowingStateLoss();
    }

    @SuppressLint("InlinedApi")
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ArrayList<String> permissions = getArguments().getStringArrayList(PERMISSION_GROUP);

        if (permissions == null) {
            return;
        }

        boolean isRequestPermission = false;
        if (permissions.contains(Permission.REQUEST_INSTALL_PACKAGES) && !PermissionUtils.isHasInstallPermission(getActivity())) {
            // Skip to allow installation of unknown sources Settings page
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getContext().getPackageName()));
            startActivityForResult(intent, getArguments().getInt(REQUEST_CODE));
            isRequestPermission = true;
        }

        if (permissions.contains(Permission.SYSTEM_ALERT_WINDOW) && !PermissionUtils.isHasOverlaysPermission(getActivity())) {
            // Jump to the hover window Settings page
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
            startActivityForResult(intent, getArguments().getInt(REQUEST_CODE));
            isRequestPermission = true;
        }

        // There must currently be no jump to the hover window or installation permissions screen
        if (!isRequestPermission) {
            requestPermission();
        }
    }

    public void requestPermission() {
        if (PermissionUtils.isOverMarshmallow()) {
            ArrayList<String> permissions = getArguments().getStringArrayList(PERMISSION_GROUP);
            if (permissions != null && permissions.size() > 0) {
                requestPermissions(permissions.toArray(new String[permissions.size() - 1]), getArguments().getInt(REQUEST_CODE));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        OnPermission callback = PERMISSION_ARRAY.get(requestCode);

        // According to the request code out of the object is empty, it is directly returned without processing
        if (callback == null) {
            return;
        }

        for (int i = 0; i < permissions.length; i++) {
            // Re-check installation permissions
            if (Permission.REQUEST_INSTALL_PACKAGES.equals(permissions[i])) {
                if (PermissionUtils.isHasInstallPermission(getActivity())) {
                    grantResults[i] = PackageManager.PERMISSION_GRANTED;
                } else {
                    grantResults[i] = PackageManager.PERMISSION_DENIED;
                }
            }

            // Re-check the hover window permissions
            if (Permission.SYSTEM_ALERT_WINDOW.equals(permissions[i])) {
                if (PermissionUtils.isHasOverlaysPermission(getActivity())) {
                    grantResults[i] = PackageManager.PERMISSION_GRANTED;
                } else {
                    grantResults[i] = PackageManager.PERMISSION_DENIED;
                }
            }

            // Re-check the two new permissions for 8.0
            if (Permission.ANSWER_PHONE_CALLS.equals(permissions[i]) || Permission.READ_PHONE_NUMBERS.equals(permissions[i])) {
                // Check that the current version of Android meets the requirements
                if (!PermissionUtils.isOverOreo()) {
                    grantResults[i] = PackageManager.PERMISSION_GRANTED;
                }
            }
        }

        // Get the grant permission
        List<String> succeedPermissions = PermissionUtils.getSucceedPermissions(permissions, grantResults);
        // If the request succeeds with a permission set that is the same size as the array of requests, all permissions are granted
        if (succeedPermissions.size() == permissions.length) {
            // All permissions requested by the representative have been granted
            callback.hasPermission(succeedPermissions, true);
        } else {
            // Get deny permission
            List<String> failPermissions = PermissionUtils.getFailPermissions(permissions, grantResults);

            // Check to see if continuing to apply mode is enabled, and if so, to see if ungranted permissions can continue to apply
            if (getArguments().getBoolean(REQUEST_CONSTANT)
                    && PermissionUtils.isRequestDeniedPermission(getActivity(), failPermissions)) {

                // If so, continue to request permission until the user authorizes it or rejects it permanently
                requestPermission();
                return;
            }

            // If any permission is denied permanently, it will return true to the developer, so that the developer can guide the user to set the permissions to open the interface
            callback.noPermission(failPermissions, PermissionUtils.checkMorePermissionPermanentDenied(getActivity(), failPermissions));

            // Prove that some permissions have been granted successfully, and that the callback is successful
            if (!succeedPermissions.isEmpty()) {
                callback.hasPermission(succeedPermissions, false);
            }
        }

        // After the permission callback, the objects in the collection should be deleted to avoid repeated requests
        PERMISSION_ARRAY.remove(requestCode);
        getFragmentManager().beginTransaction().remove(this).commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mCallback && requestCode == getArguments().getInt(REQUEST_CODE)) {
            mCallback = true;
            // Need to delay the execution, otherwise some Huawei models are authorized but can not get the permission
            HANDLER.postDelayed(this, 500);
        }
    }

    @Override
    public void run() {
        // If the user has been away for too long, the Activity will be recalled, so it is important to check if the current Fragment has been added to the Activity (this can enable a Bug in developer mode that does not retain the Activity repeat crash).
        if (isAdded()) {
            // Request additional dangerous permissions
            requestPermission();
        }
    }
}