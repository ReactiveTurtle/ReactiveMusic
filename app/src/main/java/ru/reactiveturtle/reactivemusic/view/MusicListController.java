package ru.reactiveturtle.reactivemusic.view;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.util.Objects;

import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.AllMusicDataManager;
import ru.reactiveturtle.reactivemusic.player.MusicPlayerProvider;
import ru.reactiveturtle.reactivemusic.player.loaders.TrackPathsLoader;
import ru.reactiveturtle.reactivemusic.theme.Theme;

public class MusicListController {
    private TrackPathsLoader trackPathsLoader;
    private MusicPlayerProvider musicPlayerProvider;
    private TextView empty;
    private RecyclerView recyclerView;
    private MusicListAdapter musicListAdapter;

    public MusicListController(View root) {
        empty = root.findViewById(R.id.musicListEmpty);
        recyclerView = root.findViewById(R.id.player_music_list_recycler_view);
        LinearLayoutManager llm = new LinearLayoutManager(root.getContext());
        recyclerView.setLayoutManager(llm);
        musicListAdapter = new MusicListAdapter();
        musicListAdapter.setOnItemClickListener(musicData -> {
            Objects.requireNonNull(musicData.getMetadata());
            musicPlayerProvider.loadTrack(musicData.getMetadata().getPath());
        });
        recyclerView.setAdapter(musicListAdapter);

        trackPathsLoader = new TrackPathsLoader(root.getContext());
        trackPathsLoader.setTrackPathsLoadListener((tracks) -> {
            trackPathsLoader = null;
            if (tracks.size() <= 0) {
                empty.setVisibility(View.VISIBLE);
            }
        });
        trackPathsLoader.load();
    }

    public void setMusicPlayerProvider(MusicPlayerProvider musicPlayerProvider) {
        this.musicPlayerProvider = musicPlayerProvider;
    }

    public void setAllMusicDataManager(@Nullable AllMusicDataManager allMusicDataManager) {
        musicListAdapter.setAllMusicDataManager(allMusicDataManager);
    }

    public void setCurrentTrackPath(@NonNull String path) {
        musicListAdapter.setCurrentTrackPath(path);
    }

    public void updateTheme(Theme theme) {
        musicListAdapter.updateTheme(theme);
    }
}
