package ru.reactiveturtle.reactivemusic;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import ru.reactiveturtle.reactivemusic.player.Loaders;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.service.MusicService;

public class Helper {
    public static final String ACTION_EXTRA = "action_extra";

    @NonNull
    public static List<String> getAllTracksPathsInfo(@NonNull Context context) {
        String[] proj = {MediaStore.Audio.Media.DATA};
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        CursorLoader loader = new CursorLoader(context, uri, proj, MediaStore.Audio.Media.IS_MUSIC + " = 1",
                null, null);
        Cursor cursor = loader.loadInBackground();
        List<String> tracks = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    int pathColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);

                    String path = cursor.getString(pathColumnIndex);
                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                            Helper.getPathFormat(path));
                    if (mimeType != null) {
                        if (mimeType.equals("audio/mpeg") ||
                                mimeType.equals("audio/vnd.wave")) {
                            tracks.add(path);
                        }
                    }
                } while (cursor.moveToNext());
            }
        }
        return tracks;
    }

    public static String getPathFormat(@NonNull String path) {
        return path.substring(path.lastIndexOf(".") + 1);
    }

    public static Uri getArtUriFromMusicFile(Context context, String filePath) {
        final Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        final String[] cursor_cols = {MediaStore.Audio.Media.ALBUM_ID};

        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1 AND "
                + MediaStore.Audio.Media.DATA + "=?";
        final Cursor cursor = context.getApplicationContext().getContentResolver().query(uri, cursor_cols, where,
                new String[]{filePath}, null);
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

    public static BitmapDrawable getDrawable(ContentResolver contentResolver, Uri uri, int iconSize) throws FileNotFoundException {
        InputStream inputStream = contentResolver.openInputStream(uri);
        Drawable drawable = null;
        try {
            drawable = Drawable.createFromStream(inputStream, uri.toString());
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            drawable = new BitmapDrawable(Resources.getSystem(),
                    Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888));
        }
        if (drawable == null) {
            throw new FileNotFoundException();
        }
        return new BitmapDrawable(Resources.getSystem(), rectBitmap(drawableToBitmap(drawable)));
    }

    private static Bitmap rectBitmap(Bitmap src) {
        int size = Math.max(src.getWidth(), src.getHeight());
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawBitmap(src, (size - src.getWidth()) / 2f, (size - src.getHeight()) / 2f, paint);
        return Bitmap.createScaledBitmap(bitmap, 512, 512, false);
    }

    @NonNull
    public static Bitmap drawableToBitmap(@NonNull Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

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

    public static BitmapDrawable getDefaultAlbumCover(Resources resources) {
        Bitmap note = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(resources, R.drawable.ic_note),
                256, 256, true);
        Canvas canvas = new Canvas(note);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawRect(0, 0, 256, 256, paint);

        paint.setXfermode(null);
        Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        paint.setColor(Color.BLACK);
        canvas.drawRoundRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                64, 64, paint);
        canvas.drawBitmap(note, (bitmap.getWidth() - note.getWidth()) / 2f,
                (bitmap.getHeight() - note.getHeight()) / 2f, paint);

        return new BitmapDrawable(resources, bitmap);
    }

    public static void goToMainLooper(Runnable runnable) {
        new Handler(Looper.getMainLooper()).post(runnable);
    }

    public static boolean serviceIsRunning(@NonNull Context context, String className) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (className.equals(service.service.getClassName()))
                    return true;
            }
        }
        return false;
    }

    public static GradientDrawable getRoundDrawable(int color, float radius) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(color);
        gd.setCornerRadius(radius);
        return gd;
    }

    public static String code(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            char ch = string.charAt(i);
            if (i > 0) {
                result.append("\\");
            }
            result.append((int) ch);
        }
        return result.toString();
    }

    public static String decode(String string) {
        StringBuilder result = new StringBuilder();
        String[] chars = string.split("\\\\");
        for (String ch : chars) {
            result.append((char) Integer.parseInt(ch));
        }
        return result.toString();
    }

    public static BitmapDrawable getNavigationIcon(Resources resources, int id, int degrees) {
        float dip = 24f;
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dip,
                resources.getDisplayMetrics()
        );
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        Bitmap bitmap = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(resources, id), px, px, false);
        Bitmap result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        bitmap.recycle();
        BitmapDrawable drawable = new BitmapDrawable(resources, result);
        return drawable;
    }

    public static void initSelectMusic(AppCompatActivity activity, final DrawerLayout drawerLayout) {
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);

        drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                if (((DrawerLayout.LayoutParams) drawerView.getLayoutParams()).gravity == GravityCompat.END) {
                    if (((ViewPager2) drawerLayout.findViewById(R.id.selectMusicViewPager)).getCurrentItem() == 0) {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
                    } else {
                        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, GravityCompat.END);
                    }
                }
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                if (((DrawerLayout.LayoutParams) drawerView.getLayoutParams()).gravity == GravityCompat.END) {
                    drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, GravityCompat.END);
                }
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);

        NavigationView view = activity.findViewById(R.id.playerMusicNavigationView);
        DrawerLayout.LayoutParams layoutParams = (DrawerLayout.LayoutParams) view.getLayoutParams();
        layoutParams.width = dm.widthPixels;
        view.setLayoutParams(layoutParams);
    }

    public static MusicInfo getMusicInfo(Context context, String trackPath) {
        String[] proj = {MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE};
        Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        CursorLoader loader = new CursorLoader(context, uri, proj, MediaStore.Audio.Media.IS_MUSIC + " = 1 AND "
                + MediaStore.Audio.Media.DATA + "=?",
                new String[]{trackPath}, null);
        Cursor cursor = loader.loadInBackground();
        if (cursor != null && cursor.moveToFirst()) {
            try {
                return Loaders.MusicInfoLoader.getMusicInfo(cursor);
            } catch (SQLiteException e) {
                e.printStackTrace();
            }
        }
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(trackPath);
            String pathFormat = Helper.getPathFormat(trackPath);
            File file = new File(trackPath);
            String album = file.getParentFile() == null ? "Неизвестный альбом" : file.getParentFile().getName();
            return new MusicInfo(trackPath, album, "Неизвестный исполнитель",
                    file.getName().substring(0, file.getName().length() - pathFormat.length() - 1));
        } catch (IOException ignored) {
        }
        mediaPlayer.release();
        return null;
    }

    public static class ImageIconLoader extends AsyncTaskLoader<Bitmap> {
        private final int mIconSize;
        private final String mPath;

        public ImageIconLoader(@NonNull Context context, int iconSize, @NonNull String path) {
            super(context);
            mIconSize = iconSize;
            mPath = path;
        }

        @Nullable
        @Override
        public Bitmap loadInBackground() {
            return decodeSampledBitmapFromResource(mPath, mIconSize);
        }
    }

    public static Bitmap decodeSampledBitmapFromResource(String path, int iconSize) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        options.inSampleSize = calculateInSampleSize(options, iconSize, iconSize);

        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static String getFileSize(long length, Resources resources) {
        if (length <= 0) return "0 " + resources.getString(R.string.bytes);
        int[] endResources = new int[]{R.string.bytes, R.string.kilobytes,
                R.string.megabytes, R.string.gigabytes, R.string.terabytes, R.string.petabytes,
                R.string.exabytes, R.string.zetabytes, R.string.yotabytes};
        int digitGroups = (int) (Math.log10(length) / Math.log10(1024));
        long sizeM100 = Math.round(length * 100 / Math.pow(1024, digitGroups));
        String result = sizeM100 / 100 + "";
        if (sizeM100 % 100 > 0) {
            result += "." + sizeM100 % 100;
        }
        return String.format("%s %s", result,
                resources.getString(endResources[digitGroups]));
    }
}
