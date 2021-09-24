package ru.reactiveturtle.reactivemusic.musicservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import ru.reactiveturtle.reactivemusic.musicservice.notification.MusicPlayerNotification;
import ru.reactiveturtle.reactivemusic.musicservice.notification.MusicServiceNotificationProvider;
import ru.reactiveturtle.reactivemusic.player.AllMusicDataManager;
import ru.reactiveturtle.reactivemusic.player.MusicPlayer;
import ru.reactiveturtle.reactivemusic.player.MusicPlayerProvider;
import ru.reactiveturtle.reactivemusic.theme.Theme;

public class MusicService extends Service {
    private static boolean isRunning = false;

    public static boolean isRunning() {
        return isRunning;
    }

    private Theme theme;
    private AllMusicDataManager allMusicDataManager;
    private MusicPlayer musicPlayer;
    private MusicPlayerNotification musicPlayerNotification;

    @Override
    public void onCreate() {
        super.onCreate();
        isRunning = true;
        theme = new Theme(this);
        allMusicDataManager = new AllMusicDataManager(this, theme);
        musicPlayer = new MusicPlayer(this, allMusicDataManager);
        musicPlayerNotification = new MusicPlayerNotification(this, theme, new MusicServiceNotificationProvider(this), musicPlayer);
    }

    private Intent serviceIntent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new Binder(this);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
    }

    public void closeService() {
        musicPlayer.release();
        musicPlayerNotification.release();
        theme.release();
        stopService(serviceIntent);
        stopSelf();
    }

    public static class Binder extends android.os.Binder {
        private MusicService musicService;

        public Binder(MusicService musicService) {
            this.musicService = musicService;
        }

        public Theme getTheme() {
            return musicService.theme;
        }

        public AllMusicDataManager getAllMusicDataManager() {
            return this.musicService.allMusicDataManager;
        }

        public MusicPlayerProvider getPlayer() {
            return musicService.musicPlayer;
        }
    }
}
