package ru.reactiveturtle.reactivemusic.view;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.musicservice.MusicService;
import ru.reactiveturtle.reactivemusic.player.MusicMetadata;
import ru.reactiveturtle.reactivemusic.player.MusicPlayerProvider;
import ru.reactiveturtle.reactivemusic.player.shared.MusicAlbumCoverData;
import ru.reactiveturtle.reactivemusic.theme.Theme;
import ru.reactiveturtle.reactivemusic.theme.ThemeDependent;
import ru.reactiveturtle.reactivemusic.toolkit.BitmapExtensions;

public class MusicFragment extends Fragment implements ThemeDependent {
    private Unbinder unbinder;

    public static MusicFragment newInstance() {

        Bundle args = new Bundle();

        MusicFragment fragment = new MusicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.playerSeekBar)
    protected SeekBar seekBar;

    @BindView(R.id.playerTrackProgress)
    protected TextView mTrackProgress;
    @BindView(R.id.playerTrackDuration)
    protected TextView mTrackDuration;

    @BindView(R.id.playerTrackAlbum)
    protected TextView mPlayerTrackAlbum;
    @BindView(R.id.playerTrackArtist)
    protected TextView mPlayerTrackArtist;

    @BindView(R.id.playerAlbumImage)
    protected View mPlayerAlbumImage;

    @BindView(R.id.playerTrackName)
    protected TextView mPlayerTrackName;

    @BindView(R.id.playerRandomTrack)
    protected CheckBox randomTrack;
    @BindView(R.id.playerPreviousTrack)
    protected Button mPreviousTrack;
    @BindView(R.id.playerPlayPause)
    protected Button playPause;
    @BindView(R.id.playerNextTrack)
    protected Button mNextTrack;
    @BindView(R.id.playerLoopingTrack)
    protected CheckBox loopingTrack;

    private MusicPlayerProvider musicPlayerProvider;
    private Theme theme;
    private MusicPlayerProvider.MusicPlayerListener musicPlayerListener;

    public void bind(MusicService.Binder musicServiceBinder) {
        unbind(musicServiceBinder);
        this.musicPlayerProvider = musicServiceBinder.getPlayer();
        this.theme = musicServiceBinder.getTheme();
        this.musicPlayerListener = new MusicPlayerProvider.MusicPlayerListener() {
            @Override
            public void onPrepared(int duration) {
                seekBar.setMax(duration);
                mTrackDuration.setText(getTime(duration, false));
            }

            @Override
            public void onPlay() {
                playPause.setBackground(theme.getIconManager().getRoundPauseIcon());
            }

            @Override
            public void onPause() {
                playPause.setBackground(theme.getIconManager().getRoundPlayIcon());
            }

            @Override
            public void onLoopingChanged(boolean isLooping) {
                loopingTrack.setChecked(isLooping);
            }

            @Override
            public void onPlayRandomTrackChanged(boolean isPlayRandomTrack) {
                randomTrack.setChecked(isPlayRandomTrack);
            }

            @Override
            public void onProgressChanged(int progress) {
                updateTrackProgressSafely(progress);
            }

            @Override
            public void onMusicMetadataLoad(@NonNull MusicMetadata musicMetadata) {
                updateMusicInfo(musicMetadata);
            }

            @Override
            public void onMusicCoverDataLoad(@NonNull MusicAlbumCoverData musicAlbumCoverData) {
                updateMusicCover(musicAlbumCoverData.getCover());
            }
        };
        if (getView() != null) {
            this.musicPlayerProvider.addMusicPlayerListener(musicPlayerListener);
        }
    }

