package ru.reactiveturtle.reactivemusic.player.shared;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import com.google.android.gms.common.api.UnsupportedApiCallException;

import java.io.File;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.net.URI;

import ru.reactiveturtle.reactivemusic.player.MusicMetadata;
import ru.reactiveturtle.reactivemusic.toolkit.UnsupportedApiException;

public final class MusicMetadataFromMediaMetadataRetreiver {
    private MusicMetadataFromMediaMetadataRetreiver() {
    }

    @Nullable
    public static MusicMetadata load(Context context, String musicFilePath) {
        try {
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                uri = Uri.parse(musicFilePath);
            } else {
                uri = Uri.fromFile(new File(musicFilePath));
            }
            DocumentFile documentFile = DocumentFile.fromSingleUri(context, uri);
            if (documentFile == null || !documentFile.exists()) {
                return null;
            }
            ParcelFileDescriptor parcelFileDescriptor = context.getContentResolver().openFileDescriptor(documentFile.getUri(), "r");
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(parcelFileDescriptor.getFileDescriptor());
            String mimeType = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            if (mimeType != null && mimeType.startsWith("audio")) {
                String albumName = getAlbum(mmr);
                String artistName = getArtist(mmr);
                String trackName = getTrackName(context.getContentResolver(), documentFile.getUri(), mmr);
                int trackDuration = getTrackDuration(mmr);
                return new MusicMetadata(
                        musicFilePath,
                        albumName,
                        artistName,
                        trackName
                );
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String getAlbum(MediaMetadataRetriever mediaMetadataRetriever) {
        return mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST);
    }

    private static String getArtist(MediaMetadataRetriever mediaMetadataRetriever) {
        String artistName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        if (artistName == null) {
            artistName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUTHOR);
        }
        return artistName;
    }

    private static String getTrackName(ContentResolver contentResolver,
                                       Uri uri,
                                       MediaMetadataRetriever mediaMetadataRetriever) {
        String trackName = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        if (trackName == null) {
            String trackFileName = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Cursor cursor = contentResolver.query(uri, new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null);
                if (cursor != null) {
                    try {
                        if (cursor.moveToFirst()) {
                            trackFileName = cursor.getString(0);
                        }
                    } finally {
                        cursor.close();
                    }
                }
            }
            if (trackFileName == null) {
                trackFileName = new File(uri.toString()).getName();
            }
            int lastIndex = trackFileName.lastIndexOf('.');
            if (lastIndex == -1) {
                lastIndex = trackFileName.length();
            }
            trackName = trackFileName.substring(0, lastIndex);
        }
        return trackName;
    }


    private static int getTrackDuration(MediaMetadataRetriever mediaMetadataRetriever) {
        String trackDurationAsString = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        int trackDuration;
        if (trackDurationAsString != null) {
            trackDuration = Integer.parseInt(trackDurationAsString);
        } else {
            trackDuration = 0;
        }
        return trackDuration;
    }
}
