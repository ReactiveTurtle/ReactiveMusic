package ru.reactiveturtle.reactivemusic.player;

import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import ru.reactiveturtle.reactivemusic.player.shared.MusicAlbumCoverData;

public interface MusicPlayerProvider {
    void loadTrack(@NonNull String path);

    void loadPreviousTrack();

    void loadNextTrack();

    void play();

    void pause();

    boolean isPlaying();

    void setLooping(boolean isLooping);

    boolean isLooping();

    void setPlayRandomTrack(boolean isPlayRandomTrack);

    boolean isPlayRandomTrack();

    void setProgress(int milliseconds);

    int getDuration();

    void setVolume(float left, float right);

    MusicData getCurrentMusicData();

    void addMusicPlayerListener(MusicPlayerListener musicPlayerListener);

    void removeMusicPlayerListener(MusicPlayerListener musicPlayerListener);

    abstract class MusicPlayerListener {
        public void onPrepared(int duration) {
        }

        public void onPlay() {
        }

        public void onPause() {
        }

        public void onLoopingChanged(boolean isLooping) {
        }

        public void onPlayRandomTrackChanged(boolean isPlayRandomTrack) {
        }

        public void onProgressChanged(int progress) {
        }

        public void onMusicMetadataLoad(@NonNull MusicMetadata musicMetadata) {
            Objects.requireNonNull(musicMetadata);
        }

        public void onMusicCoverDataLoad(@NonNull MusicAlbumCoverData musicAlbumCoverData) {
            Objects.requireNonNull(musicAlbumCoverData);
        }
    }
}
