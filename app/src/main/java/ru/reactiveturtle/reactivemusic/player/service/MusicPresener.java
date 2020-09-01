package ru.reactiveturtle.reactivemusic.player.service;

import androidx.annotation.NonNull;

import java.util.List;

import ru.reactiveturtle.reactivemusic.player.GlobalModel;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.PlayerContract;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerRepository;

public class MusicPresener implements IMusicService.Presenter {
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

        GlobalModel.registerListener(0, new GlobalModel.OnModelUpdateListener() {
            @Override
            public void onTrackChanged(MusicInfo currentTrack) {
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
        });
        mView.playTrack(mRepository.getCurrentMusic().getPath());
    }

    @Override
    public void onActivityResumed() {
        mModel.setActivityActive(true);
        mView.hidePlayer();
        if (GlobalModel.isTrackPlay()) {
            mView.startTimer();
        }
    }

    @Override
    public void onActivityPaused() {
        mModel.setActivityActive(false);
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
        GlobalModel.setCurrentTrack(GlobalModel.getCurrentTrack(), 0);
        mView.updateTrackProgress(0);
        mModel.setTrackPrepared(true);
        if (!isFirst && GlobalModel.isTrackPlay()) {
            onPlay();
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
        GlobalModel.unregisterListener(0);
        mView.closeService();
    }

    @Override
    public void onShowActivity() {
        mView.showActivity(mRepository.getCurrentPlaylist(),
                GlobalModel.getCurrentTrack().getPath());
    }

    @Override
    public void onPreviousTrack() {
        if (!mModel.isTrackPrepared()) {
            return;
        }
        mModel.setTrackPrepared(false);
        if (!GlobalModel.isTrackPlay()) {
            GlobalModel.setTrackPlay(true, 0);
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
        GlobalModel.setTrackPlay(!GlobalModel.isTrackPlay(), 1);
    }

    @Override
    public void onNextTrack() {
        if (!mModel.isTrackPrepared()) {
            return;
        }
        mModel.setTrackPrepared(false);
        if (!GlobalModel.isTrackPlay()) {
            GlobalModel.setTrackPlay(true, 0);
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
        GlobalModel.setCurrentTrack(musicInfo, 0, 1);
        mView.showCurrentTrack(musicInfo);
    }

    @Override
    public void onRepeatTrack() {

    }

    @Override
    public void onRepeatTrackChanged(boolean isRepeat) {
        mModel.setRepeatTrack(isRepeat);
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
        if (!mModel.isActivityActive()) {
            mView.showPlayer(GlobalModel.getCurrentTrack() == null ?
                    MusicInfo.getDefault() : GlobalModel.getCurrentTrack());
        }
    }
}
