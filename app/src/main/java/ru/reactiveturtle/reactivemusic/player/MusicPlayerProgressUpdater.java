package ru.reactiveturtle.reactivemusic.player;

import java.util.Timer;
import java.util.TimerTask;

public class MusicPlayerProgressUpdater {
    private Timer timer;

    public void start() {
        if (timer != null) {
            return;
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (callback != null) {
                    callback.onUpdate();
                }
            }
        }, 0, 15);
    }

    public void stop() {
        if (timer == null) {
            return;
        }
        timer.cancel();
        timer = null;
    }

    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void onUpdate();
    }
}
