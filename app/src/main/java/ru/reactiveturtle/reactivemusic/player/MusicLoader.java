package ru.reactiveturtle.reactivemusic.player;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import ru.reactiveturtle.reactivemusic.player.loaders.MusicAlbumCoverLoader;
import ru.reactiveturtle.reactivemusic.player.shared.MusicMetadataFromDatabase;
import ru.reactiveturtle.reactivemusic.player.shared.MusicMetadataFromMediaMetadataRetreiver;
import ru.reactiveturtle.reactivemusic.toolkit.ThreadWatcher;

public final class MusicLoader {
    private Context context;

    private MusicMetadataBuilder musicMetadataBuilder;
    private BitmapDrawable musicCover;

    private Thread musicInfoThread;
    private ThreadWatcher musicInfoThreadWatcher;

    private Thread musicCoverThread;
    private ThreadWatcher musicCoverThreadWatcher;

    public MusicLoader(Context context) {
        this.context = context;
    }

    public synchronized void load(@NonNull String path) {
        if (musicInfoThread != null || musicCoverThread != null) {
            throw new IllegalStateException("Loader already started");
        }
        this.musicMetadataBuilder = new MusicMetadataBuilder();
        Objects.requireNonNull(path);
        path = getTrackPath(path);
        startMusicInfoThread(path);
        startMusicCoverThread(path);
    }

    public void reset() {
        if (musicInfoThread != null) {
            musicInfoThreadWatcher.stopObserving();
            musicInfoThread.interrupt();
            musicInfoThread = null;
            musicInfoThreadWatcher = null;
        }
        if (musicCoverThread != null) {
            musicCoverThreadWatcher.stopObserving();
            musicCoverThread.interrupt();
            musicCoverThread = null;
            musicCoverThreadWatcher = null;
        }
    }

    public boolean isInfoLoaded() {
        return musicInfoThreadWatcher.isEnd();
    }

    public boolean isCoverLoaded() {
        return musicCoverThreadWatcher.isEnd();
    }

    private MusicLoadListener musicLoadListener;

    public void setMusicLoadListener(MusicLoadListener musicLoadListener) {
        this.musicLoadListener = musicLoadListener;
    }

    public void release() {
        reset();
        context = null;
        musicMetadataBuilder = null;
    }

    public interface MusicLoadListener {
        void onMusicMetadataLoadFinish(@NonNull MusicMetadata musicMetadata);

        void onMusicCoverLoadFinish(@Nullable BitmapDrawable musicCover);
    }

    private void startMusicInfoThread(String path) {
        musicInfoThread = new Thread(() -> {
            MusicMetadata musicMetadata = MusicMetadataFromMediaMetadataRetreiver.load(context, path);
            if (musicMetadata == null) {
                musicMetadata = MusicMetadata.notFound();
            }
            musicMetadataBuilder.setPath(musicMetadata.getPath());
            musicMetadataBuilder.setArtist(musicMetadata.getArtist());
            musicMetadataBuilder.setTitle(musicMetadata.getTitle());
        });
        musicInfoThreadWatcher = new ThreadWatcher(musicInfoThread);
        musicInfoThreadWatcher.setThreadListener(() -> {
            if (musicLoadListener != null) {
                musicLoadListener.onMusicMetadataLoadFinish(musicMetadataBuilder.build());
            }
        });
        musicInfoThread.start();
        musicInfoThreadWatcher.observe();
    }

    private void startMusicCoverThread(String path) {
        musicCoverThread = new Thread(() -> musicCover = MusicAlbumCoverLoader.load(context, path));
        musicCoverThreadWatcher = new ThreadWatcher(musicInfoThread);
        musicCoverThreadWatcher.setThreadListener(() -> {
            if (musicLoadListener != null) {
                musicLoadListener.onMusicCoverLoadFinish(musicCover);
            }
        });
        musicCoverThread.start();
        musicCoverThreadWatcher.observe();
    }

    private String getTrackPath(String path) {
        return path;
    }
}
