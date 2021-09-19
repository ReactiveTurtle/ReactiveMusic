package ru.reactiveturtle.reactivemusic.toolkit;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class BitmapExtensions {

    public static BitmapDrawable loadSquareDrawableOrDefault(ContentResolver contentResolver, Uri uri, int squareSize) throws IOException {
        InputStream inputStream = contentResolver.openInputStream(uri);
        Objects.requireNonNull(inputStream);
        Drawable drawable = Drawable.createFromStream(inputStream, uri.toString());
        inputStream.close();
        return new BitmapDrawable(Resources.getSystem(), getSquareBitmap(drawableToBitmap(drawable), squareSize));
    }

    public static Bitmap getSquareBitmap(Bitmap src, int squareSize) {
        int size = Math.max(src.getWidth(), src.getHeight());
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawBitmap(src, (size - src.getWidth()) / 2f, (size - src.getHeight()) / 2f, paint);
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, squareSize, squareSize, false);
        bitmap.recycle();
        return scaledBitmap;
    }

    @NonNull
    public static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        Bitmap bitmap;
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
