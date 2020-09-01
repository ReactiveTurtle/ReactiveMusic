package ru.reactiveturtle.reactivemusic.player.mvp.model;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.PlayerContract;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ColorSet;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;

public class PlayerRepository implements PlayerContract.Repository {
    private static final String REPOSITORY_NAME = "player_repository";
    private static final String SELECTED_MUSIC_FILE = "selected_music_file";
    private static final String CURRENT_PLAYLIST = "current_playlist";
    private static final String PLAYLISTS = "playlists";
    private static final String THEME_IS_DARK = "theme_is_dark";
    private static final String THEME_COLOR = "theme_color";

    private SharedPreferences preferences;
    private Context context;

    public PlayerRepository(Context context) {
        preferences = context.getSharedPreferences(REPOSITORY_NAME, 0);
        this.context = context;
    }

    private SharedPreferences.Editor getEditor() {
        return preferences.edit();
    }

    @Override
    public void setCurrentMusic(MusicInfo musicInfo) {
        getEditor().putString(SELECTED_MUSIC_FILE, musicInfo.getPath()).apply();
    }

    @Override
    public MusicInfo getCurrentMusic() {
        return new MusicInfo(preferences.getString(SELECTED_MUSIC_FILE, null));
    }

    @Override
    public void setCurrentPlaylist(String playlist) {
        getEditor().putString(CURRENT_PLAYLIST, playlist).apply();
    }

    @Override
    public String getCurrentPlaylist() {
        return preferences.getString(CURRENT_PLAYLIST, null);
    }

    @Override
    public void setThemeContextDark(boolean isDark) {
        getEditor().putBoolean(THEME_IS_DARK, isDark).apply();
    }

    @Override
    public boolean isThemeContextDark() {
        return preferences.getBoolean(THEME_IS_DARK, false);
    }

    @Override
    public void setThemeColorSet(@NonNull ColorSet colorSet) {
        getEditor().putString(THEME_COLOR, colorSet.toString()).apply();
    }

    @NonNull
    @Override
    public ColorSet getThemeColorSet() {
        String colorString = preferences.getString(THEME_COLOR, null);
        if (colorString == null) {
            colorString = Theme.getColors(5).get(5).toString();
        }
        return new ColorSet(colorString);
    }

    @Override
    public boolean addPlaylistName(@NonNull String name) {
        String src = preferences.getString(PLAYLISTS, null);
        if (src == null) {
            src = "";
        }
        List<String> playlists = getCodedPlaylists();
        String coded = Helper.code(name);
        boolean isAdded;
        if ((isAdded = !playlists.contains(coded))) {
            getEditor().putString(PLAYLISTS,
                    src + (src.length() > 0 ? ";" : "") + coded).apply();
        }
        return isAdded;
    }

    @Override
    public void removePlaylistName(@NonNull String playlist) {
        List<String> playlists = getCodedPlaylists();
        playlists.remove(Helper.code(playlist));
        getEditor().putString(PLAYLISTS, listToString(playlists)).apply();
        getEditor().putString(playlist, null).apply();
    }

    @Override
    public void updatePlaylist(@NonNull String playlist, @NonNull List<String> playlistTracks) {
        List<String> codedTracks = new ArrayList<>();
        for (String track : playlistTracks) {
            codedTracks.add(Helper.code(track));
        }
        getEditor().putString(playlist, listToString(codedTracks)).apply();
    }

    @Override
    public boolean renamePlaylist(@NonNull String oldName, @NonNull String newName) {
        String playlistString = preferences.getString(oldName, "");
        List<String> playlists = getPlaylists();
        int playlistIndex = getPlaylists().indexOf(oldName);
        if (!playlists.contains(newName)) {
            getEditor().putString(oldName, null).apply();

            List<String> codedPlaylists = getCodedPlaylists();
            codedPlaylists.set(playlistIndex, Helper.code(newName));
            getEditor().putString(PLAYLISTS, listToString(codedPlaylists)).apply();
            getEditor().putString(newName, playlistString).apply();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean addTrack(@NonNull String playlist, @NonNull String trackPath) {
        String src = preferences.getString(playlist, null);
        if (src == null) {
            src = "";
        }
        List<String> tracks = getCodedPlaylist(playlist);
        String coded = Helper.code(trackPath);
        boolean isAdded;
        if ((isAdded = !tracks.contains(coded))) {
            getEditor().putString(playlist,
                    src + (src.length() > 0 ? ";" : "") + coded).apply();
        }
        return isAdded;
    }

    @Override
    public void removeTrack(@NonNull String playlist, @NonNull String trackPath) {
        List<String> tracks = getCodedPlaylist(playlist);
        tracks.remove(Helper.code(trackPath));
        getEditor().putString(playlist, listToString(tracks)).apply();
    }

    @NonNull
    @Override
    public List<String> getPlaylists() {
        String playlists = preferences.getString(PLAYLISTS, null);
        String[] playlistsArray = playlists != null ? playlists.split(";") : new String[0];
        for (int i = 0; i < playlistsArray.length; i++) {
            playlistsArray[i] = Helper.decode(playlistsArray[i]);
        }
        return new ArrayList<>(Arrays.asList(playlistsArray));
    }

    @NonNull
    @Override
    public List<String> getCodedPlaylists() {
        String[] playlists = Objects.requireNonNull(preferences.getString(PLAYLISTS, "")).split(";");
        return new ArrayList<>(Arrays.asList(playlists));
    }

    @NonNull
    @Override
    public List<String> getPlaylist(String playlist) {
        if (playlist != null) {
            String tracks = preferences.getString(playlist, null);
            String[] tracksArray = tracks != null ? tracks.split(";") : new String[0];
            for (int i = 0; i < tracksArray.length; i++) {
                tracksArray[i] = Helper.decode(tracksArray[i]);
            }
            return new ArrayList<>(Arrays.asList(tracksArray));
        } else {
            System.out.println("Context: " + context);
            return Helper.getAllTracksPathsInfo(context);
        }
    }

    @NonNull
    @Override
    public List<String> getCodedPlaylist(@NonNull String playlist) {
        String[] tracks = Objects.requireNonNull(preferences.getString(playlist, "")).split(";");
        return new ArrayList<>(Arrays.asList(tracks));
    }

    @Override
    public void release() {
        context = null;
    }

    @Nullable
    private String listToString(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String string :
                list) {
            if (builder.length() > 0) {
                builder.append(";");
            }
            builder.append(string);
        }
        if (builder.length() > 0) {
            return builder.toString();
        } else {
            return null;
        }
    }
}
