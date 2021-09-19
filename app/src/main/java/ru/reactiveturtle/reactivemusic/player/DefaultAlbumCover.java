package ru.reactiveturtle.reactivemusic.player;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.ColorInt;

import ru.reactiveturtle.reactivemusic.R;

public final class DefaultAlbumCover {
    public static BitmapDrawable get(Resources resources, @ColorInt int color) {
        int width = 512;
        int height = 512;
        Bitmap note = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_note),
                width / 2, height / 2, true);
        Canvas canvas = new Canvas(note);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawRect(0, 0, width, height, paint);

        paint.setXfermode(null);
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint.setColor(color);
        canvas.drawRoundRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                width / 8f, height / 8f, paint);
        canvas.drawBitmap(note, (bitmap.getWidth() - note.getWidth()) / 2f,
                (bitmap.getHeight() - note.getHeight()) / 2f, paint);

        return new BitmapDrawable(resources, bitmap);
    }
}
