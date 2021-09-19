package ru.reactiveturtle.reactivemusic.player;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.Objects;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.player.loaders.TrackPathsLoader;
import ru.reactiveturtle.reactivemusic.player.shared.MusicAlbumCoverData;
import ru.reactiveturtle.reactivemusic.theme.Theme;
import ru.reactiveturtle.reactivemusic.toolkit.ReactiveList;

public class AllMusicDataManager {
    @NonNull
    private final Context context;
    @NonNull
    private final Theme theme;

    private ReactiveList<String> paths = new ReactiveList<>();
    private ReactiveList<MusicMetadata> metadataList = new ReactiveList<>();
    private ReactiveList<MusicAlbumCoverData> coverDataList = new ReactiveList<>();
    private ReactiveList<Integer> durations = new ReactiveList<>();

    private ReactiveList<MusicLoader> activeMusicLoaders = new ReactiveList<>();
    private ReactiveList<RequestItem> requests = new ReactiveList<>();

    private ReactiveList<PathsLoadListener> pathsLoadListeners = new ReactiveList<>();
    private boolean isPathsLoaded = false;

    public AllMusicDataManager(@NonNull Context context, @NonNull Theme theme) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(theme);
        this.context = context;
        this.theme = theme;

        TrackPathsLoader trackPathsLoader = new TrackPathsLoader(context);
        trackPathsLoader.setTrackPathsLoadListener(trackPaths -> {
            paths.addAll(trackPaths);
            trackPaths.forEachP(x -> {
                metadataList.add(null);
                coverDataList.add(null);
                durations.add(null);
            });
            isPathsLoaded = true;

            pathsLoadListeners.forEachP(x -> Helper.goToMainLooper(x::onLoad));
            pathsLoadListeners.clear();
        });
        trackPathsLoader.load();
    }

    public void addPathsLoadListener(@NonNull PathsLoadListener pathsLoadListener) {
        Objects.requireNonNull(pathsLoadListener);
        if (isPathsLoaded) {
            pathsLoadListener.onLoad();
            return;
        }
        pathsLoadListeners.add(pathsLoadListener);
    }

    public void removePathsLoadListener(@NonNull PathsLoadListener pathsLoadListener) {
        Objects.requireNonNull(pathsLoadListener);
        pathsLoadListeners.remove(pathsLoadListener);
    }

    public synchronized void requestData(@NonNull String path, @NonNull RequestCallback requestCallback) {
        Objects.requireNonNull(requestCallback);
        Objects.requireNonNull(path);
        int index = paths.indexOf(path);
        if (index == -1) {
            throw new IllegalStateException(String.format("Undeclared path \"%s\".", path));
        }

        requestData(index, requestCallback);
    }

    public synchronized void requestData(int index, @NonNull RequestCallback requestCallback) {
        MusicData musicData = getMusicData(index);
        if (musicData.getMetadata() != null) {
            Objects.requireNonNull(musicData.getMetadata());
            requestCallback.onMetadataLoad(musicData.getMetadata());
        }
        if (musicData.getCoverData() != null) {
            Objects.requireNonNull(musicData.getCoverData());
            requestCallback.onCoverDataLoad(musicData.getCoverData());
        }
        if (musicData.getMetadata() != null && musicData.getCoverData() != null) {
            requestCallback.onDataLoad(getMusicData(index));
            return;
        }
        if (activeMusicLoaders.size() < 10) {
            loadMusic(new RequestItem(index, requestCallback));
        } else {
            requests.add(new RequestItem(index, requestCallback));
        }
    }

    public MusicData getMusicData(int index) {
        Integer duration = durations.get(index);
        return new MusicData(
                metadataList.get(index),
                coverDataList.get(index),
                duration == null ? -1 : duration);
    }

    public ReactiveList<String> getPaths() {
        return new ReactiveList<>(Collections.unmodifiableList(paths));
    }

    public void recycle(int index) {
        requests.removeAll(x -> x.pathIndex == index);
        metadataList.set(index, null);
        coverDataList.set(index, null);
    }

    public void release() {
        for (MusicLoader loader : activeMusicLoaders) {
            loader.release();
        }
        requests.clear();
        coverDataList.forEachP(x -> x.getCover().getBitmap().recycle());
    }

    private synchronized void loadMusic(RequestItem requestItem) {
        int index = requestItem.getPathIndex();
        String path = paths.get(index);

        MusicLoader musicLoader = new MusicLoader(context);
        activeMusicLoaders.add(musicLoader);
        musicLoader.setMusicLoadListener(new MusicLoader.MusicLoadListener() {
            @Override
            public void onMusicMetadataLoadFinish(@NonNull MusicMetadata musicMetadata) {
                AllMusicDataManager.this.metadataList.set(index, musicMetadata);
                Helper.goToMainLooper(() -> requestItem.getRequestCallback().onMetadataLoad(musicMetadata));
                tryLoadNext(musicLoader, requestItem);
            }

            @Override
            public void onMusicCoverLoadFinish(@Nullable BitmapDrawable musicCover) {
                BitmapDrawable finalMusicCover = musicCover == null ? theme.getDefaultAlbumCover() : musicCover;
                MusicAlbumCoverData albumCoverData = new MusicAlbumCoverData(musicCover != null, finalMusicCover);
                AllMusicDataManager.this.coverDataList.set(index, albumCoverData);
                Helper.goToMainLooper(() -> requestItem.getRequestCallback().onCoverDataLoad(albumCoverData));
                tryLoadNext(musicLoader, requestItem);
            }
        });
        musicLoader.load(path);
    }

    private synchronized void tryLoadNext(MusicLoader musicLoader, RequestItem previousRequestItem) {
        int previousLoadedPathIndex = previousRequestItem.getPathIndex();
        if (this.metadataList.get(previousLoadedPathIndex) == null || this.coverDataList.get(previousLoadedPathIndex) == null) {
            return;
        }
        activeMusicLoaders.remove(musicLoader);
        MusicData musicData = getMusicData(previousLoadedPathIndex);
        Helper.goToMainLooper(() -> previousRequestItem.getRequestCallback().onDataLoad(musicData));
        if (!requests.isEmpty()) {
            RequestItem requestItem = requests.last();
            requests.remove(requests.size() - 1);
            Helper.goToMainLooper(() -> requestData(requestItem.getPathIndex(), requestItem.getRequestCallback()));
        }
    }

    private static final class RequestItem {
        private final int pathIndex;
        private final RequestCallback requestCallback;

        private RequestItem(int pathIndex, RequestCallback requestCallback) {
            Objects.requireNonNull(requestCallback);
            this.pathIndex = pathIndex;
            this.requestCallback = requestCallback;
        }

        public int getPathIndex() {
            return pathIndex;
        }

        public RequestCallback getRequestCallback() {
            return requestCallback;
        }
    }

    public interface PathsLoadListener {
        void onLoad();
    }

    public interface RequestCallback {
        void onDataLoad(MusicData musicData);

        void onMetadataLoad(MusicMetadata musicMetadata);

        void onCoverDataLoad(MusicAlbumCoverData cover);
    }
}
