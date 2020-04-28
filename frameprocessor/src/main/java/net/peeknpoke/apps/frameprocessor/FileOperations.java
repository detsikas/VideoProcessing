package net.peeknpoke.apps.frameprocessor;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class FileOperations {

    static File getAppMediaFolder(String appName)
    {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        if (!Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return  null;
        }

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
