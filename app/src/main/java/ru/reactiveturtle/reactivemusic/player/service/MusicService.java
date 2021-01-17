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
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.GlobalModel;
import ru.reactiveturtle.reactivemusic.player.Loaders;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerModel;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerRepository;
import ru.reactiveturtle.reactivemusic.player.mvp.view.PlayerActivity;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.tools.reactiveuvm.Bridge;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;
import ru.reactiveturtle.tools.reactiveuvm.StateKeeper;
import ru.reactiveturtle.tools.reactiveuvm.service.ArchitectService;

import static ru.reactiveturtle.reactivemusic.Helper.getArtUriFromMusicFile;

public class MusicService extends ArchitectService {
    private MusicBroadcastReceiver mPlayerReceiver;
    private HeadphoneMuteReceiver mHeadphoneMuteReceiver;
    private HeadphoneClickReceiver mHeadphoneClickReceiver;

    private MediaPlayer mMusicPlayer;

    private NotificationManager mNotificationManager;

    private PlayerManager playerManager;

    @Override
    public void onCreate() {
        MusicModel.initialize();
        super.onCreate();

        PlayerRepository repository = new PlayerRepository(this);

        mAudioFocusListener = new AudioFocusListener(mMusicPlayer, "player");
        mMusicPlayer = new MediaPlayer();
        mMusicPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(NOTIFICATION_SERVICE);

        playerManager = new PlayerManager(repository, this);

        mPlayerReceiver = new MusicBroadcastReceiver();
        registerReceiver(mPlayerReceiver, new IntentFilter(getClass().getName()));

        mHeadphoneMuteReceiver = new HeadphoneMuteReceiver();
        registerReceiver(mHeadphoneMuteReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));

        mHeadphoneClickReceiver = new HeadphoneClickReceiver();

        initNotification();

        mMusicPlayer.setOnPreparedListener(mediaPlayer -> {
            MusicModel.setCurrentTrackProgress(mMusicPlayer.getCurrentPosition());
            MusicModel.setCurrentTrackDuration(mMusicPlayer.getDuration());
            mMusicPlayer.setOnPreparedListener(mediaPlayer1 -> {
                startMusic();
                MusicModel.setCurrentTrackDuration(mMusicPlayer.getDuration());
            });
        });
        mMusicPlayer.setOnCompletionListener(mediaPlayer -> {
            if (!PlayerModel.isTrackLooping()) {
                MusicModel.setCurrentTrackProgress(mMusicPlayer.getCurrentPosition());
                MusicModel.setCurrentTrackPath(playerManager.getNextTrack());
            } else {
                //Костыль
                mMusicPlayer.start();
            }
        });

