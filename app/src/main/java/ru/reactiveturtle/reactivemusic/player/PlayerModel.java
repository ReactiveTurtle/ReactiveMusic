package ru.reactiveturtle.reactivemusic.player;

import androidx.annotation.Nullable;

public final class PlayerModel {
    private String currentMusicPath;
    private String currentPlaylistName;
    private boolean isTrackLooped;
    private boolean isPlayRandomTrack;

    public PlayerModel(@Nullable String currentMusicPath,
                       @Nullable String currentPlaylistName,
                       boolean isTrackLooped,
                       boolean isPlayRandomTrack) {
        this.currentMusicPath = currentMusicPath;
        this.currentPlaylistName = currentPlaylistName;
        this.isTrackLooped = isTrackLooped;
        this.isPlayRandomTrack = isPlayRandomTrack;
    }

    public String getCurrentMusicPath() {
        return currentMusicPath;
    }

    public String getCurrentPlaylistName() {
        return currentPlaylistName;
    }

    public boolean isTrackLooped() {
        return isTrackLooped;
    }

    public boolean isPlayRandomTrack() {
        return isPlayRandomTrack;
    }
}
