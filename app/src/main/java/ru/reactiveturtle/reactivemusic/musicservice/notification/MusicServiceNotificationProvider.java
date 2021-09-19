package ru.reactiveturtle.reactivemusic.musicservice.notification;

import android.app.Notification;

import ru.reactiveturtle.reactivemusic.musicservice.MusicService;

public final class MusicServiceNotificationProvider {
    private MusicService musicService;

    public MusicServiceNotificationProvider(MusicService musicService) {
        this.musicService = musicService;
    }

    public void show(Notification notification) {
        musicService.startForeground(17253, notification);
    }

    public void hide() {
        musicService.stopForeground(true);
    }

    public void closeService() {
        musicService.closeService();
    }
}
