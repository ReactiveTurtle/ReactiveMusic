package ru.reactiveturtle.reactivemusic.player.shared;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.documentfile.provider.DocumentFile;

import ru.reactiveturtle.reactivemusic.player.MusicMetadata;

public class MusicMetadataFromDatabase {
    public static MusicMetadata load(Context context, String path) {
        Cursor cursor = getTrackCursor(context, path);
        if (cursor == null || !cursor.moveToFirst()) {
            if (cursor != null) {
                cursor.close();
            }
            return null;
        }
        DocumentFile documentFile = DocumentFile.fromSingleUri(context, Uri.parse(path));
        try {
            return new MusicMetadata(
                    getPathFromDatabase(cursor),
                    getAlbumNameFromDatabase(cursor),
                    getArtistNameFromDatabase(cursor),
                    getTitleFromDatabase(cursor));
        } catch (SQLiteException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
        return null;
    }

    private static String getPathFromDatabase(Cursor cursor) {
        int pathColumnIndex = cursor.getColumnIndexOrThrow(getPathColumn());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            long id = cursor.getLong(pathColumnIndex);
            return ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString();
        }
        return cursor.getString(pathColumnIndex);
    }

    private static String getAlbumNameFromDatabase(Cursor cursor) {
        int albumColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
        return cursor.getString(albumColumnIndex);
    }

    private static String getArtistNameFromDatabase(Cursor cursor) {
        int artistColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
        return cursor.getString(artistColumnIndex);
    }

    private static String getTitleFromDatabase(Cursor cursor) {
        int titleColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
        return cursor.getString(titleColumnIndex);
    }

    private static int getTrackDurationFromMetadata(MediaMetadataRetriever mediaMetadataRetriever) {
        String trackDurationAsString = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int trackDuration;
        if (trackDurationAsString != null) {
            trackDuration = Integer.parseInt(trackDurationAsString);
        } else {
            trackDuration = 0;
        }
        return trackDuration;
    }

    private static Cursor getTrackCursor(Context context, String path) {
        Uri collection = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                ? MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                : MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String pathColumn = getPathColumn();
        String[] projection = {
                pathColumn,
                MediaStore.Audio.AudioColumns.ALBUM,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.TITLE};

        String selection = String.format("%s = 1 AND %s = ?", MediaStore.Audio.Media.IS_MUSIC, pathColumn);
        return context.getContentResolver().query(
                collection,
                projection,
                selection,
                new String[]{
                        path
                },
                null);
    }

    private static String getPathColumn() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                MediaStore.Audio.Media._ID :
                MediaStore.Audio.Media.DATA;
    }
}
