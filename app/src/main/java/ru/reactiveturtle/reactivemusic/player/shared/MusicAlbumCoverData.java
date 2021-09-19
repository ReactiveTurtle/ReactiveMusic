package ru.reactiveturtle.reactivemusic.player.shared;

import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.NonNull;

import java.util.Objects;

public class MusicAlbumCoverData {
    private final boolean isOwnCover;
    private final BitmapDrawable cover;

    public MusicAlbumCoverData(boolean isOwnCover, @NonNull BitmapDrawable cover) {
        Objects.requireNonNull(cover);
        this.isOwnCover = isOwnCover;
        this.cover = cover;
    }

    public boolean isOwnCover() {
        return isOwnCover;
    }

    @NonNull
    public BitmapDrawable getCover() {
        return cover;
    }
}
