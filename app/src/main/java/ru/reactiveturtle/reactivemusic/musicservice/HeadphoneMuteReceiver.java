package ru.reactiveturtle.reactivemusic.musicservice;

import android.content.Context;
import android.content.Intent;

import java.util.Objects;

import ru.reactiveturtle.reactivemusic.player.MusicPlayerProvider;
import ru.reactiveturtle.reactivemusic.toolkit.IndependentBroadcastReceiver;

public class HeadphoneMuteReceiver extends IndependentBroadcastReceiver {
    public boolean isFirst = true;
    private MusicPlayerProvider musicPlayerProvider;

    public HeadphoneMuteReceiver(MusicPlayerProvider musicPlayerProvider) {
        Objects.requireNonNull(musicPlayerProvider);
        this.musicPlayerProvider = musicPlayerProvider;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int state = intent.getIntExtra("state", -1);
        switch (state) {
            case 0:
                if (isFirst) {
                    isFirst = false;
                    return;
                }
                musicPlayerProvider.pause();
                break;
        }
    }
}
