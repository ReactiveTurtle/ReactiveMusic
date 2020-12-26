package ru.reactiveturtle.reactivemusic.player.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.player.GlobalModel;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerModel;
import ru.reactiveturtle.reactivemusic.player.mvp.view.PlayerActivity;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;

public class MusicBroadcastReceiver extends BroadcastReceiver {
    public static final int PLAY_PREVIOUS_TRACK = 0;

    public static final int PLAY_PAUSE_TRACK = 1;

    public static final int PLAY_NEXT_TRACK = 2;

    public static final int CLOSE_SERVICE = 7;

    public static final int SHOW_ACTIVITY = 8;

    public boolean isActivityShowed = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        int action = intent.getIntExtra(Helper.ACTION_EXTRA, -1);
        switch (action) {
            case PLAY_PAUSE_TRACK:
                PlayerModel.switchPlayPause();
                break;
            case PLAY_PREVIOUS_TRACK:
                ReactiveArchitect.getBridge(Bridges.PreviousTrackClick_To_PlayTrack).pull();
                break;
            case PLAY_NEXT_TRACK:
                ReactiveArchitect.getBridge(Bridges.NextTrackClick_To_PlayTrack).pull();
                break;
            case CLOSE_SERVICE:
                ReactiveArchitect.getBridge(Bridges.MusicBroadcast_To_CloseService).pull();
                break;
            case SHOW_ACTIVITY:
                if (!isActivityShowed) {
                    isActivityShowed = true;
                }
                Intent activityIntent = new Intent(context, PlayerActivity.class);
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //activityIntent.putExtra(Helper.ACTION_EXTRA, MusicBroadcastReceiver.SHOW_ACTIVITY);
                context.startActivity(activityIntent);
                break;
        }
    }
}