    private void unbind(MusicService.Binder musicServiceBinder) {
        if (musicPlayerListener != null) {
            musicServiceBinder.getPlayer().removeMusicPlayerListener(musicPlayerListener);
            theme.removeThemeDependent(this);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.player_music_fragment, container);
        unbinder = ButterKnife.bind(this, view);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                setProgress(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsProgressLocked = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                musicPlayerProvider.setProgress(seekBar.getProgress());
                unlockTrackProgress();
            }
        });

        mPreviousTrack.setOnClickListener((v) -> {
            musicPlayerProvider.loadPreviousTrack();
        });

        playPause.setOnClickListener((v) -> {
            if (!musicPlayerProvider.isPlaying()) {
                musicPlayerProvider.play();
            } else {
                musicPlayerProvider.pause();
            }
        });

        mNextTrack.setOnClickListener((v) -> {
            musicPlayerProvider.loadNextTrack();
        });

        loopingTrack.setOnClickListener((v) -> {
            musicPlayerProvider.setLooping(!musicPlayerProvider.isLooping());
        });

        showEmptyContent();
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            int albumImageSize = (int) (getResources().getDisplayMetrics().widthPixels * 0.5f);
            mPlayerAlbumImage.getLayoutParams().width = albumImageSize;
            mPlayerAlbumImage.getLayoutParams().height = albumImageSize;
        } else {
            ViewTreeObserver vto = mPlayerAlbumImage.getViewTreeObserver();
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mPlayerAlbumImage.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mPlayerAlbumImage.getLayoutParams().width = mPlayerAlbumImage.getMeasuredHeight();
                }
            });
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (musicPlayerProvider != null) {
            this.musicPlayerProvider.addMusicPlayerListener(musicPlayerListener);
        }
        updateThemeContext();
        updateThemeColorSet();
    }

    @Override
    public void onThemeUpdate() {

    }

    @Override
    public void onThemeContextUpdate() {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        musicPlayerProvider.removeMusicPlayerListener(musicPlayerListener);
        unbinder.unbind();
    }

    @OnClick(R.id.playerRandomTrack)
    protected void switchPlayRandomTrack() {
        musicPlayerProvider.setPlayRandomTrack(!musicPlayerProvider.isPlayRandomTrack());
    }

    private boolean mIsProgressLocked = false;
    private boolean mIsProgressReadyToUnlock = false;

    public void updateTrackProgressSafely(int progress) {
        if (mIsProgressReadyToUnlock) {
            mIsProgressReadyToUnlock = false;
            mIsProgressLocked = false;
        }
        if (!mIsProgressLocked) {
            setProgress(progress);
        }
    }

    public void unlockTrackProgress() {
        mIsProgressReadyToUnlock = true;
    }

    private void setProgress(int progress) {
        seekBar.setProgress(progress);
        mTrackProgress.setText(getTime(progress, false));
    }

    private synchronized void updateMusicInfo(MusicMetadata musicMetadata) {
        mPlayerTrackAlbum.setBackgroundColor(Color.TRANSPARENT);
        mPlayerTrackArtist.setBackgroundColor(Color.TRANSPARENT);
        mPlayerTrackName.setBackgroundColor(Color.TRANSPARENT);
        mTrackProgress.setBackgroundColor(Color.TRANSPARENT);
        mTrackDuration.setBackgroundColor(Color.TRANSPARENT);

        mPlayerTrackAlbum.setText(musicMetadata.getAlbum());
        mPlayerTrackArtist.setText(musicMetadata.getArtist());
        mPlayerTrackName.setText(musicMetadata.getTitle());
    }

    public void clearCurrentMusic() {

    }

    public void updateThemeColorSet() {
        theme.updateSeekBar(seekBar);
        randomTrack.setBackground(theme.getIconManager().getRandomIcon());
        loopingTrack.setBackground(theme.getIconManager().getRepeatIcon());
        if (!musicPlayerProvider.isPlaying()) {
            playPause.setBackground(theme.getIconManager().getRoundPlayIcon());
        } else {
            playPause.setBackground(theme.getIconManager().getRoundPauseIcon());
        }
    }

    public void updateThemeContext() {
        if (mPlayerTrackArtist.getText().toString().equals("")) {
            showEmptyContent();
        }

        mPlayerTrackAlbum.setTextColor(theme.getThemeContext().getNegativePrimary());
        mPlayerTrackArtist.setTextColor(theme.getThemeContext().getNegativePrimary());
        mPlayerTrackName.setTextColor(theme.getThemeContext().getNegativePrimary());

        theme.updateSeekBar(seekBar);

        mTrackProgress.setTextColor(theme.getThemeContext().getNegativeSecondary());
        mTrackDuration.setTextColor(theme.getThemeContext().getNegativeSecondary());

        randomTrack.setBackground(theme.getIconManager().getRandomIcon());
        mPreviousTrack.setBackground(theme.getIconManager().getPreviousIcon());
        mNextTrack.setBackground(theme.getIconManager().getNextIcon());
        loopingTrack.setBackground(theme.getIconManager().getRepeatIcon());
    }

    private void showEmptyContent() {
        int color = theme.isDark() ? theme.getThemeContext().getLight() : theme.getThemeContext().getPrimaryLight();
        mPlayerTrackAlbum.setBackgroundColor(color);
        mPlayerTrackArtist.setBackgroundColor(color);
        mPlayerAlbumImage.setBackgroundColor(color);
        mPlayerTrackName.setBackgroundColor(color);
        mTrackProgress.setBackgroundColor(color);
        mTrackDuration.setBackgroundColor(color);
    }

    private void updateMusicCover(@NonNull BitmapDrawable cover) {
        mPlayerAlbumImage.setBackground(new BitmapDrawable(getResources(), BitmapExtensions.drawableToBitmap(cover)));
    }

    public static String getTime(long millis, boolean includesMillisAtEnd) {
        StringBuilder stringBuilder = new StringBuilder();
        long days = TimeUnit.MILLISECONDS.toDays(millis);
        if (days > 0) {
            millis -= TimeUnit.DAYS.toMillis(days);
            stringBuilder.append(days).append(":");
        }
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        if (hours > 0) {
            millis -= TimeUnit.HOURS.toMillis(hours);
            stringBuilder.append(hours).append(":");
        }

        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        millis -= TimeUnit.MINUTES.toMillis(minutes);
        stringBuilder.append(String.format(Locale.ENGLISH, "%02d", minutes)).append(":");

        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
        millis -= TimeUnit.SECONDS.toMillis(seconds);
        stringBuilder.append(String.format(Locale.ENGLISH, "%02d", seconds));

        if (includesMillisAtEnd) {
            stringBuilder.append(".").append(String.format(Locale.ENGLISH, "%03d", millis));
        }

        return stringBuilder.toString();
    }
}