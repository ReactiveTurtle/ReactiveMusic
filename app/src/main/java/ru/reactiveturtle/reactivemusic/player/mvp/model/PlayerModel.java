package ru.reactiveturtle.reactivemusic.player.mvp.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.reactiveturtle.reactivemusic.player.service.MusicModel;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;

public class PlayerModel {
    public static final String IS_ACTIVITY_ACTIVE = "IS_ACTIVITY_ACTIVE";
    public static final String SEEKBAR_TRACK_PROGRESS = "SEEKBAR_TRACK_PROGRESS";
    public static final String IS_PLAY = "IS_PLAY";
    public static final String IS_TRACK_LOOPING = "IS_TRACK_LOOPING";
    public static final String IS_PLAY_RANDOM_TRACK = "IS_PLAY_RANDOM_TRACK";
    public static final String PLAYLISTS = "PLAYLISTS";
    public static final String OPEN_PLAYLIST_NAME = "OPEN_PLAYLIST_NAME";
    public static final String OPEN_PLAYLIST = "OPEN_PLAYLIST";
    public static final String SELECTED_PAGE = "SELECTED_PAGE";
    public static final String PLAYLIST_ACTIONS_PLAYLIST_NAME = "ACTIONS_PLAYLIST_NAME";

    public static void initialize(PlayerRepository repository) {
        ReactiveArchitect.createState(IS_ACTIVITY_ACTIVE, false);
        ReactiveArchitect.createState(SEEKBAR_TRACK_PROGRESS, 0);
        ReactiveArchitect.createState(IS_PLAY, false);
        ReactiveArchitect.createState(IS_TRACK_LOOPING, repository.isTrackLooping());
        ReactiveArchitect.createState(IS_PLAY_RANDOM_TRACK, repository.isPlayRandomTrack());
        ReactiveArchitect.createState(PLAYLISTS, new ArrayList<>());
        ReactiveArchitect.createState(OPEN_PLAYLIST_NAME, null);
        ReactiveArchitect.createState(OPEN_PLAYLIST, new ArrayList<>());
        ReactiveArchitect.createState(SELECTED_PAGE, 0);
        ReactiveArchitect.createState(PLAYLIST_ACTIONS_PLAYLIST_NAME, null);
    }

    public static void setActivityActive(boolean isActivityActive) {
        ReactiveArchitect.changeState(IS_ACTIVITY_ACTIVE, isActivityActive);
    }

    public static boolean isActivityActive() {
        return (boolean) ReactiveArchitect.getStateKeeper(IS_ACTIVITY_ACTIVE).getState();
    }

    public static int getSeekBarTrackProgress() {
        return (int) ReactiveArchitect.getStateKeeper(SEEKBAR_TRACK_PROGRESS).getState();
    }

    public static void switchPlayPause() {
        setPlay(!(Boolean) ReactiveArchitect.getStateKeeper(MusicModel.IS_TRACK_PLAY).getState());
    }

    public static void setPlay(boolean isPlay) {
        ReactiveArchitect.changeState(IS_PLAY, isPlay);
    }

    public static int isPlay() {
        return (int) ReactiveArchitect.getStateKeeper(IS_PLAY).getState();
    }

    public static void setSeekBarTrackProgress(int seekBarTrackProgress) {
        ReactiveArchitect.changeState(SEEKBAR_TRACK_PROGRESS, seekBarTrackProgress);
    }

    public static void setTrackLooping(boolean isLooping) {
        ReactiveArchitect.changeState(IS_TRACK_LOOPING, isLooping);
    }

    public static boolean isTrackLooping() {
        return (boolean) ReactiveArchitect.getStateKeeper(IS_TRACK_LOOPING).getState();
    }

    public static void setPlayRandomTrack(boolean isPlay) {
        ReactiveArchitect.changeState(IS_PLAY_RANDOM_TRACK, isPlay);
    }

    public static boolean isPlayRandomTrack() {
        return (boolean) ReactiveArchitect.getStateKeeper(IS_PLAY_RANDOM_TRACK).getState();
    }

    public static void setOpenPlaylistName(@Nullable String playlist) {
        ReactiveArchitect.changeState(OPEN_PLAYLIST_NAME, playlist);
    }

    public static void setPlaylists(List<String> playlists) {
        ReactiveArchitect.changeState(PLAYLISTS, playlists);
    }

    public static void setOpenPlaylist(@Nullable List<String> playlist) {
        ReactiveArchitect.changeState(OPEN_PLAYLIST, playlist);
    }

    @Nullable
    public static String getOpenPlaylistName() {
        return (String) ReactiveArchitect.getStateKeeper(OPEN_PLAYLIST_NAME).getState();
    }

    public static void setSelectedPage(int page) {
        ReactiveArchitect.changeState(SELECTED_PAGE, page);
    }

    public static int getSelectedPage() {
        return (int) ReactiveArchitect.getStateKeeper(SELECTED_PAGE).getState();
    }

    public static void setPlaylistActionsPlaylistName(String playlistName) {
        ReactiveArchitect.changeState(PLAYLIST_ACTIONS_PLAYLIST_NAME, playlistName);
    }

    public static String getPlaylistActionsPlaylistName() {
        return (String) ReactiveArchitect.getStateKeeper(PLAYLIST_ACTIONS_PLAYLIST_NAME).getState();
    }
}
