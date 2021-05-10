package com.example.smarthomegesturecontrol.permissions;

final class ManifestException extends RuntimeException {

    ManifestException() {
        // No permissions are registered in the manifest file
        super("No permissions are registered in the manifest file");
    }

    ManifestException(String permission) {
        // The requested dangerous permissions are not registered in the manifest file
        super(permission + ": Permissions are not registered in the manifest file");
    }
}