package ru.reactiveturtle.reactivemusic.player;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.tools.BaseAsyncTask;

import static ru.reactiveturtle.reactivemusic.Helper.getArtUriFromMusicFile;
import static ru.reactiveturtle.reactivemusic.Helper.getDrawable;

public abstract class Loaders {
    public static class TracksPathsLoader extends AsyncTaskLoader<List<String>> {
        public TracksPathsLoader(Context context) {
            super(context);
        }

        @NonNull
        @Override
        public List<String> loadInBackground() {
            return Helper.getAllTracksPathsInfo(getContext());
        }
    }

    public static class AlbumCoverLoader extends BaseAsyncTask<BitmapDrawable> {
        private String trackPath;
        private BitmapDrawable defaultAlbumCover;

        public AlbumCoverLoader(@NonNull Context context, @NonNull String trackPath, BitmapDrawable defatultAlbumCover) {
            super(context);
            this.trackPath = trackPath;
            this.defaultAlbumCover = defatultAlbumCover;
        }

        @Nullable
        public BitmapDrawable doInBackground(Void... voids) {
            try {
                return getDrawable(getContext().getContentResolver(),
                        getArtUriFromMusicFile(getContext(), trackPath), 256);
            } catch (FileNotFoundException e) {
                try {
                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        mmr.setDataSource(getContext(), Uri.parse(trackPath));
                    } else {
                        mmr.setDataSource(trackPath);
                    }
                    byte[] data = mmr.getEmbeddedPicture();
                    mmr.release();
                    if (data != null) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        return new BitmapDrawable(Resources.getSystem(), Helper.rectBitmap(bitmap));
                    }
                } catch (Exception ignored) {
                }
            }
            return defaultAlbumCover;
        }

        @Override
        protected void onPostExecute(BitmapDrawable bitmapDrawable) {
            super.onPostExecute(bitmapDrawable);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }

    public static class MusicInfoLoader extends BaseAsyncTask<MusicInfo> {
        @NonNull
        private String trackPath;

        public MusicInfoLoader(@NonNull Context context, @NonNull String trackPath) {
            super(context);
            Objects.requireNonNull(trackPath);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                this.trackPath = trackPath.substring(trackPath.lastIndexOf("/") + 1);
            } else {
                this.trackPath = trackPath;
            }
        }

        @Nullable
        public MusicInfo doInBackground(Void... voids) {
            String[] proj = {Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                    MediaStore.Audio.Media._ID :
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.AudioColumns.ALBUM,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.TITLE};
            Uri collection;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            } else {
                collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }

            Cursor cursor = getContext().getContentResolver().query(collection, proj,
                    MediaStore.Audio.Media.IS_MUSIC + " = 1 AND "
                            + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                            MediaStore.Audio.Media._ID :
                            MediaStore.Audio.Media.DATA) + "=?",
                    new String[]{trackPath}, null);
            MusicInfo musicInfo = null;
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    musicInfo = getMusicInfo(getContext(), cursor);
                } catch (SQLiteException e) {
                    e.printStackTrace();
                }
                cursor.close();
            } else {
                musicInfo = getMusicInfo(trackPath);
            }
            return musicInfo;
        }

        @Override
        protected void onPostExecute(MusicInfo musicInfo) {
            super.onPostExecute(musicInfo);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        public static MusicInfo getMusicInfo(Context context, Cursor cursor) {
            int pathColumnIndex = cursor.getColumnIndexOrThrow(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                    MediaStore.Audio.Media._ID :
                    MediaStore.Audio.Media.DATA);
            int albumColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int artistColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int titleColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);

            String path;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                long id = cursor.getLong(pathColumnIndex);
                path = ContentUris.withAppendedId(
                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString();
            } else {
                path = cursor.getString(pathColumnIndex);
            }
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.Q && !new File(path).exists()) {
                return null;
            }
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mmr.setDataSource(context, Uri.parse(path));
            } else {
                mmr.setDataSource(path);
            }
            int trackDuration = 0;
            try {
                trackDuration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                mmr.release();
            } catch (NumberFormatException ignored) {

            }
            mmr.release();
            return new MusicInfo(path,
                    cursor.getString(albumColumnIndex),
                    cursor.getString(artistColumnIndex),
                    cursor.getString(titleColumnIndex),
                    trackDuration);
        }

        @Nullable
        public static MusicInfo getMusicInfo(String filePath) {
            try {
                if (!new File(filePath).exists()) {
                    return null;
                }
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(filePath);
                String mimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
                if (mimeType != null && mimeType.startsWith("audio")) {
                    String albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
                    if (albumName == null) {
                        albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE);
                    }
                    String artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                    if (artistName == null) {
                        artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
                    }
                    String trackName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                    if (trackName == null) {
                        File file = new File(filePath);
                        int lastIndex = file.getName().lastIndexOf('.');
                        if (lastIndex == -1) {
                            lastIndex = file.getName().length();
                        }
                        trackName = file.getName().substring(0, lastIndex);
                    }
                    int trackDuration = 0;
                    try {
                        trackDuration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
                        mmr.release();
                    } catch (NumberFormatException ignored) {

                    }
                    return new MusicInfo(filePath,
                            albumName, artistName, trackName,
                            trackDuration);
                }
            } catch (Exception ignored) {
            }
            return null;
        }
    }
}
