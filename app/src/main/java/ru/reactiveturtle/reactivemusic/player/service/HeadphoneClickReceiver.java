package ru.reactiveturtle.reactivemusic.player.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import java.util.Timer;
import java.util.TimerTask;

import ru.reactiveturtle.reactivemusic.player.GlobalModel;

public class HeadphoneClickReceiver extends BroadcastReceiver {
    private Timer timeout;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        if (!Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
            return;
        }
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event == null) {
            return;
        }
        int action = event.getAction();
        if (timeout == null && action == KeyEvent.ACTION_UP) {
            timeout = new Timer();
            timeout.schedule(new TimerTask() {
                @Override
                public void run() {
                    new Thread(() -> {
                        timeout = null;
                    });
                }
            }, 100);
            GlobalModel.setTrackPlay(!GlobalModel.isTrackPlay());
        }
    }
}