package ru.reactiveturtle.reactivemusic.player;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Objects;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.player.repository.PlayerRepository;
import ru.reactiveturtle.reactivemusic.player.shared.MusicAlbumCoverData;
import ru.reactiveturtle.reactivemusic.toolkit.ReactiveList;
import ru.reactiveturtle.reactivemusic.toolkit.Releasable;

public final class MusicPlayer implements MusicPlayerProvider, Releasable {
    private Context context;
    private PlayerRepository playerRepository;

    private AudioManager audioManager;

    private boolean isPrepared = false;
    private MediaPlayer mediaPlayer;

    private AllMusicDataManager allMusicDataManager;
    private MusicPlayerProgressUpdater musicPlayerProgressUpdater;
    private MusicTracksNavigator musicTracksNavigator;
    private AudioFocusManager audioFocusManager;
    private HeadphoneManager headphoneManager;

    public MusicPlayer(Context context, AllMusicDataManager allMusicDataManager) {
        this.context = context;
        this.playerRepository = new PlayerRepository(context);

        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        this.mediaPlayer = new MediaPlayer();
        this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        MediaPlayer.OnPreparedListener mainPreparedListener = mediaPlayer -> {
            isPrepared = true;
            int duration = mediaPlayer.getDuration();
            Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onPrepared(duration)));
            play();
        };
        this.mediaPlayer.setOnPreparedListener(mediaPlayer -> {
            isPrepared = true;
            int duration = mediaPlayer.getDuration();
            Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onPrepared(duration)));
            mediaPlayer.setOnPreparedListener(mainPreparedListener);
        });
        this.mediaPlayer.setOnCompletionListener(mediaPlayer -> {
            if (!mediaPlayer.isLooping()) {
                int duration = mediaPlayer.getDuration();
                Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onProgressChanged(duration)));
                loadTrack(musicTracksNavigator.getNextTrack());
            }
        });

        this.allMusicDataManager = allMusicDataManager;

        this.musicPlayerProgressUpdater = new MusicPlayerProgressUpdater();
        this.musicPlayerProgressUpdater.setCallback(() -> {
            int progress = mediaPlayer.getCurrentPosition();
            Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onProgressChanged(progress)));
        });

        this.musicTracksNavigator = new MusicTracksNavigator(playerRepository, context, allMusicDataManager);

        audioFocusManager = new AudioFocusManager(this);
        headphoneManager = new HeadphoneManager(context, audioManager, this);
        //headphoneManager.register();
        allMusicDataManager.addPathsLoadListener(() -> {
            String currentTrackPath = playerRepository.getCurrentTrackPath();
            if (currentTrackPath == null) {
                ReactiveList<String> paths = allMusicDataManager.getPaths();
                if (paths.size() > 0) {
                    currentTrackPath = paths.get(0);
                }
            }
            if (currentTrackPath != null) {
                loadTrack(currentTrackPath);
            }
        });
    }

    @Override
    public synchronized void loadTrack(@NonNull String path) {
        Objects.requireNonNull(path);
        musicPlayerProgressUpdater.stop();
        mediaPlayer.reset();

        if (trySetDataSource(path)) {
            playerRepository.setCurrentTrackPath(path);
            mediaPlayer.prepareAsync();
            allMusicDataManager.requestData(path, new AllMusicDataManager.RequestCallback() {
                @Override
                public void onDataLoad(MusicData musicData) {
                }

                @Override
                public void onMetadataLoad(MusicMetadata musicMetadata) {
                    Objects.requireNonNull(musicMetadata);
                    Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onMusicMetadataLoad(musicMetadata)));
                }

                @Override
                public void onCoverDataLoad(MusicAlbumCoverData musicAlbumCoverData) {
                    Objects.requireNonNull(musicAlbumCoverData);
                    Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onMusicCoverDataLoad(musicAlbumCoverData)));
                }
            });
        }
    }

    @Override
    public synchronized void loadPreviousTrack() {
        loadTrack(musicTracksNavigator.getPreviousTrack());
    }

    @Override
    public synchronized void loadNextTrack() {
        loadTrack(musicTracksNavigator.getNextTrack());
    }

    @Override
    public synchronized void play() {
        mediaPlayer.start();
        musicPlayerProgressUpdater.start();
        audioFocusManager.focus(audioManager);
        Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(MusicPlayerListener::onPlay));
    }

    @Override
    public synchronized void pause() {
        mediaPlayer.pause();
        musicPlayerProgressUpdater.stop();
        audioFocusManager.freeFocus(audioManager);
        Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(MusicPlayerListener::onPause));
    }

    @Override
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    @Override
    public void setLooping(boolean isLooping) {
        mediaPlayer.setLooping(isLooping);
        Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onLoopingChanged(isLooping)));
    }

    @Override
    public boolean isLooping() {
        return mediaPlayer.isLooping();
    }

    @Override
    public void setPlayRandomTrack(boolean isPlayRandomTrack) {
        playerRepository.setPlayRandomTrack(isPlayRandomTrack);
        Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onPlayRandomTrackChanged(isPlayRandomTrack)));
    }

    @Override
    public boolean isPlayRandomTrack() {
        return playerRepository.getPlayerModel().isPlayRandomTrack();
    }

    @Override
    public void setProgress(int milliseconds) {
        mediaPlayer.seekTo(milliseconds);
        Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onProgressChanged(milliseconds)));
    }

    @Override
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    @Override
    public void setVolume(float left, float right) {
        mediaPlayer.setVolume(left, right);
    }

    @Override
    public MusicData getCurrentMusicData() {
        String currentMusicPath = playerRepository.getPlayerModel().getCurrentMusicPath();
        int musicPathIndex = allMusicDataManager.getPaths().indexOf(currentMusicPath);
        if (musicPathIndex == -1 && allMusicDataManager.getPaths().size() > 0) {
            musicPathIndex = 0;
        }
        return allMusicDataManager.getMusicData(musicPathIndex);
    }

    @Override
    public void release() {
        allMusicDataManager.release();
        mediaPlayer.release();
    }

    private ReactiveList<MusicPlayerListener> musicPlayerListeners = new ReactiveList<>();

    @Override
    public void addMusicPlayerListener(MusicPlayerListener musicPlayerListener) {
        Objects.requireNonNull(musicPlayerListener);
        this.allMusicDataManager.addPathsLoadListener(() -> {
            MusicData musicData = getCurrentMusicData();
            if (musicData.getMetadata() != null) {
                Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onMusicMetadataLoad(musicData.getMetadata())));
            }
            if (musicData.getCoverData() != null) {
                Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onMusicCoverDataLoad(musicData.getCoverData())));
            }
            if (isPrepared) {
                Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onPrepared(mediaPlayer.getDuration())));
            }
            Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onLoopingChanged(mediaPlayer.isLooping())));
            Helper.goToMainLooper(() -> musicPlayerListeners.forEachP(x -> x.onPlayRandomTrackChanged(playerRepository.isPlayRandomTrack())));
        });
        this.musicPlayerListeners.add(musicPlayerListener);
    }

    @Override
    public void removeMusicPlayerListener(MusicPlayerListener musicPlayerListener) {
        Objects.requireNonNull(musicPlayerListener);
        this.musicPlayerListeners.remove(musicPlayerListener);
    }

    public AllMusicDataManager getAllMusicDataManager() {
        return allMusicDataManager;
    }

    private synchronized boolean trySetDataSource(String path) {
        System.out.println(path);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                mediaPlayer.setDataSource(context, Uri.parse(path));
            } else {
                mediaPlayer.setDataSource(path);
            }
            isPrepared = false;
            return true;
        } catch (IOException e) {
            Toast.makeText(context, "Не удаётся загрузить музыкальную дорожку", Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
