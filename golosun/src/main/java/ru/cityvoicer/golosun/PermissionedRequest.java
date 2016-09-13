package ru.cityvoicer.golosun;

abstract class PermissionedRequest {
    private String[] mPermissions;
    private boolean mRetryPermission;

    public PermissionedRequest(String[] permissions, boolean retryPermission) {
        mPermissions = permissions;
        mRetryPermission = retryPermission;
    }

    abstract void onPermissionGranted();
    abstract void onCancel();
    abstract void onPermissionNotGranted();

    public String[] getPermissions() {
        return mPermissions;
    }

    public boolean getRetryPermissionFlag() {
        return mRetryPermission;
    }

    public int getCode() {
        return getClass().hashCode() & 0xffff;
    }
}
