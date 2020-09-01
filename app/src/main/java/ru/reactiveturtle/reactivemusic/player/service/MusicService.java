package ru.reactiveturtle.reactivemusic.player.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.loader.content.Loader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.GlobalModel;
import ru.reactiveturtle.reactivemusic.player.Loaders;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerRepository;
import ru.reactiveturtle.reactivemusic.player.mvp.view.PlayerActivity;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;

import static ru.reactiveturtle.reactivemusic.Helper.getArtUriFromMusicFile;

public class MusicService extends Service implements IMusicService.View {
    private IMusicService.Presenter mPresenter;
    private MusicBroadcastReceiver mPlayerReceiver;
    private HeadphoneMuteReceiver mHeadphoneMuteReceiver;
    private HeadphoneClickReceiver mHeadphoneClickReceiver;

    private MediaPlayer mMusicPlayer;

    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mMusicPlayer = new MediaPlayer();
        mMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        // TODO: Исправить
        mPresenter = new MusicPresener(this, new PlayerRepository(this));

        mPlayerReceiver = new MusicBroadcastReceiver(mPresenter);
        registerReceiver(mPlayerReceiver, new IntentFilter(getClass().getName()));

        mHeadphoneMuteReceiver = new HeadphoneMuteReceiver(mPresenter);
        registerReceiver(mHeadphoneMuteReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));

        mHeadphoneClickReceiver = new HeadphoneClickReceiver();

        initNotification();

        mMusicPlayer.setOnPreparedListener(mediaPlayer -> {
            updateTrackProgress(mediaPlayer.getCurrentPosition());
            mPresenter.onPlayerPrepared(true, mMusicPlayer.getDuration());
            mMusicPlayer.setOnPreparedListener(mediaPlayer1 ->
                    mPresenter.onPlayerPrepared(false, mMusicPlayer.getDuration()));
        });
        mMusicPlayer.setOnCompletionListener(mediaPlayer -> {
            mPresenter.onNextTrack();
        });
    }

    private Intent mServiceIntent;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceIntent = intent;
        Intent receiverIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, receiverIntent, 0);
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mAudioManager.registerMediaButtonEventReceiver(pendingIntent);
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.onDestroy();
        mMusicPlayer.release();
    }

    @Override
    public void showCurrentTrack(MusicInfo musicInfo) {
        try {
            if (musicInfo != null) {
                if (GlobalModel.isTrackPlay()) {
                    mMusicPlayer.pause();
                }
                mMusicPlayer.reset();
                mMusicPlayer.setDataSource(musicInfo.getPath());
                mMusicPlayer.prepare();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void clearCurrentMusic() {

    }

    @Override
    public void showPlayer(MusicInfo currentMusic) {
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.player_music_notification_fragment);

        BitmapDrawable bitmapDrawable;
        try {
            bitmapDrawable = Helper.getDrawable(getContentResolver(),
                    getArtUriFromMusicFile(this, currentMusic.getPath()), 256);
        } catch (FileNotFoundException e) {
            bitmapDrawable = Theme.getDefaultAlbumCover();
        }

        remoteViews.setBitmap(R.id.playerNotificationAlbum, "setImageBitmap",
                bitmapDrawable.getBitmap());
        remoteViews.setTextViewText(R.id.playerNotificationTitle, currentMusic.getTitle());
        remoteViews.setTextViewText(R.id.playerNotificationInfo,
                currentMusic.getArtist() + " | " + currentMusic.getAlbum());
        remoteViews.setInt(R.id.playerNotificationPlayPause, "setBackgroundResource",
                mMusicPlayer.isPlaying() ? R.drawable.pause_button :
                        R.drawable.play_button);

        Intent intent = new Intent(getClass().getName());
        intent.putExtra(Helper.ACTION_EXTRA, MusicBroadcastReceiver.CLOSE_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, MusicBroadcastReceiver.CLOSE_SERVICE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.playerNotificationCloseButton, pendingIntent);

        intent = new Intent(getClass().getName());
        intent.putExtra(Helper.ACTION_EXTRA, MusicBroadcastReceiver.PLAY_PREVIOUS_TRACK);
        pendingIntent = PendingIntent.getBroadcast(this, MusicBroadcastReceiver.PLAY_PREVIOUS_TRACK,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.playerNotificationPreviousTrack, pendingIntent);

        intent = new Intent(getClass().getName());
        intent.putExtra(Helper.ACTION_EXTRA, MusicBroadcastReceiver.PLAY_PAUSE_TRACK);
        pendingIntent = PendingIntent.getBroadcast(this, MusicBroadcastReceiver.PLAY_PAUSE_TRACK,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.playerNotificationPlayPause, pendingIntent);

        intent = new Intent(getClass().getName());
        intent.putExtra(Helper.ACTION_EXTRA, MusicBroadcastReceiver.PLAY_NEXT_TRACK);
        pendingIntent = PendingIntent.getBroadcast(this, MusicBroadcastReceiver.PLAY_NEXT_TRACK,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.playerNotificationNextTrack, pendingIntent);

        mServiceNotificationBuilder.setCustomBigContentView(remoteViews);

        startForeground(17253, mServiceNotificationBuilder.build());
    }

    @Override
    public void hidePlayer() {
        stopForeground(true);
    }

    private Timer mProgressUpdater;

    @Override
    public void startMusic() {
        mMusicPlayer.start();
        startTimer();
    }

    @Override
    public void startTimer() {
        if (mProgressUpdater != null) {
            mProgressUpdater.cancel();
        }
        mProgressUpdater = new Timer();
        mProgressUpdater.schedule(new TimerTask() {
            @Override
            public void run() {
                new Thread() {
                    @Override
                    public void run() {
                        sendUpdateTrackProgress(mMusicPlayer.getCurrentPosition(), false);
                    }
                }.start();
            }
        }, 0, 250);
    }

    @Override
    public void pauseMusic() {
        mMusicPlayer.pause();
        stopTimer();
    }

    @Override
    public void updateTheme() {

    }

    @Override
    public void updateThemeContext() {

    }

    @Override
    public void stopTimer() {
        if (mProgressUpdater != null) {
            mProgressUpdater.cancel();
            mProgressUpdater = null;
        }
    }

    @Override
    public void updateTrackProgress(int progress) {
        mMusicPlayer.seekTo(progress);
        sendUpdateTrackProgress(mMusicPlayer.getCurrentPosition(), true);
    }

    @Override
    public void repeatTrack(boolean isRepeat) {
        mMusicPlayer.setLooping(isRepeat);
    }

    @Override
    public void sendRepeatTrack(boolean isRepeat) {

    }

    @Override
    public void showActivity(String playlist, String track) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Helper.ACTION_EXTRA, MusicBroadcastReceiver.SHOW_ACTIVITY);
        startActivity(intent);
    }

    @Override
    public void playTrack(String path) {
        Loaders.MusicInfoLoader musicInfoLoader = new Loaders.MusicInfoLoader(this, path);
        musicInfoLoader.registerListener(0, (loader, data) -> {
            Loaders.AlbumCoverLoader albumCoverLoader =
                    new Loaders.AlbumCoverLoader(this, path, Theme.getDefaultAlbumCover());
            albumCoverLoader.registerListener(0, (loader1, data1) -> {
                data.setAlbumImage(data1);
                mPresenter.onMusicChanged(data);
            });
            albumCoverLoader.forceLoad();
        });
        musicInfoLoader.forceLoad();
    }

    @Override
    public void closeService() {
        unregisterReceiver(mPlayerReceiver);
        unregisterReceiver(mHeadphoneMuteReceiver);
        hidePlayer();
        stopSelf();
        stopService(mServiceIntent);
    }

    public void sendUpdateTrackProgress(int progress, boolean isUnlockProgressUpdate) {
        GlobalModel.setTrackProgress(progress, isUnlockProgressUpdate, 0);
    }

    private NotificationCompat.Builder mServiceNotificationBuilder;

    private static final String MUSIC_CHANNEL = "MUSIC_CHANNEL";

    private void initNotification() {
        NotificationChannel channel;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(MUSIC_CHANNEL, MUSIC_CHANNEL,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(MUSIC_CHANNEL);
            channel.enableLights(true);
            channel.setLightColor(Color.CYAN);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
            mServiceNotificationBuilder = new NotificationCompat.Builder(this, MUSIC_CHANNEL);
        } else {
            mServiceNotificationBuilder = new NotificationCompat.Builder(this);
        }

        Intent intent = new Intent(getClass().getName());
        intent.putExtra(Helper.ACTION_EXTRA, MusicBroadcastReceiver.SHOW_ACTIVITY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, MusicBroadcastReceiver.SHOW_ACTIVITY,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mServiceNotificationBuilder
                .setSmallIcon(R.drawable.ic_note)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(getResources().getColor(R.color.colorPrimary))
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true);
    }
}
