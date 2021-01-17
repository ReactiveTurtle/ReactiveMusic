package ru.reactiveturtle.reactivemusic.player.service;

import android.graphics.drawable.BitmapDrawable;

import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;

public class MusicModel {
    public static final String CURRENT_TRACK_PATH = "CURRENT_TRACK_PATH";
    public static final String CURRENT_TRACK_NAME = "CURRENT_TRACK_NAME";
    public static final String CURRENT_TRACK_ARTIST = "CURRENT_TRACK_ARTIST";
    public static final String CURRENT_TRACK_ALBUM = "CURRENT_TRACK_ALBUM";
    public static final String CURRENT_TRACK_DURATION = "CURRENT_TRACK_DURATION";
    public static final String CURRENT_TRACK_PROGRESS = "CURRENT_TRACK_PROGRESS";
    public static final String CURRENT_TRACK_COVER = "CURRENT_TRACK_COVER";

    public static final String IS_TRACK_PLAY = "IS_TRACK_PLAY";

    private static boolean isInitialized = false;

    public static void initialize() {
        MusicInfo defaultMusicInfo = MusicInfo.getDefault();
        ReactiveArchitect.createState(CURRENT_TRACK_PATH, defaultMusicInfo.getPath());
        ReactiveArchitect.createState(CURRENT_TRACK_NAME, defaultMusicInfo.getTitle());
        ReactiveArchitect.createState(CURRENT_TRACK_ARTIST, defaultMusicInfo.getArtist());
        ReactiveArchitect.createState(CURRENT_TRACK_ALBUM, defaultMusicInfo.getAlbum());
        ReactiveArchitect.createState(CURRENT_TRACK_DURATION, defaultMusicInfo.getDuration());
        ReactiveArchitect.createState(CURRENT_TRACK_PROGRESS, 0);
        ReactiveArchitect.createState(CURRENT_TRACK_COVER, Theme.getDefaultAlbumCoverCopy());

        ReactiveArchitect.createState(IS_TRACK_PLAY, false);

        isInitialized = true;
    }

    public static boolean isInitialized() {
        return isInitialized;
    }

    public static void setCurrentTrackPath(String path) {
        ReactiveArchitect.changeState(CURRENT_TRACK_PATH, path);
    }

    public static String getCurrentTrackPath() {
        return (String) ReactiveArchitect.getStateKeeper(CURRENT_TRACK_PATH).getState();
    }

    public static void setCurrentTrackName(String currentTrackName) {
        ReactiveArchitect.changeState(CURRENT_TRACK_NAME, currentTrackName);
    }

    public static String getCurrentTrackName() {
        return (String) ReactiveArchitect.getStateKeeper(CURRENT_TRACK_NAME).getState();
    }

    public static void setCurrentTrackArtist(String currentTrackArtist) {
        ReactiveArchitect.changeState(CURRENT_TRACK_ARTIST, currentTrackArtist);
    }

    public static String getCurrentTrackArtist() {
        return (String) ReactiveArchitect.getStateKeeper(CURRENT_TRACK_ARTIST).getState();
    }

    public static void setCurrentTrackAlbum(String currentTrackAlbum) {
        ReactiveArchitect.changeState(CURRENT_TRACK_ALBUM, currentTrackAlbum);
    }

    public static String getCurrentTrackAlbum() {
        return (String) ReactiveArchitect.getStateKeeper(CURRENT_TRACK_ALBUM).getState();
    }

    public static void setCurrentTrackDuration(int currentTrackDuration) {
        ReactiveArchitect.changeState(CURRENT_TRACK_DURATION, currentTrackDuration);
    }

    public static int getCurrentTrackDuration() {
        return (int) ReactiveArchitect.getStateKeeper(CURRENT_TRACK_DURATION).getState();
    }

    public static void setCurrentTrackProgress(int currentTrackProgress) {
        ReactiveArchitect.changeState(CURRENT_TRACK_PROGRESS, currentTrackProgress);
    }

    public static int getCurrentTrackProgress() {
        return (int) ReactiveArchitect.getStateKeeper(CURRENT_TRACK_PROGRESS).getState();
    }

    public static void setCurrentTrackCover(BitmapDrawable currentTrackCover) {
        ReactiveArchitect.changeState(CURRENT_TRACK_COVER, currentTrackCover);
    }

    public static BitmapDrawable getCurrentTrackCover() {
        return (BitmapDrawable) ReactiveArchitect.getStateKeeper(CURRENT_TRACK_COVER).getState();
    }

    public static void switchTrackPlayPause() {
        setTrackPlay(!isTrackPlay());
    }

    public static void setTrackPlay(boolean isPlay) {
        ReactiveArchitect.changeState(IS_TRACK_PLAY, isPlay);
    }

    public static boolean isTrackPlay() {
        return (boolean) ReactiveArchitect.getStateKeeper(IS_TRACK_PLAY).getState();
    }

    public static MusicInfo getMusicInfo() {
        return new MusicInfo(MusicModel.getCurrentTrackPath(),
                MusicModel.getCurrentTrackAlbum(),
                MusicModel.getCurrentTrackArtist(),
                MusicModel.getCurrentTrackName(),
                MusicModel.getCurrentTrackDuration());
    }
}
