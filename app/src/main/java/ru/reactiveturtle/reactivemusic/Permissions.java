package ru.reactiveturtle.reactivemusic;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import ru.reactiveturtle.reactivemusic.player.mvp.view.PlayerActivity;

public class Permissions {
    public static void requestPermission(Activity activity, String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public static boolean hasSelfPermission(Context context, String permission) {
        return (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
    }

    public static boolean hasExternalStorage(Context context) {
        return hasSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    public static void requestExternalStorage(Activity activity) {
        requestPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE, 0);
    }
}