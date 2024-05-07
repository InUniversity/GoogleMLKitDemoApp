package com.example.googlemlkitdemoapp;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    public static Bitmap uriToBitmap(Context context, Uri uri) {
        try {
            ContentResolver resolver = context.getContentResolver();
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(resolver, uri);
            return bitmap;
        } catch (IOException e) {
            Log.e(TAG, "Error converting Uri to Bitmap: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
