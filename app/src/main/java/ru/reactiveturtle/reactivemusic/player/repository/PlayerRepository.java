package ru.reactiveturtle.reactivemusic.player.repository;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;

import ru.reactiveturtle.reactivemusic.musicservice.Repository;
import ru.reactiveturtle.reactivemusic.player.PlayerModel;
import ru.reactiveturtle.reactivemusic.toolkit.JsonExtensions;
import ru.reactiveturtle.reactivemusic.toolkit.ReactiveList;

public class PlayerRepository extends Repository implements PlayerRepositoryImpl {
    private static final String SELECTED_MUSIC_FILE = "SELECTED_MUSIC_FILE";
    private static final String CURRENT_PLAYLIST = "CURRENT_PLAYLIST";
    private static final String IS_TRACK_LOOPED = "IS_LOOPED_TRACK";
    private static final String IS_PLAY_RANDOM_TRACK = "IS_PLAY_RANDOM_TRACK";

    private static final String PLAYLISTS = "PLAYLISTS";

    public PlayerRepository(Context context) {
        super("player_repository", context);
        getPlayerModel();
    }

    private PlayerModel playerModel;

    @NonNull
    @Override
    public PlayerModel getPlayerModel() {
        if (playerModel == null) {
            playerModel = new PlayerModel(
                    getCurrentTrackPath(),
                    getCurrentPlaylistName(),
                    isTrackLooped(),
                    isPlayRandomTrack()
            );
        }
        return playerModel;
    }

    @Override
    public void setCurrentTrackPath(@Nullable String path) {
        getEditor().putString(SELECTED_MUSIC_FILE, path).apply();
        playerModel = new PlayerModel(
                path,
                playerModel.getCurrentPlaylistName(),
                playerModel.isTrackLooped(),
                playerModel.isPlayRandomTrack());
    }

    @Nullable
    @Override
    public String getCurrentTrackPath() {
        return getPreferences().getString(SELECTED_MUSIC_FILE, null);
    }

    @Override
    public void setCurrentPlaylistName(String playlistName) {
        getEditor().putString(CURRENT_PLAYLIST, playlistName).apply();
        playerModel = new PlayerModel(
                playerModel.getCurrentMusicPath(),
                playlistName,
                playerModel.isTrackLooped(),
                playerModel.isPlayRandomTrack());
    }

    @Nullable
    @Override
    public String getCurrentPlaylistName() {
        return getPreferences().getString(CURRENT_PLAYLIST, null);
    }

    @Override
    public void setTrackLooped(boolean isLooped) {
        getEditor().putBoolean(IS_TRACK_LOOPED, isLooped).apply();
        playerModel = new PlayerModel(
                playerModel.getCurrentMusicPath(),
                playerModel.getCurrentPlaylistName(),
                isLooped,
                playerModel.isPlayRandomTrack());
    }

    @Override
    public boolean isTrackLooped() {
        return getPreferences().getBoolean(IS_TRACK_LOOPED, false);
    }

    @Override
    public void setPlayRandomTrack(boolean isPlayRandomTrack) {
        getEditor().putBoolean(IS_PLAY_RANDOM_TRACK, isPlayRandomTrack).apply();
        playerModel = new PlayerModel(
                playerModel.getCurrentMusicPath(),
                playerModel.getCurrentPlaylistName(),
                playerModel.isTrackLooped(),
                isPlayRandomTrack);
    }

    @Override
    public boolean isPlayRandomTrack() {
        return getPreferences().getBoolean(IS_PLAY_RANDOM_TRACK, false);
    }

    @Override
    public boolean addPlaylist(@NonNull String name) {
        Objects.requireNonNull(name);
        List<String> playlists = getPlaylistNames();
        boolean isAdd = playlists.contains(name);
        if (isAdd) {
            getEditor().putString(PLAYLISTS, JsonExtensions.serializeList(playlists)).apply();
        }
        return isAdd;
    }

    @Override
    public void removePlaylist(@NonNull String playlistName) {
        Objects.requireNonNull(playlistName);
        List<String> playlists = getPlaylistNames();
        playlists.remove(playlistName);
        getEditor().putString(PLAYLISTS, JsonExtensions.serializeList(playlists)).apply();
        getEditor().putString(playlistName, null).apply();
    }

    @Override
    public boolean renamePlaylist(@NonNull String oldName, @NonNull String newName) {
        String playlistString = getPreferences().getString(oldName, null);
        List<String> playlists = getPlaylistNames();
        int playlistIndex = playlists.indexOf(oldName);
        if (!playlists.contains(newName)) {
            getEditor().putString(oldName, null).apply();

            playlists.set(playlistIndex, newName);
            getEditor().putString(PLAYLISTS, JsonExtensions.serializeList(playlists)).apply();
            getEditor().putString(newName, playlistString).apply();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addTrack(@NonNull String playlist, @NonNull String trackPath) {
        List<String> tracks = getPlaylist(playlist);
        if (tracks == null) {
            throw new IllegalArgumentException(String.format("Playlist \"%s\" not exists.", playlist));
        }
        Objects.requireNonNull(trackPath);
        boolean isAdded;
        if ((isAdded = !tracks.contains(trackPath))) {
            tracks.add(trackPath);
            getEditor().putString(playlist, JsonExtensions.serializeList(tracks)).apply();
        }
        return isAdded;
    }

    @Override
    public void removeTrack(@NonNull String playlist, @NonNull String trackPath) {
        List<String> tracks = getPlaylist(playlist);
        if (tracks == null) {
            throw new IllegalArgumentException(String.format("Playlist \"%s\" not exists.", playlist));
        }
        Objects.requireNonNull(trackPath);
        tracks.remove(trackPath);
        getEditor().putString(playlist, JsonExtensions.serializeList(tracks)).apply();
    }

    @NonNull
    @Override
    public List<String> getPlaylistNames() {
        return JsonExtensions.deserializeList(getPreferences().getString(PLAYLISTS, "[]"));
    }

    @Nullable
    @Override
    public ReactiveList<String> getPlaylist(@Nullable String playlistName) {
        if (playlistName != null) {
            String tracks = getPreferences().getString(playlistName, null);
            return tracks != null ? JsonExtensions.deserializeList(tracks) : null;
        }
        return null;
    }
}
