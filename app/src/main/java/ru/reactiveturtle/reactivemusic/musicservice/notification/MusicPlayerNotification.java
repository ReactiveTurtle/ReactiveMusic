package ru.reactiveturtle.reactivemusic.musicservice.notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.Objects;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.musicservice.MusicService;
import ru.reactiveturtle.reactivemusic.player.MusicData;
import ru.reactiveturtle.reactivemusic.player.MusicMetadata;
import ru.reactiveturtle.reactivemusic.player.MusicPlayerProvider;
import ru.reactiveturtle.reactivemusic.theme.Theme;
import ru.reactiveturtle.reactivemusic.toolkit.Releasable;

public class MusicPlayerNotification implements Releasable {
    private static final String CHANNEL_NAME = "Music Channel";

    private MusicServiceNotificationProvider musicServiceNotificationProvider;
    private Context context;
    private Theme theme;
    private NotificationManager notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private MusicPlayerNotificationBroadcastReceiver notificationBroadcastReceiver;
    private MusicPlayerProvider musicPlayerProvider;

    public MusicPlayerNotification(Context context,
                                   Theme theme,
                                   MusicServiceNotificationProvider musicServiceNotificationProvider,
                                   MusicPlayerProvider musicPlayerProvider) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(theme);
        Objects.requireNonNull(musicServiceNotificationProvider);
        Objects.requireNonNull(musicPlayerProvider);

        this.context = context;
        this.theme = theme;
        this.musicServiceNotificationProvider = musicServiceNotificationProvider;
        this.musicPlayerProvider = musicPlayerProvider;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.notificationBroadcastReceiver = new MusicPlayerNotificationBroadcastReceiver(musicServiceNotificationProvider, musicPlayerProvider);
        initNotification();
    }

    private void initNotification() {
        NotificationChannel channel;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_NAME, CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(CHANNEL_NAME);
            channel.enableLights(true);
            channel.setLightColor(Color.CYAN);
            channel.enableVibration(false);
            notificationManager.createNotificationChannel(channel);
            notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_NAME);
        } else {
            notificationBuilder = new NotificationCompat.Builder(context);
        }

        Intent intent = new Intent(getClass().getName());
        intent.putExtra(Helper.ACTION_EXTRA, MusicPlayerNotificationBroadcastReceiver.Action.SHOW_ACTIVITY);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MusicPlayerNotificationBroadcastReceiver.Action.SHOW_ACTIVITY.getValue(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        notificationBuilder
                .setSmallIcon(R.drawable.ic_note)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(context.getResources().getColor(R.color.colorPrimary))
                .setContentIntent(pendingIntent)
                .setAutoCancel(false)
                .setOngoing(true);
    }

    public void showPlayer() {
        RemoteViews remoteViews = new RemoteViews(MusicService.class.getName(), R.layout.player_music_notification_fragment);

        MusicData currentMusicData = musicPlayerProvider.getCurrentMusicData();
        MusicMetadata currentMusicMetadata = currentMusicData.getMetadata();
        Objects.requireNonNull(currentMusicData.getCoverData());
        Objects.requireNonNull(currentMusicMetadata);
        remoteViews.setBitmap(
                R.id.playerNotificationAlbum,
                "setImageBitmap",
                currentMusicData.getCoverData().getCover().getBitmap());
        remoteViews.setTextViewText(R.id.playerNotificationTitle, currentMusicMetadata.getTitle());
        remoteViews.setTextViewText(R.id.playerNotificationInfo,
                currentMusicMetadata.getArtist() + " | " + currentMusicMetadata.getAlbum());
        remoteViews.setInt(
                R.id.playerNotificationPlayPause,
                "setBackgroundResource",
                R.drawable.play_button);

        Intent intent = new Intent(getClass().getName());
        intent.putExtra(Helper.ACTION_EXTRA, MusicPlayerNotificationBroadcastReceiver.Action.CLOSE_SERVICE);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, MusicPlayerNotificationBroadcastReceiver.Action.CLOSE_SERVICE.getValue(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.playerNotificationCloseButton, pendingIntent);

        intent = new Intent(getClass().getName());
        intent.putExtra(Helper.ACTION_EXTRA, MusicPlayerNotificationBroadcastReceiver.Action.PREVIOUS);
        pendingIntent = PendingIntent.getBroadcast(context, MusicPlayerNotificationBroadcastReceiver.Action.PREVIOUS.getValue(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.playerNotificationPreviousTrack, pendingIntent);

        intent = new Intent(getClass().getName());
        intent.putExtra(Helper.ACTION_EXTRA, MusicPlayerNotificationBroadcastReceiver.Action.PLAY_PAUSE);
        pendingIntent = PendingIntent.getBroadcast(context, MusicPlayerNotificationBroadcastReceiver.Action.PLAY_PAUSE.getValue(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.playerNotificationPlayPause, pendingIntent);

        intent = new Intent(getClass().getName());
        intent.putExtra(Helper.ACTION_EXTRA, MusicPlayerNotificationBroadcastReceiver.Action.NEXT);
        pendingIntent = PendingIntent.getBroadcast(context, MusicPlayerNotificationBroadcastReceiver.Action.NEXT.getValue(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews.setOnClickPendingIntent(R.id.playerNotificationNextTrack, pendingIntent);

        notificationBuilder.setCustomBigContentView(remoteViews);

        musicServiceNotificationProvider.show(notificationBuilder.build());

        notificationBroadcastReceiver.register(context, getClass().getName());
    }

    public void hidePlayer() {
        musicServiceNotificationProvider.hide();
        notificationBroadcastReceiver.unregister(context);
    }

    @Override
    public void release() {
        hidePlayer();
    }
}
