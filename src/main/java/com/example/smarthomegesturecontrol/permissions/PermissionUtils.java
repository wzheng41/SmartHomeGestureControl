package com.example.smarthomegesturecontrol.permissions;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class PermissionUtils {

    /**
     * Version 6.0 or above
     */
    static boolean isOverMarshmallow() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * Version 8.0 or above
     */
    static boolean isOverOreo() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
    }

    /**
     * Returns the permissions that the application registered in the manifest file
     */
    static List<String> getManifestPermissions(Context context) {
        try {
            return Arrays.asList(context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_PERMISSIONS).requestedPermissions);
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
    }

    /**
     * Do you have installation permissions
     */
    static boolean isHasInstallPermission(Context context) {
        if (isOverOreo()) {
            return context.getPackageManager().canRequestPackageInstalls();
        }
        return true;
    }

    /**
     * Whether there is a floating window permission
     */
    static boolean isHasOverlaysPermission(Context context) {
        if (isOverMarshmallow()) {
            return Settings.canDrawOverlays(context);
        }
        return true;
    }

    /**
     * Gets an ungranted permission
     *
     * @param context     Context object
     * @param permissions The permission group to request
     */
    static ArrayList<String> getFailPermissions(Context context, List<String> permissions) {
        // Returns null if Android 6.0 or below
        if (!PermissionUtils.isOverMarshmallow()) {
            return null;
        }

        ArrayList<String> failPermissions = null;

        for (String permission : permissions) {
            // Check installation permissions
            if (Permission.REQUEST_INSTALL_PACKAGES.equals(permission)) {

                if (!isHasInstallPermission(context)) {
                    if (failPermissions == null) {
                        failPermissions = new ArrayList<>();
                    }
                    failPermissions.add(permission);
                }
                continue;
            }

            // Check the hover window permissions
            if (Permission.SYSTEM_ALERT_WINDOW.equals(permission)) {

                if (!isHasOverlaysPermission(context)) {
                    if (failPermissions == null) {
                        failPermissions = new ArrayList<>();
                    }
                    failPermissions.add(permission);
                }
                continue;
            }

            // Detect two new permissions for 8.0
            if (Permission.ANSWER_PHONE_CALLS.equals(permission) || Permission.READ_PHONE_NUMBERS.equals(permission)) {
                // Check that the current version of Android meets the requirements
                if (!isOverOreo()) {
                    continue;
                }
            }

            // Adds ungranted permissions to a collection
            if (context.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED) {
                if (failPermissions == null) {
                    failPermissions = new ArrayList<>();
                }
                failPermissions.add(permission);
            }
        }

        return failPermissions;
    }

    /**
     * Whether you can continue to apply for ungranted permissions
     *
     * @param activity        The Activity object
     * @param failPermissions Failed permissions
     */
    static boolean isRequestDeniedPermission(Activity activity, List<String> failPermissions) {
        for (String permission : failPermissions) {
            // Setup permission and float permission are not considered, but the application method is different from the dangerous permission method. There is no permanent reject option, so return false here
            if (Permission.REQUEST_INSTALL_PACKAGES.equals(permission) || Permission.SYSTEM_ALERT_WINDOW.equals(permission)) {
                continue;
            }

            // Check to see if there is still permission to continue the application (in this case no permission has been granted but has not been permanently denied).
            if (!checkSinglePermissionPermanentDenied(activity, permission)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check in the permission group to see if any of the permissions are permanently denied
     *
     * @param activity    The Activity object
     * @param permissions Requested permission
     */
    static boolean checkMorePermissionPermanentDenied(Activity activity, List<String> permissions) {
        for (String permission : permissions) {
            // Setup permission and float permission are not considered, but the application method is different from the dangerous permission method. There is no permanent reject option, so return false here
            if (Permission.REQUEST_INSTALL_PACKAGES.equals(permission) || Permission.SYSTEM_ALERT_WINDOW.equals(permission)) {
                continue;
            }
            if (checkSinglePermissionPermanentDenied(activity, permission)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether a permission is permanently denied
     *
     * @param activity   The Activity Object
     * @param permission Requested permission
     */
    private static boolean checkSinglePermissionPermanentDenied(Activity activity, String permission) {
//        // Setup permission and float permission are not considered, but the application method is different from the dangerous permission method. There is no permanent reject option, so return false here
//        if (Permission.REQUEST_INSTALL_PACKAGES.equals(permission) || Permission.SYSTEM_ALERT_WINDOW.equals(permission)) {
//            return false;
//        }

        // Detect two new permissions for 8.0
        if (Permission.ANSWER_PHONE_CALLS.equals(permission) || Permission.READ_PHONE_NUMBERS.equals(permission)) {
            // Check that the current version of Android meets the requirements
            if (!isOverOreo()) {
                return false;
            }
        }

        if (PermissionUtils.isOverMarshmallow()) {
            return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED &&
                    !activity.shouldShowRequestPermissionRationale(permission);
        }

        return false;
    }

    /**
     * Gets an ungranted permission
     *
     * @param permissions  The permission group to request
     * @param grantResults Allowable result group
     */
    static List<String> getFailPermissions(String[] permissions, int[] grantResults) {
        List<String> failPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            // Adds ungranted permissions to the collection. -1 means ungranted and 0 means granted
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                failPermissions.add(permissions[i]);
            }
        }

        return failPermissions;
    }

    /**
     * Gets the granted permission
     *
     * @param permissions  The permission group to request
     * @param grantResults Allowable result group
     */
    static List<String> getSucceedPermissions(String[] permissions, int[] grantResults) {
        List<String> succeedPermissions = new ArrayList<>();
        for (int i = 0; i < grantResults.length; i++) {
            // Adds the granted permissions to the collection. -1 means not granted and 0 means granted
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                succeedPermissions.add(permissions[i]);
            }
        }

        return succeedPermissions;
    }

    /**
     * Checks whether the permissions are registered in the manifest file
     *
     * @param activity           The Activity object
     * @param requestPermissions The permission group requested
     */
    static void checkPermissions(Activity activity, List<String> requestPermissions) {
        List<String> manifestPermissions = PermissionUtils.getManifestPermissions(activity);
        if (manifestPermissions != null && !manifestPermissions.isEmpty()) {
            for (String permission : requestPermissions) {
                if (!manifestPermissions.contains(permission)) {
                    throw new ManifestException(permission);
                }
            }
        } else {
            throw new ManifestException();
        }
    }

    /**
     * Check that targetSdkVersion meets the requirements
     *
     * @param context            Context object
     * @param requestPermissions The permission group requested
     */
    static void checkTargetSdkVersion(Context context, List<String> requestPermissions) {
        // Check that 8.0 permissions are included
        if (requestPermissions.contains(Permission.REQUEST_INSTALL_PACKAGES)
                || requestPermissions.contains(Permission.ANSWER_PHONE_CALLS)
                || requestPermissions.contains(Permission.READ_PHONE_NUMBERS)) {
            // You must set targetSdkVersion >= 26 to properly detect permissions
            if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.O) {
                throw new RuntimeException("The targetSdkVersion SDK must be 26 or more");
            }
        } else {
            // You must set targetSdkVersion >= 23 to properly detect permissions
            if (context.getApplicationInfo().targetSdkVersion < Build.VERSION_CODES.M) {
                throw new RuntimeException("The targetSdkVersion SDK must be 23 or more");
            }
        }
    }
}