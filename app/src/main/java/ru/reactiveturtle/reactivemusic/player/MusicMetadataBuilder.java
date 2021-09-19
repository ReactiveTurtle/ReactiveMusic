package ru.reactiveturtle.reactivemusic.player;

import java.util.Objects;

public final class MusicMetadataBuilder {
    private String path;

    public void setPath(String path) {
        Objects.requireNonNull(path);
        this.path = path;
    }

    private String album;

    public void setAlbum(String album) {
        this.album = album;
    }

    private String artist;

    public void setArtist(String artist) {
        this.artist = artist;
    }

    private String title;

    public void setTitle(String title) {
        this.title = title;
    }

    public MusicMetadata build() {
        return new MusicMetadata(
                path,
                album,
                artist,
                title
        );
    }
}
