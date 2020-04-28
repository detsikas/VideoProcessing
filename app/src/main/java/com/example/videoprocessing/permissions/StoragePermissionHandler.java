package com.example.videoprocessing.permissions;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;

import com.example.videoprocessing.R;

public class StoragePermissionHandler extends PermissionHandlerAbstract {
    //private static final String TAG = StoragePermissionHandler.class.getSimpleName();
    public static final int CODE = 0b10;

    public StoragePermissionHandler()
    {
        super(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                new int[]{R.string.permission_external_storage_rationale},
                new int[]{R.string.permission_storage_never_ask_again_rationale});
    }

    @Override
    public boolean checkAndRequestPermission(final Activity thisActivity, int code)
    {
        boolean requestPermission = false;
        int isPermissionGranted = ActivityCompat.checkSelfPermission(thisActivity, mPermissionsString[0]);
        if (isPermissionGranted!= PackageManager.PERMISSION_GRANTED)
        {
            // If the permission is not granted, then check if we should show the rationale
            // Now check if we should ask the permission again
            if (mShouldRequestPermission)
            {
                mShouldRequestPermission = false;
                requestPermission = true;
            }
            else
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity, mPermissionsString[0]))
                {
                    if (!mIsDialogVisible)
                    {
                        showPermissionRationale(thisActivity, code, mPermissionsRationale[0]);
                    }
                }
                else
                {
                    // The user has clicked not to be asked again
                    showNeverAskAgainRationale(thisActivity, mNeverAskAgainPermissionsRationale[0]);
                }
                return false;
            }
        }
        if (requestPermission)
        {
            ActivityCompat.requestPermissions(thisActivity,
                    mPermissionsString,
                    code);
            return false;
        }

        return true;
    }

    @Override
    protected void showPermissionRationale(final Activity thisActivity, int code, int rationale)
    {
        DialogInterface.OnClickListener listenerNeutral;
        DialogInterface.OnDismissListener dismissListener;
        DialogInterface.OnShowListener showListener;

        listenerNeutral = (dialog, id) -> {
            mShouldRequestPermission = true;
            dialog.dismiss();
        };

        dismissListener = dialog -> {
            mIsDialogVisible = false;
//            checkAndRequestPermission(thisActivity, code);
        };

        showListener = dialog -> mIsDialogVisible = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        builder.setTitle("Xpoze")
                .setMessage(rationale)
                .setOnDismissListener(dismissListener)
                .setNeutralButton(R.string.ok, listenerNeutral);
        AlertDialog rationaleDialog = builder.create();
        rationaleDialog.setCancelable(false);
        rationaleDialog.setCanceledOnTouchOutside(false);
        rationaleDialog.setOnShowListener(showListener);
        rationaleDialog.show();
    }

    @Override
    protected void showNeverAskAgainRationale(final Activity thisActivity, int rationale)
    {
        DialogInterface.OnClickListener listenerNegative, listenerPositive;

        listenerNegative = (dialog, id) -> dialog.dismiss();

        //listenerPositive = (dialog, id) -> thisActivity.finish();
        listenerPositive = (dialog, id) -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", thisActivity.getPackageName(), null);
            intent.setData(uri);
            thisActivity.getApplicationContext().startActivity(intent);
        };
/*
        DialogInterface.OnDismissListener dismissListener = dialog -> {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", thisActivity.getPackageName(), null);
            intent.setData(uri);
            thisActivity.getApplicationContext().startActivity(intent);
        };*/

        AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity, R.style.Theme_MaterialComponents_Light_Dialog_Alert);
        builder.setTitle("Xpoze")
                .setMessage(rationale)
                .setPositiveButton(R.string.Goto_app_settings, listenerPositive)
                //.setOnDismissListener(dismissListener)
                .setNegativeButton(R.string.ok, listenerNegative);
        AlertDialog rationaleDialog = builder.create();
        rationaleDialog.setCancelable(false);
        rationaleDialog.setCanceledOnTouchOutside(false);
        rationaleDialog.show();
    }
}
