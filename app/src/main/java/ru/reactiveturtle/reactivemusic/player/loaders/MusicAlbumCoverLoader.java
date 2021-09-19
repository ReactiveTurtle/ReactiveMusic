package ru.reactiveturtle.reactivemusic.player.loaders;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.DisplayMetrics;

import java.io.IOException;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.theme.Theme;

import static ru.reactiveturtle.reactivemusic.toolkit.BitmapExtensions.getSquareBitmap;
import static ru.reactiveturtle.reactivemusic.toolkit.BitmapExtensions.loadSquareDrawableOrDefault;

public final class MusicAlbumCoverLoader {
    private MusicAlbumCoverLoader() {
    }

    public static BitmapDrawable loadBackgroundAlbumCover(
            BitmapDrawable musicCover,
            Theme theme,
            Resources resources) {
        Bitmap bitmap;
        Canvas canvas;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (musicCover == null) {
            bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
            canvas = new Canvas(bitmap);
            paint.setShader(new RadialGradient(0, 0, 512,
                    new int[]{
                            theme.getColorSet().getPrimaryDark(),
                            theme.getColorSet().getPrimaryDark(),
                            theme.isDark() ? theme.getThemeContext().getPrimaryLight() : theme.getThemeContext().getPrimary()
                    },
                    new float[]{
                            0, 0.5f, 1
                    }, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);

            paint.setShader(new RadialGradient(256, 256, 256,
                    new int[]{
                            theme.getColorSet().getPrimary(),
                            Color.TRANSPARENT
                    },
                    new float[]{
                            0, 1
                    }, Shader.TileMode.CLAMP));
            canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);
            paint.setShader(null);
        } else {
            bitmap = musicCover.getBitmap().copy(Bitmap.Config.ARGB_8888, true);
        }
        DisplayMetrics dm = resources.getDisplayMetrics();
        Bitmap back;
        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            float aspectRatio = dm.widthPixels / (float) dm.heightPixels;
            back = Bitmap.createBitmap((int) (bitmap.getHeight() * aspectRatio),
                    bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            float aspectRatio = (float) dm.heightPixels / dm.widthPixels;
            back = Bitmap.createBitmap(bitmap.getWidth(),
                    (int) (bitmap.getHeight() * aspectRatio), Bitmap.Config.ARGB_8888);
        }
        canvas = new Canvas(back);
        canvas.drawBitmap(bitmap, (back.getWidth() - bitmap.getWidth()) / 2f,
                (back.getHeight() - bitmap.getHeight()) / 2f, paint);
        int color = Theme.setAlpha("6a", theme.isDark() ?
                theme.getThemeContext().getPrimaryLight() : theme.getThemeContext().getPrimary());
        paint.setColor(color);
        canvas.drawRect(0, 0, back.getWidth(), back.getHeight(), paint);
        return new BitmapDrawable(resources, Helper.fastblur(back, 1, 16));
    }

    public static BitmapDrawable load(Context context, String path) {
        try {
            return loadSquareDrawableOrDefault(
                    context.getContentResolver(),
                    getArtUriFromMusicFile(context.getContentResolver(), path),
                    512);
        } catch (IOException ignored) {
        }
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mmr.setDataSource(context, Uri.parse(path));
            } else {
                mmr.setDataSource(path);
            }
            byte[] data = mmr.getEmbeddedPicture();
            mmr.release();
            if (data != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                BitmapDrawable bitmapDrawable = new BitmapDrawable(Resources.getSystem(), getSquareBitmap(bitmap, 512));
                bitmap.recycle();
                return bitmapDrawable;
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static Uri getArtUriFromMusicFile(ContentResolver contentResolver, String path) {
        String trackPath = path;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            trackPath = path.substring(path.lastIndexOf("/") + 1);
        }
        Uri collection;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        final String[] cursor_cols = {MediaStore.Audio.Media.ALBUM_ID};

        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                MediaStore.Audio.Media._ID :
                MediaStore.Audio.Media.DATA) + "=?";
        final Cursor cursor = contentResolver.query(collection, cursor_cols, where,
                new String[]{trackPath}, null);
        /*
         * If the cusor count is greater than 0 then parse the data and get the art id.
         */
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));

            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
            cursor.close();
            return albumArtUri;
        }
        return Uri.EMPTY;
    }
}
