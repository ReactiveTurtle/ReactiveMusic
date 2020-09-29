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
import android.util.Log;
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
        GlobalModel.setServiceRunning(true);
        mAudioFocusListener = new AudioFocusListener(mMusicPlayer, "player");
        mMusicPlayer = new MediaPlayer();
        mMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        PlayerRepository playerRepository = new PlayerRepository(this);
        if (playerRepository.getCurrentMusic() == null) {
            List<String> paths = Helper.getAllTracksPathsInfo(this);
            if (paths.size() > 0) {
                playerRepository.setCurrentMusic(new MusicInfo(paths.get(0)));
            }
        }
        mPresenter = new MusicPresener(this, playerRepository);

        mPlayerReceiver = new MusicBroadcastReceiver(mPresenter);
        registerReceiver(mPlayerReceiver, new IntentFilter(getClass().getName()));

        mHeadphoneMuteReceiver = new HeadphoneMuteReceiver(mPresenter);
        registerReceiver(mHeadphoneMuteReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));

        mHeadphoneClickReceiver = new HeadphoneClickReceiver();

        initNotification();

        mMusicPlayer.setOnPreparedListener(mediaPlayer -> {
            updateTrackProgress(mediaPlayer.getCurrentPosition());
            mPresenter.onPlayerPrepared(true, mMusicPlayer.getDuration());
            mMusicPlayer.setOnPreparedListener(mediaPlayer1 -> {
                mPresenter.onPlayerPrepared(false, mMusicPlayer.getDuration());
            });
        });
        mMusicPlayer.setOnCompletionListener(mediaPlayer -> {
            mPresenter.onTrackComplete();
        });
    }

    private Intent mServiceIntent;
    private AudioManager mAudioManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceIntent = intent;
        Intent receiverIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, receiverIntent, 0);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            mAudioManager.registerMediaButtonEventReceiver(pendingIntent);
        }
        return START_NOT_STICKY;
    }

    private class AudioFocusListener implements AudioManager.OnAudioFocusChangeListener {

        String label = "";
        MediaPlayer mp;

        public AudioFocusListener(MediaPlayer mp, String label) {
            this.label = label;
            this.mp = mp;
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            String event = "";
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    if (GlobalModel.isTrackPlay()) {
                        mPresenter.onPlayPause();
                    }
                    event = "AUDIOFOCUS_LOSS";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    event = "AUDIOFOCUS_LOSS_TRANSIENT";
                    if (mMusicPlayer.isPlaying()) {
                        mMusicPlayer.pause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    event = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                    mMusicPlayer.setVolume(0.4f, 0.4f);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    event = "AUDIOFOCUS_GAIN";
                    mMusicPlayer.setVolume(1f, 1f);
                    if (GlobalModel.isTrackPlay()) {
                        mMusicPlayer.start();
                    }
                    break;
            }
            System.out.println(label + " onAudioFocusChange: " + event);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void showCurrentTrack(MusicInfo musicInfo) {
        try {
            if (musicInfo != null) {
                stopTimer();
                Log.e("DEBUG", "flag 1");
                mMusicPlayer.reset();
                Log.e("DEBUG", "flag 2");
                mMusicPlayer.setDataSource(musicInfo.getPath());
                Log.e("DEBUG", "flag 3");
                mMusicPlayer.prepareAsync();
                Log.e("DEBUG", "flag 4");
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
        mPlayerReceiver.isActivityShowed = false;
    }

    private Timer mProgressUpdater;
    private AudioFocusListener mAudioFocusListener;

    @Override
    public void startMusic() {
        mMusicPlayer.start();
        mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
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
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
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
        System.out.println("Is repeat: " + isRepeat);
        mMusicPlayer.setLooping(isRepeat);
        System.out.println("Is looping: " + mMusicPlayer.isLooping());
    }

    @Override
    public void showActivity(String playlist, String track) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Helper.ACTION_EXTRA, MusicBroadcastReceiver.SHOW_ACTIVITY);
        startActivity(intent);
    }

    Loaders.MusicInfoLoader musicInfoLoader;
    Loaders.AlbumCoverLoader albumCoverLoader;

    @Override
    public void playTrack(@NonNull String path) {
        if (musicInfoLoader != null) {
            musicInfoLoader.cancelLoad();
        }
        musicInfoLoader = new Loaders.MusicInfoLoader(this, path);
        musicInfoLoader.registerListener(0, (loader, data) -> {
            musicInfoLoader = null;
            if (albumCoverLoader != null) {
                albumCoverLoader.cancelLoad();
            }
            albumCoverLoader =
                    new Loaders.AlbumCoverLoader(this, path, Theme.getDefaultAlbumCover());
            albumCoverLoader.registerListener(0, (loader1, data1) -> {
                albumCoverLoader = null;
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
        stopService(mServiceIntent);
        mPresenter.onPause();
        mPresenter.onDestroy();
        mMusicPlayer.release();
        mPresenter.onPlayPause();
        GlobalModel.setServiceRunning(false);
        stopSelf();
    }

    public void sendUpdateTrackProgress(int progress, boolean isUnlockProgressUpdate) {
        GlobalModel.setTrackProgress(progress, isUnlockProgressUpdate,
                MusicPresener.GLOBAL_MODEL_LISTENER);
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
