package ru.reactiveturtle.reactivemusic.player;

import android.media.AudioManager;
import android.util.Log;

public final class AudioFocusManager {
    private final AudioFocusListener audioFocusListener;

    public AudioFocusManager(MusicPlayerProvider musicPlayerProvider) {
        audioFocusListener = new AudioFocusListener(musicPlayerProvider);
    }

    public void focus(AudioManager audioManager) {
        audioManager.requestAudioFocus(audioFocusListener,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
    }

    public void freeFocus(AudioManager audioManager) {
        audioManager.abandonAudioFocus(audioFocusListener);
    }

    private static class AudioFocusListener implements AudioManager.OnAudioFocusChangeListener {
        MusicPlayerProvider musicPlayerProvider;

        public AudioFocusListener(MusicPlayerProvider musicPlayerProvider) {
            this.musicPlayerProvider = musicPlayerProvider;
        }

        @Override
        public void onAudioFocusChange(int focusChange) {
            String event = "";
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    musicPlayerProvider.pause();
                    event = "AUDIOFOCUS_LOSS";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    musicPlayerProvider.pause();
                    event = "AUDIOFOCUS_LOSS_TRANSIENT";
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    musicPlayerProvider.setVolume(0.4f, 0.4f);
                    event = "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK";
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    event = "AUDIOFOCUS_GAIN";
                    musicPlayerProvider.setVolume(1f, 1f);
                    musicPlayerProvider.play();
                    break;
            }
            Log.d("Audio focus changed",  event);
        }
    }
}
