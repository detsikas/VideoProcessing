package com.example.videoprocessing;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class FileOperations {

    static boolean uriExists(Uri uri)
    {
        File file = new File(uri.toString());
        return file.exists();
    }

    static Uri getAppMediaUri(Context context)
    {
        return MediaStore.Files.getContentUri("external").
                buildUpon().appendPath(context.getResources().getString(R.string.app_name)).build();
        //return FileProvider.getUriForFile(context, context.getApplicationContext().getPackageName()+
        //".provider", getAppMediaFolder());
        //return Uri.fromFile(getAppMediaFolder());
    }

    static File getAppMediaFolder(Context context)
    {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return  null;
        }

        String appName = context.getResources().getString(R.string.app_name);

        File mediaStorageDir = new  File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM),appName );

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()) {
                Log.d(appName, "failed to create directory");
                return null;
            }
        }

        return mediaStorageDir;
    }

    static File createMediaFile(File mediaStorageDir, String filename)
    {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());

        String definingFilename = mediaStorageDir.getPath() + File.separator +
                timeStamp + "_" + filename;
        definingFilename = definingFilename.replace(' ','_').replace('\'','_');
        return new File(definingFilename + ".jpg");
    }
}