        MusicModel.setCurrentTrackPath(playerManager.getLastTrackPath());
        ReactiveArchitect.getBridge(Bridges.MusicService_To_Init).pull();
    }

    @Override
    protected void onInitializeBinders(List<StateKeeper.Binder> container) {
        container.addAll(Arrays.asList(
                ReactiveArchitect.getStateKeeper(PlayerModel.IS_ACTIVITY_ACTIVE).subscribe((view, value) -> {
                    boolean isActive = (boolean) value;
                    if (!isActive) {
                        stopTimer();
                        showPlayer(new MusicInfo(MusicModel.getCurrentTrackPath(),
                                MusicModel.getCurrentTrackAlbum(),
                                MusicModel.getCurrentTrackArtist(),
                                MusicModel.getCurrentTrackName(),
                                MusicModel.getCurrentTrackDuration()));
                    } else {
                        startTimer();
                        hidePlayer();
                    }
                }).call(),
                ReactiveArchitect.getStateKeeper(PlayerModel.SEEKBAR_TRACK_PROGRESS).subscribe((view, value) -> {
                    mMusicPlayer.seekTo((Integer) value);
                }),
                ReactiveArchitect.getStateKeeper(PlayerModel.IS_PLAY_RANDOM_TRACK).subscribe((view, value) -> {
                    playerManager.rememberPlayRandomTrack((Boolean) value);
                }),
                ReactiveArchitect.getStateKeeper(PlayerModel.IS_TRACK_LOOPING).subscribe((view, value) -> {
                    mMusicPlayer.setLooping((Boolean) value);
                    playerManager.rememberLooping((Boolean) value);
                }).call(),
                ReactiveArchitect.getStateKeeper(PlayerModel.IS_PLAY).subscribe((view, value) -> {
                    if ((boolean) value) {
                        startMusic();
                    } else {
                        pauseMusic();
                    }
                }),
                ReactiveArchitect.getStateKeeper(MusicModel.CURRENT_TRACK_PATH).subscribe((view, value) -> {
                    playTrack((String) value);
                }),
                ReactiveArchitect.getStateKeeper(MusicModel.CURRENT_TRACK_ALBUM).subscribe((view, value) ->
                        showPlayer(MusicModel.getMusicInfo())
                ),
                ReactiveArchitect.getStateKeeper(MusicModel.IS_TRACK_PLAY).subscribe((view, value) -> {
                    showPlayer(MusicModel.getMusicInfo());
                })
        ));
    }

    @Override
    protected void onInitializeBridges(List<Bridge> container) {
        container.addAll(Arrays.asList(
                ReactiveArchitect.createBridge(Bridges.PreviousTrackClick_To_PlayTrack).connect(() -> {
                    MusicModel.setCurrentTrackPath(playerManager.getPreviousTrack());
                }),
                ReactiveArchitect.createBridge(Bridges.NextTrackClick_To_PlayTrack).connect(() -> {
                    MusicModel.setCurrentTrackPath(playerManager.getNextTrack());
                }),
                ReactiveArchitect.createBridge(Bridges.MusicBroadcast_To_CloseService).connect(this::closeService)
        ));
    }

    private Intent mServiceIntent;
    private AudioManager mAudioManager;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceIntent = intent;
        Intent receiverIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 1, receiverIntent, 0);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.registerMediaButtonEventReceiver(pendingIntent);
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
                    ReactiveArchitect.changeState(MusicModel.IS_TRACK_PLAY, false);
                    event = "AUDIOFOCUS_LOSS";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    event = "AUDIOFOCUS_LOSS_TRANSIENT";
                    ReactiveArchitect.changeState(MusicModel.IS_TRACK_PLAY, false);
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    event = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                    mMusicPlayer.setVolume(0.4f, 0.4f);
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    event = "AUDIOFOCUS_GAIN";
                    mMusicPlayer.setVolume(1f, 1f);
                    ReactiveArchitect.changeState(MusicModel.IS_TRACK_PLAY, true);
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

    public void showPlayer(MusicInfo currentMusic) {
        if (PlayerModel.isActivityActive()) {
            return;
        }
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
                MusicModel.isTrackPlay() ? R.drawable.pause_button :
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

    public void hidePlayer() {
        stopForeground(true);
        mPlayerReceiver.isActivityShowed = false;
    }

    private Timer mProgressUpdater;
    private AudioFocusListener mAudioFocusListener;

    public void startMusic() {
        MusicModel.setTrackPlay(true);
        mMusicPlayer.start();
        mAudioManager.requestAudioFocus(mAudioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        startTimer();
    }

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
                        MusicModel.setCurrentTrackProgress(mMusicPlayer.getCurrentPosition());
                    }
                }.start();
            }
        }, 0, 250);
    }

    public void pauseMusic() {
        MusicModel.setTrackPlay(false);
        mMusicPlayer.pause();
        mAudioManager.abandonAudioFocus(mAudioFocusListener);
        stopTimer();
    }

    public void stopTimer() {
        if (mProgressUpdater != null) {
            mProgressUpdater.cancel();
            mProgressUpdater = null;
        }
    }

    Loaders.MusicInfoLoader musicInfoLoader;
    Loaders.AlbumCoverLoader albumCoverLoader;

    public void playTrack(@NonNull String path) {
        //TODO: Переписать, сделать более плавное обновление на экране
        stopTimer();
        if (mMusicPlayer.isPlaying()) {
            MusicModel.setCurrentTrackProgress(0);
        }
        if (musicInfoLoader != null) {
            musicInfoLoader.cancel(true);
            musicInfoLoader = null;
        }
        if (albumCoverLoader != null) {
            albumCoverLoader.cancel(true);
            albumCoverLoader = null;
        }

        try {
            if (mProgressUpdater != null) {
                throw new IllegalStateException("Track progress updater is running. Stop it!");
            }
            mMusicPlayer.reset();

            System.out.println(path);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mMusicPlayer.setDataSource(this, Uri.parse(path));
            } else {
                mMusicPlayer.setDataSource(path);
            }

            mMusicPlayer.prepareAsync();
            startTimer();

            musicInfoLoader = new Loaders.MusicInfoLoader(this, path);
            musicInfoLoader.setFinishCallback(data -> {
                playerManager.rememberPath(data.getPath());
                GlobalModel.updateTrackText(data);
            });
            musicInfoLoader.setCancelCallback(GlobalModel::updateTrackText);
            musicInfoLoader.execute();
            albumCoverLoader =
                    new Loaders.AlbumCoverLoader(this, path, Theme.getDefaultAlbumCoverCopy());
            albumCoverLoader.setFinishCallback(MusicModel::setCurrentTrackCover);
            albumCoverLoader.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeService() {
        unregisterReceiver(mPlayerReceiver);
        unregisterReceiver(mHeadphoneMuteReceiver);
        hidePlayer();
        stopService(mServiceIntent);
        mMusicPlayer.release();
        stopSelf();
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
