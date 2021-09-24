package ru.reactiveturtle.reactivemusic.player.loaders;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import ru.reactiveturtle.reactivemusic.toolkit.ReactiveList;
import ru.reactiveturtle.reactivemusic.toolkit.ThreadWatcher;

public class TrackPathsLoader {
    private Context context;
    private Thread trackPathsThread;
    private ThreadWatcher trackPathsThreadWatcher;
    private ReactiveList<String> trackPaths;

    public TrackPathsLoader(Context context) {
        Objects.requireNonNull(context);
        this.context = context;
    }

    public synchronized void load() {
        if (trackPathsThread != null) {
            throw new IllegalStateException("Loader already started");
        }
        startTracksPathThread();
    }

    public void reset() {
        if (trackPathsThread != null) {
            trackPathsThreadWatcher.stopObserving();
            trackPathsThread.interrupt();
            trackPathsThread = null;
            trackPathsThreadWatcher = null;
        }
    }

    private TrackPathsLoadListener trackPathsLoadListener;

    public void setTrackPathsLoadListener(TrackPathsLoadListener trackPathsLoadListener) {
        this.trackPathsLoadListener = trackPathsLoadListener;
    }

    public void release() {
        reset();
    }

    public interface TrackPathsLoadListener {
        void onTracksPathLoadFinish(@NonNull ReactiveList<String> trackPaths);
    }

    private void startTracksPathThread() {
        trackPathsThread = new Thread(() -> {
            trackPaths = getAllTracksPathsInfo(context);
        });
        trackPathsThreadWatcher = new ThreadWatcher(trackPathsThread);
        trackPathsThreadWatcher.setThreadListener(() -> {
            if (trackPathsLoadListener != null) {
                trackPathsLoadListener.onTracksPathLoadFinish(trackPaths);
            }
        });
        trackPathsThread.start();
        trackPathsThreadWatcher.observe();
    }

    private static final List<String> allTracksPaths = new ArrayList<>();
    private static Timer allTracksPathsDestroyTimeout;

    @NonNull
    private static synchronized ReactiveList<String> getAllTracksPathsInfo(@NonNull Context context) {
        if (allTracksPathsDestroyTimeout == null) {
            Uri collection;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                collection = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
            } else {
                collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            String[] proj = {Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
                    MediaStore.Audio.Media._ID :
                    MediaStore.Audio.Media.DATA};
            Cursor cursor = context.getContentResolver().query(collection, proj,
                    MediaStore.Audio.Media.IS_MUSIC + " = 1", null, null);
            List<String> tracks = new ArrayList<>();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    int idColumn;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                        idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                    } else {
                        idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                    }
                    do {
                        String path;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                            long id = cursor.getLong(idColumn);
                            path = ContentUris.withAppendedId(
                                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id).toString();
                            System.out.println(path);
                        } else {
                            path = cursor.getString(idColumn);
                        }
                        tracks.add(path);
                    } while (cursor.moveToNext());
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            allTracksPaths.clear();
            allTracksPaths.addAll(tracks);
        }
        if (allTracksPathsDestroyTimeout != null) {
            allTracksPathsDestroyTimeout.cancel();
        }
        Collections.sort(allTracksPaths, (o1, o2) -> new File(o1).getName().compareTo(new File(o2).getName()));
        allTracksPathsDestroyTimeout = new Timer();
        allTracksPathsDestroyTimeout.schedule(new TimerTask() {
            @Override
            public void run() {
                allTracksPathsDestroyTimeout = null;
            }
        }, 60000);
        return new ReactiveList<>(Collections.unmodifiableList(allTracksPaths));
    }
}
