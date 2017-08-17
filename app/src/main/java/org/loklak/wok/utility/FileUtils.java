package org.loklak.wok.utility;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;


public class FileUtils {

    final static boolean IS_KITKAT_AND_ABOVE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public static String getColumnData(Context context, Uri uri,
                                       String selection, String[] selectionArgs) {
        final String column = "_data";
        final String[] projection = {column};

        Cursor cursor = context.getContentResolver().query(
                uri,
                projection,
                selection,
                selectionArgs,
                null);
        if (cursor != null && cursor.moveToFirst()) {
            final int index = cursor.getColumnIndex(column);
            if (index >= 0) {
                return cursor.getString(index);
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return null;
    }

    public static String getPath(final Context context, final Uri uri) {
        if (IS_KITKAT_AND_ABOVE && DocumentsContract.isDocumentUri(context, uri)) {

            // document provider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                final String id = split[1];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + id;
                }
            }
            // downloads provider
            else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        Long.parseLong(id));
                return getColumnData(context, contentUri, null, null);
            }
            // media provider
            else if (isMediaDocument(uri)) {
                Uri contentUri = null;
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                final String id = split[1];

                if (type.equals("image")) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                else if (type.equals("video")) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }
                else if (type.equals("audio")) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = {id};
                return getColumnData(context, contentUri, selection, selectionArgs);
            }
            // file
            else if (uri.getScheme().equalsIgnoreCase("file")) return uri.getPath();
        }
        return null;
    }
}
