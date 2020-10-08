package ru.reactiveturtle.reactivemusic.player;

import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.reactiveturtle.reactivemusic.R;

public class MusicInfo {
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

    private String mPath;
    private BitmapDrawable mAlbumImage;
    private String mAlbum;
    private String mArtist;
    private String mTitle;
    private int mDuration;

    public MusicInfo(String currentMusicPath) {
        mPath = currentMusicPath;
    }

    public MusicInfo(@NonNull String currentMusicPath, String album,
                     String artist, String title, int duration) {
        mPath = currentMusicPath;
        mAlbum = album;
        mArtist = artist;
        mTitle = title;
        if (mAlbum == null || mAlbum.equals("<unknown>")) {
            mAlbum = UNKNOWN_ALBUM;
        }
        if (mArtist == null || mArtist.equals("<unknown>")) {
            mArtist = UNKNOWN_ARTIST;
        }
        if (mTitle == null || mTitle.equals("<unknown>")) {
            mTitle = UNKNOWN_TRACK;
        }
        mDuration = duration;
    }

    public MusicInfo(@NonNull String currentMusicPath, int duration) {
        mPath = currentMusicPath;
        mDuration = duration;
    }

    public static MusicInfo getDefault() {
        return new MusicInfo(TRACKS_NOT_FOUND, TRACKS_NOT_FOUND,
                TRACKS_NOT_FOUND, TRACKS_NOT_FOUND, 0);
    }

    public static MusicInfo getEmpty() {
        return new MusicInfo("", "", "", "", -1);
    }

    public void setPath(@Nullable String path) {
        this.mPath = path;
    }

    @Nullable
    public String getPath() {
        return mPath;
    }

    public void setAlbumImage(BitmapDrawable albumImage) {
        mAlbumImage = albumImage;
    }

    public BitmapDrawable getAlbumImage() {
        return mAlbumImage;
    }

    public void setAlbum(@Nullable String album) {
        mAlbum = album == null ? UNKNOWN_ALBUM : album;
    }

    @NonNull
    public String getAlbum() {
        return mAlbum;
    }

    public void setArtist(@Nullable String artist) {
        mArtist = artist == null ? UNKNOWN_ARTIST : artist;
    }

    @NonNull
    public String getArtist() {
        return mArtist;
    }

    public void setTitle(@Nullable String title) {
        mTitle = title == null ? UNKNOWN_TRACK : title;
    }

    @NonNull
    public String getTitle() {
        return mTitle;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public int getDuration() {
        return mDuration;
    }

    @Override
    public String toString() {
        return "MusicInfo{" +
                "mPath='" + mPath + '\'' +
                ", mAlbumImage=" + mAlbumImage +
                ", mAlbum='" + mAlbum + '\'' +
                ", mArtist='" + mArtist + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mDuration=" + mDuration +
                '}';
    }
}
