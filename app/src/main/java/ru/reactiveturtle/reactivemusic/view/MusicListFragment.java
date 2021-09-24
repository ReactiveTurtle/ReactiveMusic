package ru.reactiveturtle.reactivemusic.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.Unbinder;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.musicservice.MusicService;
import ru.reactiveturtle.reactivemusic.player.AllMusicDataManager;
import ru.reactiveturtle.reactivemusic.player.MusicMetadata;
import ru.reactiveturtle.reactivemusic.player.MusicPlayerProvider;
import ru.reactiveturtle.reactivemusic.theme.Theme;
import ru.reactiveturtle.reactivemusic.theme.ThemeDependent;

public class MusicListFragment extends Fragment implements ThemeDependent {
    private Unbinder unbinder;

    public static MusicListFragment newInstance() {

        Bundle args = new Bundle();

        MusicListFragment fragment = new MusicListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private MusicListController musicListController;

    @BindView(R.id.playerSmallPreviousTrack)
    protected Button previousTrack;
    @BindView(R.id.playerSmallPlayPause)
    protected Button playPause;
    @BindView(R.id.playerSmallNextTrack)
    protected Button nextTrack;

    @BindView(R.id.playerSmallTitle)
    protected TextView title;
    @BindView(R.id.playerSmallInfo)
    protected TextView info;

    private MusicPlayerProvider musicPlayerProvider;
    private Theme theme;
    private AllMusicDataManager allMusicDataManager;
    private MusicPlayerProvider.MusicPlayerListener musicPlayerListener;

    public void bind(MusicService.Binder musicServiceBinder) {
        unbind(musicServiceBinder);
        this.musicPlayerProvider = musicServiceBinder.getPlayer();
        this.theme = musicServiceBinder.getTheme();
        this.allMusicDataManager = musicServiceBinder.getAllMusicDataManager();
        this.musicPlayerListener = new MusicPlayerProvider.MusicPlayerListener() {
            @Override
            public void onPlay() {
                play();
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onMusicMetadataLoad(@NonNull MusicMetadata musicMetadata) {
                showCurrentTrack(musicMetadata);
            }
        };
        if (getView() != null) {
            this.musicPlayerProvider.addMusicPlayerListener(musicPlayerListener);
        }
        theme.addThemeDependent(this);
        tryInitMusicListController();
        tryInitTheme();
    }

    private void unbind(MusicService.Binder musicServiceBinder) {
        if (musicPlayerListener != null) {
            musicServiceBinder.getPlayer().removeMusicPlayerListener(musicPlayerListener);
            theme.removeThemeDependent(this);
            musicListController.setAllMusicDataManager(null);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.player_music_list_fragment, container);
        musicListController = new MusicListController(view);
        tryInitMusicListController();
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (musicPlayerProvider != null) {
            this.musicPlayerProvider.addMusicPlayerListener(musicPlayerListener);
        }

        previousTrack.setOnClickListener((v) -> {
            musicPlayerProvider.loadPreviousTrack();
        });

        playPause.setOnClickListener((v) -> {
            if (!musicPlayerProvider.isPlaying()) {
                musicPlayerProvider.play();
            } else {
                musicPlayerProvider.pause();
            }
        });

        nextTrack.setOnClickListener((v) -> {
            musicPlayerProvider.loadNextTrack();
        });
        tryInitTheme();
    }

    @Override
    public void onThemeUpdate(Theme theme) {
        musicListController.updateTheme(theme);
        if (!musicPlayerProvider.isPlaying()) {
            playPause.setBackground(theme.getIconManager().getRoundPlayIcon());
        } else {
            playPause.setBackground(theme.getIconManager().getRoundPauseIcon());
        }
    }

    @Override
    public void onThemeContextUpdate(Theme theme) {
        previousTrack.setBackground(theme.getIconManager().getPreviousIcon());
        nextTrack.setBackground(theme.getIconManager().getNextIcon());
        title.setTextColor(theme.getThemeContext().getNegativePrimary());
        info.setTextColor(theme.getThemeContext().getNegativePrimary());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void play() {
        playPause.setBackground(theme.getIconManager().getRoundPauseIcon());
    }

    public void pause() {
        playPause.setBackground(theme.getIconManager().getRoundPlayIcon());
    }


    private void showCurrentTrack(MusicMetadata musicMetadata) {
        title.setText(musicMetadata.getTitle());
        String info = String.format("%s | %s", musicMetadata.getArtist(), musicMetadata.getAlbum());
        this.info.setText(info);
        musicListController.setCurrentTrackPath(musicMetadata.getPath());
    }

    @OnClick(R.id.playerSmallClicker)
    protected void scrollToCurrentTrack() {
        // TODO
    }

    @OnLongClick(R.id.playerSmallClicker)
    protected void findCurrentTrackInPrimaryPlaylist() {
        // TODO
    }

    private void tryInitTheme() {
        if (musicListController != null && theme != null) {
            onThemeUpdate(theme);
            onThemeContextUpdate(theme);
        }
    }

    private void tryInitMusicListController() {
        if (musicListController != null && allMusicDataManager != null) {
            musicListController.setAllMusicDataManager(allMusicDataManager);
        }
        if (musicListController != null && musicPlayerProvider != null) {
            musicListController.setMusicPlayerProvider(musicPlayerProvider);
        }
    }
}