package ru.reactiveturtle.reactivemusic.player.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

public class HeadphoneMuteReceiver extends BroadcastReceiver {
    private IMusicService.Presenter mPresenter;

    public HeadphoneMuteReceiver(@NonNull IMusicService.Presenter presenter) {
        mPresenter = presenter;
    }

    public boolean isFirst = true;
    @Override
    public void onReceive(Context context, Intent intent) {
        int state = intent.getIntExtra("state", -1);
        switch (state) {
            case 0:
                if (isFirst) {
                    isFirst = false;
                    return;
                }
                System.out.println("Наушники отключены");
                mPresenter.onPause();
                break;
        }
    }
}
