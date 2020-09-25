package ru.reactiveturtle.reactivemusic.player.service;

import androidx.annotation.NonNull;

import ru.reactiveturtle.reactivemusic.player.BaseMusicContract;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;

public interface IMusicService {
    interface Model extends BaseMusicContract.Model {
        void setTrackPrepared(boolean isPrepared);

        boolean isTrackPrepared();
    }

    interface View extends BaseMusicContract.MusicControlView {
        void showPlayer(MusicInfo currentMusic);

        void hidePlayer();

        void startTimer();

        void stopTimer();

        void playTrack(@NonNull String path);

        void updateTrackProgress(int progress);

        void repeatTrack(boolean isRepeat);

        void showActivity(String playlist, String track);

        void closeService();
    }

    interface Presenter extends BaseMusicContract.Presenter {
        void onActivityResumed();

        void onActivityPaused();

        void onPlayerPrepared(boolean isFirst, int duration);

        void onTrackComplete();

        void onPlay();

        void onPause();

        void onMusicChanged(MusicInfo musicInfo);

        void onCloseService();

        void onShowActivity();

        void onDestroy();
    }
}
