package ru.reactiveturtle.reactivemusic.player;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.BitmapDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
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

import ru.reactiveturtle.reactivemusic.Helper;

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
            String[] proj = {MediaStore.Audio.Media.DATA};
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor cursor = getContext().getApplicationContext().getContentResolver().query(uri, proj,
                    MediaStore.Audio.Media.IS_MUSIC + " = 1",
                    null, null);
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
                cursor.close();
            }
            return tracks;
        }
    }

    public static class AlbumCoverLoader extends AsyncTaskLoader<BitmapDrawable> {
        private String trackPath;
        private BitmapDrawable defaultAlbumCover;

        public AlbumCoverLoader(@NonNull Context context, @NonNull String trackPath, BitmapDrawable defatultAlbumCover) {
            super(context);
            this.trackPath = trackPath;
            this.defaultAlbumCover = defatultAlbumCover;
        }

        @Nullable
        @Override
        public BitmapDrawable loadInBackground() {
            try {
                return getDrawable(getContext().getContentResolver(),
                        getArtUriFromMusicFile(getContext(), trackPath), 256);
            } catch (FileNotFoundException e) {
                return defaultAlbumCover;
            }
        }
    }

    public static class MusicInfoLoader extends AsyncTaskLoader<MusicInfo> {
        private String trackPath;

        public MusicInfoLoader(Context context, String trackPath) {
            super(context);
            this.trackPath = trackPath;
        }

        @Override
        public MusicInfo loadInBackground() {
            String[] proj = {MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.TITLE};
            Uri uri = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

            Cursor cursor = getContext().getApplicationContext().getContentResolver().query(uri, proj,
                    MediaStore.Audio.Media.IS_MUSIC + " = 1 AND "
                            + MediaStore.Audio.Media.DATA + "=?",
                    new String[]{trackPath}, null);
            String pathFormat = Helper.getPathFormat(trackPath);
            String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(pathFormat);
            MusicInfo musicInfo = null;
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    musicInfo = getMusicInfo(cursor);
                } catch (SQLiteException e) {
                    e.printStackTrace();
                }
                cursor.close();
            } else if (mimeType != null) {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(trackPath);
                    mediaPlayer.prepare();
                    mediaPlayer.release();
                    File file = new File(trackPath);
                    String album = file.getParentFile() == null ? "Неизвестный альбом" : file.getParentFile().getName();
                    musicInfo = new MusicInfo(trackPath, album, "Неизвестный исполнитель",
                            file.getName().substring(0, file.getName().length() - pathFormat.length() - 1));
                } catch (IOException ignored) {
                }
            }
            return musicInfo;
        }

        public static MusicInfo getMusicInfo(Cursor cursor) {
            int pathColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
            int albumColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
            int artistColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
            int titleColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);

            String path = cursor.getString(pathColumnIndex);
            return new MusicInfo(path,
                    cursor.getString(albumColumnIndex),
                    cursor.getString(artistColumnIndex),
                    cursor.getString(titleColumnIndex));
        }
    }
}
