package net.peeknpoke.apps.videoprocessing.permissions;

import android.app.Activity;

abstract class PermissionHandlerAbstract {
    final String[] mPermissionsString;
    final int[] mPermissionsRationale;
    final int[] mNeverAskAgainPermissionsRationale;
    boolean mShouldRequestPermission = true;
    boolean mIsDialogVisible = false;

    PermissionHandlerAbstract(String[] mPermissionString,
                              int[] mPermissionsRationale, int[] mNeverAskAgainPermissionsRationale)
    {
        this.mPermissionsString = mPermissionString;
        this.mPermissionsRationale = mPermissionsRationale;
        this.mNeverAskAgainPermissionsRationale = mNeverAskAgainPermissionsRationale;
    }

    public abstract boolean checkAndRequestPermission(final Activity thisActivity, int code);
    protected abstract void showPermissionRationale(final Activity thisActivity, int code, int rationale);
    protected abstract void showNeverAskAgainRationale(final Activity thisActivity, int rationale);
}
