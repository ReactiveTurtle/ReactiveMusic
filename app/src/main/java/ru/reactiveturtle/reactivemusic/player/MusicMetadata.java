package ru.reactiveturtle.reactivemusic.player;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import ru.reactiveturtle.reactivemusic.R;

public class MusicMetadata {
    public static String UNKNOWN_ALBUM;
    public static String UNKNOWN_ARTIST;
    public static String UNKNOWN_TRACK;
    public static String TRACKS_NOT_FOUND;

    public static void initDefault(Resources resources) {
        UNKNOWN_ALBUM = resources.getString(R.string.unknown_album);
        UNKNOWN_ARTIST = resources.getString(R.string.unknown_artist);
        UNKNOWN_TRACK = resources.getString(R.string.unknown_track);
        TRACKS_NOT_FOUND = resources.getString(R.string.tracks_not_found);
    }

    public static MusicMetadata notFound() {
        return new MusicMetadata(
                TRACKS_NOT_FOUND,
                TRACKS_NOT_FOUND,
                TRACKS_NOT_FOUND,
                TRACKS_NOT_FOUND
        );
    }

    public static MusicMetadata empty() {
        return new MusicMetadata("", "", "", "");
    }

    private String path;
    private String album;
    private String artist;
    private String title;

    public MusicMetadata(@NonNull String musicPath,
                         String album,
                         String artist,
                         String title) {
        Objects.requireNonNull(musicPath);
        this.path = musicPath;
        this.album = album;
        this.artist = artist;
        this.title = title;
        if (this.album == null || this.album.equals("<unknown>")) {
            this.album = UNKNOWN_ALBUM;
        }
        if (this.artist == null || this.artist.equals("<unknown>")) {
            this.artist = UNKNOWN_ARTIST;
        }
        if (this.title == null || this.title.equals("<unknown>")) {
            this.title = UNKNOWN_TRACK;
        }
    }

    @NonNull
    public String getPath() {
        return path;
    }

    @NonNull
    public String getAlbum() {
        return album;
    }

    @NonNull
    public String getArtist() {
        return artist;
    }

    @NonNull
    public String getTitle() {
        return title;
    }

    public void release() {
        if (path != null) {
            path = null;
        }
        if (album != null) {
            album = null;
        }
        if (artist != null) {
            artist = null;
        }
        if (title != null) {
            title = null;
        }
    }
}
