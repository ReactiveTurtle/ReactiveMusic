package ru.reactiveturtle.reactivemusic.player;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import java.util.Objects;

import ru.reactiveturtle.reactivemusic.player.mvp.PlayerPresenter;

public class GlobalModel {
    public static PlayerPresenter PLAYER_PRESENTER = null;
    private static boolean IS_SERVICE_RUNNING = false;

    public static void setServiceRunning(boolean isServiceRunning) {
        IS_SERVICE_RUNNING = isServiceRunning;
    }

    public static boolean isServiceRunning() {
        return IS_SERVICE_RUNNING;
    }

    private static final Handler handler = new Handler(Looper.getMainLooper());
    private static ActivityState ACTIVITY_STATE = ActivityState.STOPPED;

    public static void setActivityState(ActivityState activityState, int... exceptionIds) {
        ACTIVITY_STATE = activityState;
        for (int i = 0; i < LISTENERS.size(); i++) {
            if (isNotException(LISTENERS.keyAt(i), exceptionIds)) {
                OnModelUpdateListener listener = LISTENERS.valueAt(i);
                handler.post(() -> listener.onActivityStateChanged(ACTIVITY_STATE));
            }
        }
    }

    public static ActivityState getActivityState() {
        return ACTIVITY_STATE;
    }

    public enum ActivityState {
        RESUMED, PAUSED, STOPPED
    }

    private static MusicInfo CURRENT_TRACK;
    private static boolean IS_FIRST_TRACK_LOAD = true;

    public static boolean isFirstTrackLoad() {
        return IS_FIRST_TRACK_LOAD;
    }

    public static void setFirstTrackLoad(boolean isFirstTrackLoad) {
        IS_FIRST_TRACK_LOAD = isFirstTrackLoad;
    }

    public static void setCurrentTrack(MusicInfo currentTrack, int... exceptionIds) {
        CURRENT_TRACK = currentTrack;
        for (int i = 0; i < LISTENERS.size(); i++) {
            if (isNotException(LISTENERS.keyAt(i), exceptionIds)) {
                OnModelUpdateListener listener = LISTENERS.valueAt(i);
                handler.post(() -> listener.onTrackChanged(currentTrack));
            }
        }
    }

    @Nullable
    public static MusicInfo getCurrentTrack() {
        return CURRENT_TRACK;
    }

    private static int TRACK_PROGRESS = 0;

    public static void setTrackProgress(int trackProgress, boolean isUnlockTrackProgress, int... exceptionIds) {
        GlobalModel.TRACK_PROGRESS = trackProgress;
        for (int i = 0; i < LISTENERS.size(); i++) {
            if (isNotException(LISTENERS.keyAt(i), exceptionIds)) {
                OnModelUpdateListener listener = LISTENERS.valueAt(i);
                handler.post(() -> {
                    listener.onTrackProgressUpdate(trackProgress, isUnlockTrackProgress);
                });
            }
        }
    }

    public static int getTrackProgress() {
        return TRACK_PROGRESS;
    }

    private static boolean IS_TRACK_PLAY = false;

    public static void setTrackPlay(boolean isPlay, int... exceptionIds) {
        IS_TRACK_PLAY = isPlay;
        for (int i = 0; i < LISTENERS.size(); i++) {
            if (isNotException(LISTENERS.keyAt(i), exceptionIds)) {
                OnModelUpdateListener listener = LISTENERS.valueAt(i);
                handler.post(() -> {
                    listener.onTrackPlayUpdate(IS_TRACK_PLAY);
                });
            }
        }
    }

    public static boolean isTrackPlay() {
        return IS_TRACK_PLAY;
    }

    private static boolean IS_REPEAT_TRACK = false;

    public static void setRepeatTrack(boolean isRepeat, int... exceptionIds) {
        IS_REPEAT_TRACK = isRepeat;
        for (int i = 0; i < LISTENERS.size(); i++) {
            if (isNotException(LISTENERS.keyAt(i), exceptionIds)) {
                OnModelUpdateListener listener = LISTENERS.valueAt(i);
                handler.post(() -> {
                    listener.onRepeatTrack(IS_REPEAT_TRACK);
                });
            }
        }
    }

    public static boolean isRepeatTrack() {
        return IS_REPEAT_TRACK;
    }

    private static boolean IS_PLAY_RANDOM_TRACK = false;

    public static void setPlayRandomTrack(boolean isRandom, int... exceptionIds) {
        IS_PLAY_RANDOM_TRACK = isRandom;
        for (int i = 0; i < LISTENERS.size(); i++) {
            if (isNotException(LISTENERS.keyAt(i), exceptionIds)) {
                OnModelUpdateListener listener = LISTENERS.valueAt(i);
                handler.post(() -> {
                    listener.onPlayRandomTrack(IS_PLAY_RANDOM_TRACK);
                });
            }
        }
    }

    public static boolean isPlayRandomTrack() {
        return IS_PLAY_RANDOM_TRACK;
    }

    private static ArrayMap<Integer, OnModelUpdateListener> LISTENERS = new ArrayMap<>();

    public static void registerListener(int id, @NonNull OnModelUpdateListener listener) {
        Objects.requireNonNull(listener);
        boolean isIdExists = LISTENERS.containsKey(id);
        if (isIdExists) {
            throw new IllegalArgumentException("Id " + id + " is already registered");
        }
        LISTENERS.put(id, listener);
    }

    public static void unregisterListener(int id) {
        LISTENERS.remove(id);
    }

    public static class OnModelUpdateListener {
        public void onActivityStateChanged(ActivityState activityState) {

        }

        public void onTrackChanged(MusicInfo currentTrack) {

        }

        public void onTrackProgressUpdate(int progress, boolean isUnlockTrackProgress) {

        }

        public void onTrackPlayUpdate(boolean isPlay) {

        }

        public void onRepeatTrack(boolean isRepeat) {

        }

        public void onPlayRandomTrack(boolean isRandom) {

        }
    }

    public static boolean isNotException(int id, int... exceptionIds) {
        for (int exceptionId : exceptionIds) {
            if (exceptionId == id) {
                return false;
            }
        }
        return true;
    }
}
