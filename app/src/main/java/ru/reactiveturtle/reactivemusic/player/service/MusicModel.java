package ru.reactiveturtle.reactivemusic.player.service;

public class MusicModel implements IMusicService.Model {
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
