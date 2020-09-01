package ru.reactiveturtle.reactivemusic.player.mvp.view.music;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.BaseMusicContract;
import ru.reactiveturtle.reactivemusic.player.GlobalModel;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;

public class MusicFragment extends Fragment implements MusicContract.Fragment {
    private Unbinder unbinder;

    public static MusicFragment newInstance() {

        Bundle args = new Bundle();

        MusicFragment fragment = new MusicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @BindView(R.id.playerSeekBar)
    protected SeekBar mSeekBar;

    @BindView(R.id.playerTrackProgress)
    protected TextView mTrackProgress;
    @BindView(R.id.playerTrackDuration)
    protected TextView mTrackDuration;

    @BindView(R.id.playerTrackArtist)
    protected TextView mPlayerTrackArtist;
    @BindView(R.id.playerTrackAlbum)
    protected TextView mPlayerTrackAlbum;

    @BindView(R.id.playerAlbumImage)
    protected View mPlayerAlbumImage;

    @BindView(R.id.playerTrackName)
    protected TextView mPlayerTrackName;

    @BindView(R.id.playerPreviousTrack)
    protected Button mPreviousTrack;
    @BindView(R.id.playerPlayPause)
    protected Button mPlayPause;
    @BindView(R.id.playerNextTrack)
    protected Button mNextTrack;

    @BindView(R.id.playerRepeatTrack)
    protected CheckBox mRepeatTrack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.player_music_fragment, container);
        unbinder = ButterKnife.bind(this, view);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
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
                Objects.requireNonNull(mPresenter);
                GlobalModel.setTrackProgress(seekBar.getProgress(), false);
            }
        });

        mPreviousTrack.setOnClickListener((v) -> {
            Objects.requireNonNull(mPresenter);
            mPresenter.onPreviousTrack();
        });

        mPlayPause.setOnClickListener((v) -> {
            Objects.requireNonNull(mPresenter);
            mPresenter.onPlayPause();
        });

        mNextTrack.setOnClickListener((v) -> {
            Objects.requireNonNull(mPresenter);
            mPresenter.onNextTrack();
        });

        mRepeatTrack.setOnClickListener((v) -> {
            Objects.requireNonNull(mPresenter);
            mPresenter.onRepeatTrack();
        });
        mPresenter.onMusicFragmentAvailable(this);

        int color = Theme.IS_DARK ? Theme.CONTEXT_LIGHT : Theme.CONTEXT_PRIMARY_LIGHT;
        mPlayerTrackAlbum.setBackgroundColor(color);
        mPlayerTrackArtist.setBackgroundColor(color);
        mPlayerAlbumImage.setBackgroundColor(color);
        mPlayerTrackName.setBackgroundColor(color);
        mTrackProgress.setBackgroundColor(color);
        mTrackDuration.setBackgroundColor(color);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateThemeContext();
        updateTheme();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private MusicContract.Presenter mPresenter;

    @Override
    public void setPresenter(@NonNull BaseMusicContract.FragmentPresenter presenter) {
        mPresenter = (MusicContract.Presenter) presenter;
    }

    private boolean mIsProgressLocked = false;
    private boolean mIsProgressReadyToUnlock = false;

    @Override
    public void updateTrackProgressSafely(int progress) {
        if (mIsProgressReadyToUnlock) {
            mIsProgressReadyToUnlock = false;
            mIsProgressLocked = false;
        }
        if (!mIsProgressLocked) {
            setProgress(progress);
        }
    }

    @Override
    public void unlockTrackProgress() {
        mIsProgressReadyToUnlock = true;
    }

    private void setProgress(int progress) {
        mSeekBar.setProgress(progress);
        mTrackProgress.setText(getTime(progress));
    }

    @Override
    public void showCurrentTrack(MusicInfo musicInfo) {
        mPlayerTrackAlbum.setBackgroundColor(Color.TRANSPARENT);
        mPlayerTrackArtist.setBackgroundColor(Color.TRANSPARENT);
        mPlayerTrackName.setBackgroundColor(Color.TRANSPARENT);
        mTrackProgress.setBackgroundColor(Color.TRANSPARENT);
        mTrackDuration.setBackgroundColor(Color.TRANSPARENT);

        mPlayerTrackAlbum.setText(musicInfo.getAlbum());
        mPlayerTrackArtist.setText(musicInfo.getArtist());
        mPlayerAlbumImage.setBackground(
                new BitmapDrawable(getResources(), musicInfo.getAlbumImage().getBitmap()));
        mPlayerTrackName.setText(musicInfo.getTitle());

        mSeekBar.setMax(musicInfo.getDuration());
        mTrackDuration.setText(getTime(musicInfo.getDuration()));
    }

    @Override
    public void clearCurrentMusic() {

    }

    @Override
    public void startMusic() {
        mPlayPause.setBackground(Theme.getDefaultButtonDrawable(R.drawable.ic_pause));
    }

    @Override
    public void pauseMusic() {
        mPlayPause.setBackground(Theme.getDefaultButtonDrawable(R.drawable.ic_play));
    }

    @Override
    public void updateTheme() {
        Theme.updateSeekBar(mSeekBar);
        mRepeatTrack.setBackground(Theme.getCheckDrawable(
                R.drawable.ic_repeat_all, R.drawable.ic_repeat_one, VectorDrawableCompat.class));
    }

    @Override
    public void updateThemeContext() {
        if (mPlayerTrackArtist.getText().toString().equals("")) {
            int color = Theme.IS_DARK ? Theme.CONTEXT_LIGHT : Theme.CONTEXT_PRIMARY_LIGHT;
            mPlayerTrackAlbum.setBackgroundColor(color);
            mPlayerTrackArtist.setBackgroundColor(color);
            mPlayerAlbumImage.setBackgroundColor(color);
            mPlayerTrackName.setBackgroundColor(color);
            mTrackProgress.setBackgroundColor(color);
            mTrackDuration.setBackgroundColor(color);
        }

        mPlayerTrackAlbum.setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);
        mPlayerTrackArtist.setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);
        mPlayerTrackName.setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);

        Theme.updateSeekBar(mSeekBar);

        mTrackProgress.setTextColor(Theme.CONTEXT_SECONDARY_TEXT);
        mTrackDuration.setTextColor(Theme.CONTEXT_SECONDARY_TEXT);

        mPreviousTrack.setBackground(Theme.getDefaultButtonDrawable(R.drawable.ic_previous));
        mNextTrack.setBackground(Theme.getDefaultButtonDrawable(R.drawable.ic_next));
        mRepeatTrack.setBackground(Theme.getCheckDrawable(R.drawable.ic_repeat_all, R.drawable.ic_repeat_one,
                VectorDrawableCompat.class));
    }

    @Override
    public void updateTrackProgress(int progress) {
        setProgress(progress);
    }

    @Override
    public void repeatTrack(boolean isRepeat) {
        mRepeatTrack.setChecked(isRepeat);
    }

    private String getTime(int millis) {
        @SuppressLint("DefaultLocale") String result = String.format("%s:%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toDays(millis),
                TimeUnit.MILLISECONDS.toHours(millis) - TimeUnit.DAYS.toHours(TimeUnit.MILLISECONDS.toDays(millis)),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
        while (result.length() > 5) {
            if (isTimeUnitZero(result.split(":", 2)[0])) {
                result = removeFirstUnit(result);
            }
        }
        return result;
    }

    @NonNull
    private String removeFirstUnit(@NonNull String result) {
        return result.split(":", 2)[1];
    }

    private boolean isTimeUnitZero(@NonNull String timeUnit) {
        boolean isZero = true;
        for (int i = 0; i < timeUnit.length() && isZero; i++) {
            isZero = timeUnit.charAt(i) == '0';
        }
        return isZero;
    }
}
