package ru.reactiveturtle.reactivemusic.player.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import ru.reactiveturtle.reactivemusic.player.PlayerModel;
import ru.reactiveturtle.reactivemusic.toolkit.ReactiveList;

public interface PlayerRepositoryImpl {
    @NonNull
    PlayerModel getPlayerModel();

    void setCurrentTrackPath(@Nullable String path);

    @Nullable
    String getCurrentTrackPath();

    void setCurrentPlaylistName(String playlistName);

    @Nullable
    String getCurrentPlaylistName();

    void setTrackLooped(boolean isLooped);

    boolean isTrackLooped();

    void setPlayRandomTrack(boolean isPlayRandomTrack);

    boolean isPlayRandomTrack();

    boolean addPlaylist(@NonNull String name);

    void removePlaylist(@NonNull String playlistName);

    boolean renamePlaylist(@NonNull String oldName, @NonNull String newName);

    boolean addTrack(@NonNull String playlist, @NonNull String trackPath);

    void removeTrack(@NonNull String playlist, @NonNull String trackPath);

    @NonNull
    List<String> getPlaylistNames();

    @Nullable
    ReactiveList<String> getPlaylist(@Nullable String playlistName);
}
