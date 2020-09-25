package ru.reactiveturtle.reactivemusic.player.mvp.model;

import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.PlayerContract;

public class PlayerModel implements PlayerContract.Model {
    private int mSelectedPage = 0;
    @Override
    public void setSelectedPage(int position) {
        mSelectedPage = position;
    }

    @Override
    public int getSelectedPage() {
        return mSelectedPage;
    }

    private String mPlaylist;
    @Override
    public void setPlaylistOpen(String playlist) {
        mPlaylist = playlist;
    }

    @Override
    public String getPlaylistOpen() {
        return mPlaylist;
    }

    private boolean mIsScrollToCurrentTrackLater = false;
    @Override
    public void setScrollToCurrentTrackLater(boolean isDoLater) {
        mIsScrollToCurrentTrackLater = isDoLater;
    }

    @Override
    public boolean isScrollToCurrentTrackLater() {
        return mIsScrollToCurrentTrackLater;
    }
}
