package kr.co.dotv365.shexoplayerdemo.framework.util.files;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;

public class FilesUtil {

    public static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            FileInputStream fis = null;
            fis = new FileInputStream(file);
            size = fis.available();
        }

        return size;
    }

    public static String formatFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";

        if (fileS == 0) {
            return wrongSize;
        }

        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        }
        else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        }
        else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        }
        else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }

        return fileSizeString;
    }

    public static String getRealFilePath(final Context context, final Uri uri) {
        if (null == uri) return null;
        final String scheme = uri.getScheme();
        String data = null;

        if (scheme == null) {
            data = uri.getPath();
        }
        else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            data = uri.getPath();
        }
        else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {

            Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.ImageColumns.DATA}, null, null, null);

            if (null != cursor) {

                if (cursor.moveToFirst()) {

                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);

                    if (index > -1) {
                        data = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return data;
    }

}
