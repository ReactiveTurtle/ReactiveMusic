package ru.reactiveturtle.reactivemusic.musicservice.notification;

import android.content.Context;
import android.content.Intent;


import java.util.Objects;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.player.MusicPlayerProvider;
import ru.reactiveturtle.reactivemusic.toolkit.IndependentBroadcastReceiver;
import ru.reactiveturtle.reactivemusic.view.PlayerActivity;

public class MusicPlayerNotificationBroadcastReceiver extends IndependentBroadcastReceiver {
    public enum Action {
        PREVIOUS(0),
        PLAY_PAUSE(1),
        NEXT(2),
        CLOSE_SERVICE(3),
        SHOW_ACTIVITY(4);

        private int i;

        Action(int i) {
            this.i = i;
        }

        public int getValue() {
            return i;
        }
    }
    private MusicServiceNotificationProvider musicServiceNotificationProvider;
    private MusicPlayerProvider musicPlayerProvider;

    public MusicPlayerNotificationBroadcastReceiver(MusicServiceNotificationProvider musicServiceNotificationProvider,
                                                    MusicPlayerProvider musicPlayerProvider) {
        Objects.requireNonNull(musicServiceNotificationProvider);
        Objects.requireNonNull(musicPlayerProvider);
        this.musicServiceNotificationProvider = musicServiceNotificationProvider;
        this.musicPlayerProvider = musicPlayerProvider;
    }

    private boolean isActivityShowed = false;

    public void activityWasHide() {
        isActivityShowed = false;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Action action = Action.valueOf(intent.getStringExtra(Helper.ACTION_EXTRA));
        switch (action) {
            case PREVIOUS:
                musicPlayerProvider.loadPreviousTrack();
                break;
            case PLAY_PAUSE:
                if (musicPlayerProvider.isPlaying()) {
                    musicPlayerProvider.pause();
                } else {
                    musicPlayerProvider.play();
                }
                break;
            case NEXT:
                musicPlayerProvider.loadNextTrack();
                break;
            case CLOSE_SERVICE:
                musicServiceNotificationProvider.closeService();
                break;
            case SHOW_ACTIVITY:
                if (!isActivityShowed) {
                    isActivityShowed = true;
                    Intent activityIntent = new Intent(context, PlayerActivity.class);
                    activityIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(activityIntent);
                }
                break;
        }
    }
}