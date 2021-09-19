package ru.reactiveturtle.reactivemusic.player;

import androidx.annotation.Nullable;

import ru.reactiveturtle.reactivemusic.player.shared.MusicAlbumCoverData;

public class MusicData {
    private final MusicMetadata metadata;
    private final MusicAlbumCoverData coverData;
    private final int duration;

    public MusicData(@Nullable MusicMetadata metadata,
                     @Nullable MusicAlbumCoverData coverData,
                     int duration) {
        this.metadata = metadata;
        this.coverData = coverData;
        this.duration = duration;
    }

    @Nullable
    public MusicMetadata getMetadata() {
        return metadata;
    }

    @Nullable
    public MusicAlbumCoverData getCoverData() {
        return coverData;
    }

    public int getDuration() {
        return duration;
    }
}
