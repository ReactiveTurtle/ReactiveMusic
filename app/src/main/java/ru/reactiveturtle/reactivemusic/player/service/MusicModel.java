package ru.reactiveturtle.reactivemusic.player.service;

public class MusicModel implements IMusicService.Model {
    private boolean mIsActivityActive = true;
    @Override
    public void setActivityActive(boolean isActive) {
        mIsActivityActive = isActive;
    }

    @Override
    public boolean isActivityActive() {
        return mIsActivityActive;
    }

    private boolean mIsRepeatTrack = false;
    @Override
    public void setRepeatTrack(boolean isRepeat) {
        mIsRepeatTrack = isRepeat;
    }

    @Override
    public boolean isRepeatTrack() {
        return mIsRepeatTrack;
    }

    private boolean mIsTrackPrepared = false;
    @Override
    public void setTrackPrepared(boolean isPrepared) {
        mIsTrackPrepared = isPrepared;
    }

    @Override
    public boolean isTrackPrepared() {
        return mIsTrackPrepared;
    }
}
