package ru.reactiveturtle.reactivemusic.player;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;

import java.util.Objects;

public class GlobalModel {
    private static MusicInfo CURRENT_TRACK;
    private static final Handler handler = new Handler(Looper.getMainLooper());

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
        public void onTrackChanged(MusicInfo currentTrack) {

        }

        public void onTrackProgressUpdate(int progress, boolean isUnlockTrackProgress) {

        }

        public void onTrackPlayUpdate(boolean isPlay) {

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
