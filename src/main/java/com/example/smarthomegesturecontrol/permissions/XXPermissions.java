package com.example.smarthomegesturecontrol.permissions;

import android.app.Activity;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class XXPermissions {

    private Activity mActivity;
    private List<String> mPermissions;
    private boolean mConstant;

    /**
     * Privatization constructor
     */
    private XXPermissions(Activity activity) {
        mActivity = activity;
    }

    /**
     * Sets the requested object
     */
    public static XXPermissions with(Activity activity) {
        return new XXPermissions(activity);
    }

    /**
     * Set permission group
     */
    public XXPermissions permission(String... permissions) {
        if (mPermissions == null) {
            mPermissions = new ArrayList<>(permissions.length);
        }
        mPermissions.addAll(Arrays.asList(permissions));
        return this;
    }

    /**
     * Set permission group
     */
    public XXPermissions permission(String[]... permissions) {
        if (mPermissions == null) {
            int length = 0;
            for (String[] permission : permissions) {
                length += permission.length;
            }
            mPermissions = new ArrayList<>(length);
        }
        for (String[] group : permissions) {
            mPermissions.addAll(Arrays.asList(group));
        }
        return this;
    }

    /**
     * Set permission group
     */
    public XXPermissions permission(List<String> permissions) {
        if (mPermissions == null) {
            mPermissions = permissions;
        } else {
            mPermissions.addAll(permissions);
        }
        return this;
    }

    /**
     * If rejected, continue to apply until authorized or permanently rejected
     */
    public XXPermissions constantRequest() {
        mConstant = true;
        return this;
    }

    /**
     * Request permission to
     */
    public void request(OnPermission callback) {
        // If no permission is specified for the request, the request is made using the permission registered with the manifest
        if (mPermissions == null || mPermissions.isEmpty()) {
            mPermissions = PermissionUtils.getManifestPermissions(mActivity);
        }
        if (mPermissions == null || mPermissions.isEmpty()) {
            throw new IllegalArgumentException("The requested permission cannot be empty");
        }
        if (mActivity == null) {
            throw new IllegalArgumentException("The activity is empty");
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && mActivity.isDestroyed()) {
                throw new IllegalStateException("The event has been destroyed");
            } else if (mActivity.isFinishing()) {
                throw new IllegalStateException("The event has been finish");
            }
        }
        if (callback == null) {
            throw new IllegalArgumentException("The permission request callback interface must be implemented");
        }

        PermissionUtils.checkTargetSdkVersion(mActivity, mPermissions);

        ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(mActivity, mPermissions);

        if (failPermissions == null || failPermissions.isEmpty()) {
            // Prove that all permissions have been granted
            callback.hasPermission(mPermissions, true);
        } else {
            // Checks whether the permissions are registered in the manifest file
            PermissionUtils.checkPermissions(mActivity, mPermissions);
            // The application has not been granted permission
            PermissionFragment.newInstance((new ArrayList<>(mPermissions)), mConstant).prepareRequest(mActivity, callback);
        }
    }

    /**
     * Check that certain permissions are fully granted
     *
     * @param context     Context object
     * @param permissions The permission group to request
     */
    public static boolean isHasPermission(Context context, String... permissions) {
        return isHasPermission(context, Arrays.asList(permissions));
    }

    public static boolean isHasPermission(Context context, List<String> permissions) {
        ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(context, permissions);
        return failPermissions == null || failPermissions.isEmpty();
    }

    /**
     * Check that certain permissions are fully granted
     *
     * @param context     Context object
     * @param permissions The permission group to request
     */
    public static boolean isHasPermission(Context context, String[]... permissions) {
        List<String> permissionList = new ArrayList<>();
        for (String[] group : permissions) {
            permissionList.addAll(Arrays.asList(group));
        }
        ArrayList<String> failPermissions = PermissionUtils.getFailPermissions(context, permissionList);
        return failPermissions == null || failPermissions.isEmpty();
    }

    /**
     * Skip to the Application Permission Settings page
     *
     * @param context Context object
     */
    public static void gotoPermissionSettings(Context context) {
        PermissionSettingPage.start(context, false);
    }

    /**
     * Skip to the Application Permission Settings page
     *
     * @param context Context object
     * @param newTask Whether to start with a new task stack
     */
    public static void gotoPermissionSettings(Context context, boolean newTask) {
        PermissionSettingPage.start(context, newTask);
    }
}