package ru.reactiveturtle.reactivemusic.player.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

public class HeadphoneMuteReceiver extends BroadcastReceiver {
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
                MusicModel.setTrackPlay(false);
                break;
        }
    }
}
