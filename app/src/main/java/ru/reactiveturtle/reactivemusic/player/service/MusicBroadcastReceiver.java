package ru.reactiveturtle.reactivemusic.player.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.player.GlobalModel;

public class MusicBroadcastReceiver extends BroadcastReceiver {
    public static final int PLAY_PREVIOUS_TRACK = 0;

    public static final int PLAY_PAUSE_TRACK = 1;

    public static final int PLAY_NEXT_TRACK = 2;

    public static final int ACTIVITY_RESUMED = 4;
    public static final int ACTIVITY_PAUSED = 5;

    public static final int REPEAT_TRACK = 6;
    public static final String IS_REPEAT = "is_repeat";

    public static final int CLOSE_SERVICE = 7;

    public static final int SHOW_ACTIVITY = 8;

    private IMusicService.Presenter mPresenter;

    public MusicBroadcastReceiver(@NonNull IMusicService.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int action = intent.getIntExtra(Helper.ACTION_EXTRA, -1);
        switch (action) {
            case ACTIVITY_RESUMED:
                mPresenter.onActivityResumed();
                break;
            case ACTIVITY_PAUSED:
                mPresenter.onActivityPaused();
                break;
            case PLAY_PAUSE_TRACK:
                GlobalModel.setTrackPlay(!GlobalModel.isTrackPlay());
                break;
            case PLAY_PREVIOUS_TRACK:
                mPresenter.onPreviousTrack();
                break;
            case PLAY_NEXT_TRACK:
                mPresenter.onNextTrack();
                break;
            case REPEAT_TRACK:
                mPresenter.onRepeatTrackChanged(intent.getBooleanExtra(IS_REPEAT, false));
                break;
            case CLOSE_SERVICE:
                mPresenter.onCloseService();
                break;
            case SHOW_ACTIVITY:
                mPresenter.onShowActivity();
                break;
        }
    }
}
