package ru.reactiveturtle.reactivemusic.player;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaSession2;
import android.media.session.MediaSession;

import androidx.annotation.NonNull;

import java.util.Objects;

import ru.reactiveturtle.reactivemusic.musicservice.HeadphoneClickReceiver;
import ru.reactiveturtle.reactivemusic.musicservice.HeadphoneMuteReceiver;
import ru.reactiveturtle.reactivemusic.toolkit.Releasable;

public final class HeadphoneManager implements Releasable {
    private Context context;
    private AudioManager audioManager;

    private HeadphoneMuteReceiver headphoneMuteReceiver;
    private HeadphoneClickReceiver headphoneClickReceiver;

    public HeadphoneManager(Context context,
                            AudioManager audioManager,
                            MusicPlayerProvider musicPlayerProvider) {
        Objects.requireNonNull(audioManager);
        Objects.requireNonNull(context);
        this.audioManager = audioManager;
        this.context = context;
        headphoneMuteReceiver = new HeadphoneMuteReceiver(musicPlayerProvider);
        headphoneClickReceiver = new HeadphoneClickReceiver(musicPlayerProvider);
    }

    public void register() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            MediaSession mediaSession = new MediaSession(context, "MEDIA_SESSION");
            mediaSession.setCallback(new MediaSession.Callback() {
                @Override
                public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {

                    return super.onMediaButtonEvent(mediaButtonIntent);
                }
            });
        } else {
            Intent receiverIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, receiverIntent, 0);
            audioManager.registerMediaButtonEventReceiver(pendingIntent);
        }
        headphoneMuteReceiver.register(context, Intent.ACTION_HEADSET_PLUG);
        headphoneClickReceiver.register(context, null);
    }

    @Override
    public void release() {
        headphoneMuteReceiver.unregister(context);
        headphoneClickReceiver.unregister(context);
        context = null;
        audioManager = null;
    }
}
