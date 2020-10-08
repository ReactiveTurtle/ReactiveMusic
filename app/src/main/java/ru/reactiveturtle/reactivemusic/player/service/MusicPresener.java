package ru.reactiveturtle.reactivemusic.player.service;

import androidx.annotation.NonNull;

import java.util.List;

import ru.reactiveturtle.reactivemusic.player.GlobalModel;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.PlayerContract;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerRepository;

public class MusicPresener implements IMusicService.Presenter {
    public static final int GLOBAL_MODEL_LISTENER = 0;

    private IMusicService.View mView;
    private IMusicService.Model mModel;
    private PlayerContract.Repository mRepository;

    public MusicPresener(@NonNull IMusicService.View view, PlayerRepository playerRepository) {
        mView = view;
        mModel = new MusicModel();
        mRepository = playerRepository;

        if (mRepository.getCurrentPlaylist() != null) {
            if (!mRepository.getPlaylist(mRepository.getCurrentPlaylist())
                    .contains(mRepository.getCurrentMusic().getPath())) {
                mRepository.setCurrentPlaylist(null);
            }
        }

        GlobalModel.registerListener(GLOBAL_MODEL_LISTENER, new GlobalModel.OnModelUpdateListener() {
            @Override
            public void onActivityStateChanged(GlobalModel.ActivityState activityState) {
                switch (activityState) {
                    case RESUMED:
                        onActivityResumed();
                        break;
                    case PAUSED:
                        onActivityPaused();
                        break;
                }
            }

            @Override
            public void onTrackPathUpdate(MusicInfo currentTrack) {
                mView.playTrack(currentTrack.getPath());
            }

            @Override
            public void onTrackProgressUpdate(int progress, boolean isUnlockTrackProgress) {
                mView.updateTrackProgress(progress);
            }

            @Override
            public void onTrackPlayUpdate(boolean isPlay) {
                if (!mModel.isTrackPrepared()) {
                    return;
                }
                if (isPlay) {
                    onPlay();
                } else {
                    onPause();
                }
            }

            @Override
            public void onRepeatTrack(boolean isRepeat) {
                mRepository.setRepeatTrack(isRepeat);
                onRepeatTrackChanged(isRepeat);
            }

            @Override
            public void onPlayRandomTrack(boolean isRandom) {
                mRepository.setPlayRandomTrack(isRandom);
            }
        });
        GlobalModel.setPlayRandomTrack(mRepository.isPlayRandomTrack());
        if (mRepository.getCurrentMusic() != null) {
            mView.playTrack(mRepository.getCurrentMusic().getPath());
        }
    }

    @Override
    public void onActivityResumed() {
        mView.hidePlayer();
        if (GlobalModel.isTrackPlay()) {
            mView.startTimer();
        }
    }

    @Override
    public void onActivityPaused() {
        if (GlobalModel.isTrackPlay()) {
            updateNotification();
        }
        if (GlobalModel.isTrackPlay()) {
            mView.stopTimer();
        } else {
            onCloseService();
        }
    }

    @Override
    public void onPlayerPrepared(boolean isFirst, int duration) {
        GlobalModel.getCurrentTrack().setDuration(duration);
        GlobalModel.updateCurrentTrack(GlobalModel.getCurrentTrack().getPath(), GLOBAL_MODEL_LISTENER);
        mView.updateTrackProgress(0);
        mModel.setTrackPrepared(true);
        if (!isFirst && GlobalModel.isTrackPlay()) {
            onPlay();
        } else if (isFirst) {
            GlobalModel.setRepeatTrack(mRepository.isRepeatTrack());
        }
    }

    @Override
    public void onTrackComplete() {
        if (GlobalModel.isPlayRandomTrack()) {
            List<String> paths = mRepository.getPlaylist(mRepository.getCurrentPlaylist());
            int newPosition = (int) (Math.random() * (paths.size() - 1));
            mView.playTrack(paths.get(newPosition));
            paths.clear();
        } else {
            onNextTrack();
        }
    }

    @Override
    public void onPlay() {
        mView.startMusic();
        updateNotification();
    }

    @Override
    public void onPause() {
        mView.pauseMusic();
        updateNotification();
    }

    @Override
    public void onCloseService() {
        GlobalModel.unregisterListener(GLOBAL_MODEL_LISTENER);
        mView.closeService();
    }

    @Override
    public void onShowActivity() {
        mView.showActivity(mRepository.getCurrentPlaylist(),
                GlobalModel.getCurrentTrack().getPath());
    }

    @Override
    public void onPreviousTrack() {
        mModel.setTrackPrepared(false);
        if (!GlobalModel.isTrackPlay()) {
            GlobalModel.setTrackPlay(true, GLOBAL_MODEL_LISTENER);
        }
        List<String> allTracks =
                mRepository.getPlaylist(mRepository.getCurrentPlaylist());
        if (allTracks.size() > 0) {
            int currentMusicTrack = -1;
            for (int i = 0; i < allTracks.size(); i++) {
                if (allTracks.get(i).equals(GlobalModel.getCurrentTrack().getPath())) {
                    currentMusicTrack = i;
                    break;
                }
            }
            int newTrackIndex = currentMusicTrack - 1 < 0 ? allTracks.size() - 1 : currentMusicTrack - 1;
            mView.playTrack(allTracks.get(newTrackIndex));
        }
    }

    @Override
    public void onPlayPause() {
        GlobalModel.setTrackPlay(!GlobalModel.isTrackPlay());
    }

    @Override
    public void onNextTrack() {
        mModel.setTrackPrepared(false);
        if (!GlobalModel.isTrackPlay()) {
            GlobalModel.setTrackPlay(true, GLOBAL_MODEL_LISTENER);
        }
        List<String> allTracks = mRepository.getPlaylist(mRepository.getCurrentPlaylist());
        if (allTracks.size() > 0) {
            int currentMusicTrack = -1;
            for (int i = 0; i < allTracks.size(); i++) {
                if (allTracks.get(i).equals(GlobalModel.getCurrentTrack().getPath())) {
                    currentMusicTrack = i;
                    break;
                }
            }
            int newTrackIndex = currentMusicTrack + 1 >= allTracks.size() ? 0 : currentMusicTrack + 1;
            mView.playTrack(allTracks.get(newTrackIndex));
        }
    }

    @Override
    public void onMusicChanged(MusicInfo musicInfo) {
        mModel.setTrackPrepared(false);
        GlobalModel.updateCurrentTrack(musicInfo.getPath(), GLOBAL_MODEL_LISTENER, 1);
        mView.showCurrentTrack(musicInfo);
    }

    @Override
    public void onRepeatTrack() {

    }

    @Override
    public void onRepeatTrackChanged(boolean isRepeat) {
        mView.repeatTrack(isRepeat);
    }

    @Override
    public void onDestroy() {
        mRepository.setCurrentMusic(GlobalModel.getCurrentTrack());

        mView = null;
        mRepository.release();
        mRepository = null;
    }

    private void updateNotification() {
        if (GlobalModel.getActivityState() != GlobalModel.ActivityState.RESUMED) {
            mView.showPlayer(GlobalModel.getCurrentTrack());
        }
    }
}
