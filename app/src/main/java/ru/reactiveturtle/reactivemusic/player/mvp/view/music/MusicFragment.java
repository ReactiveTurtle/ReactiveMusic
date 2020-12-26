package ru.reactiveturtle.reactivemusic.player.mvp.view.music;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerModel;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ThemeHelper;
import ru.reactiveturtle.reactivemusic.player.service.Bridges;
import ru.reactiveturtle.reactivemusic.player.service.MusicModel;
import ru.reactiveturtle.tools.reactiveuvm.Bridge;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;
import ru.reactiveturtle.tools.reactiveuvm.StateKeeper;
import ru.reactiveturtle.tools.reactiveuvm.fragment.ArchitectFragment;

public class MusicFragment extends ArchitectFragment {
    private Unbinder unbinder;

    public static MusicFragment newInstance() {

        Bundle args = new Bundle();

        MusicFragment fragment = new MusicFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @BindView(R.id.playerSeekBar)
    protected SeekBar mSeekBar;

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
    protected CheckBox mRandomTrack;
    @BindView(R.id.playerPreviousTrack)
    protected Button mPreviousTrack;
    @BindView(R.id.playerPlayPause)
    protected Button mPlayPause;
    @BindView(R.id.playerNextTrack)
    protected Button mNextTrack;
    @BindView(R.id.playerLoopingTrack)
    protected CheckBox mLoopingTrack;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_music_fragment, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, view);

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mIsProgressLocked = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                PlayerModel.setSeekBarTrackProgress(seekBar.getProgress());
                mIsProgressLocked = false;
            }
        });

        mPreviousTrack.setOnClickListener((v) ->
                ReactiveArchitect.getBridge(Bridges.PreviousTrackClick_To_PlayTrack).pull());
        mPlayPause.setOnClickListener((v) -> PlayerModel.switchPlayPause());
        mNextTrack.setOnClickListener((v) ->
                ReactiveArchitect.getBridge(Bridges.NextTrackClick_To_PlayTrack).pull());

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
        updateThemeContext();
        updateTheme();
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void onInitializeBinders(List<StateKeeper.Binder> container) {
        StateKeeper.Binder.Callback callback = (v, value) -> {
            v.setBackgroundColor(Color.TRANSPARENT);
            mTrackProgress.setBackgroundColor(Color.TRANSPARENT);
            mTrackDuration.setBackgroundColor(Color.TRANSPARENT);
        };

        container.addAll(Arrays.asList(
                ReactiveArchitect.getStateKeeper(MusicModel.CURRENT_TRACK_COVER)
                        .subscribe(mPlayerAlbumImage, "setBackground", Drawable.class).call(),
                ReactiveArchitect.getStateKeeper(MusicModel.CURRENT_TRACK_NAME)
                        .subscribe(mPlayerTrackName, "setText", CharSequence.class, callback).call(),
                ReactiveArchitect.getStateKeeper(MusicModel.CURRENT_TRACK_ARTIST)
                        .subscribe(mPlayerTrackArtist, "setText", CharSequence.class, callback).call(),
                ReactiveArchitect.getStateKeeper(MusicModel.CURRENT_TRACK_ALBUM)
                        .subscribe(mPlayerTrackAlbum, "setText", CharSequence.class, callback).call(),
                ReactiveArchitect.getStateKeeper(MusicModel.CURRENT_TRACK_DURATION)
                        .subscribe(mSeekBar, "setMax", int.class,
                                (v, value) -> mTrackDuration.setText(getTime((Integer) value)))
                        .call(),
                ReactiveArchitect.getStateKeeper(MusicModel.CURRENT_TRACK_PROGRESS)
                        .subscribe((v, value) -> updateTrackProgressSafely((int) value))
                        .call(),
                ReactiveArchitect.getStateKeeper(PlayerModel.IS_PLAY_RANDOM_TRACK)
                        .subscribe(mRandomTrack, "setChecked", boolean.class).call(),
                ReactiveArchitect.getStateKeeper(MusicModel.IS_TRACK_PLAY)
                        .subscribe(mPlayPauseCallback).call(),
                ReactiveArchitect.getStateKeeper(PlayerModel.IS_TRACK_LOOPING)
                        .subscribe(mLoopingTrack, "setChecked", boolean.class).call(),
                ReactiveArchitect.getStateKeeper(Theme.COLOR_SET).subscribe((view, value) -> updateTheme()),
                ReactiveArchitect.getStateKeeper(Theme.IS_DARK).subscribe((view, value) -> updateThemeContext()).call()
        ));
    }

    @Override
    protected void onInitializeBridges(List<Bridge> container) {

    }

    private StateKeeper.Binder.Callback mPlayPauseCallback = (view1, value) ->
            mPlayPause.setBackground(ThemeHelper.getDefaultRoundButtonDrawable(
                    ((boolean) value) ? R.drawable.ic_pause : R.drawable.ic_play));

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private boolean mIsProgressLocked = false;

    public void updateTrackProgressSafely(int progress) {
        if (!mIsProgressLocked) {
            setProgress(progress);
        }
    }

    protected void setProgress(int progress) {
        mSeekBar.setProgress(progress);
        mTrackProgress.setText(getTime(progress));
    }

    public void updateTheme() {
        if (Theme.getDefaultAlbumCover().getBitmap().equals(
                MusicModel.getCurrentTrackCover().getBitmap())) {
            mPlayerAlbumImage.setBackground(Theme.getDefaultAlbumCoverCopy());
        }
        Theme.updateSeekBar(mSeekBar);
        mRandomTrack.setBackground(ThemeHelper.getCheckDrawable(
                R.drawable.ic_random, R.drawable.ic_random, VectorDrawableCompat.class));
        mLoopingTrack.setBackground(ThemeHelper.getCheckDrawable(
                R.drawable.ic_repeat_all, R.drawable.ic_repeat_one, VectorDrawableCompat.class));
        mPlayPauseCallback.onInvoke(null, ReactiveArchitect.getStateKeeper(MusicModel.IS_TRACK_PLAY).getState());
    }

    public void updateThemeContext() {
        if (mPlayerTrackArtist.getText().toString().equals("")) {
            showEmptyContent();
        }

        mPlayerTrackAlbum.setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);
        mPlayerTrackArtist.setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);
        mPlayerTrackName.setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);

        Theme.updateSeekBar(mSeekBar);

        mTrackProgress.setTextColor(Theme.CONTEXT_SECONDARY_TEXT);
        mTrackDuration.setTextColor(Theme.CONTEXT_SECONDARY_TEXT);

        mRandomTrack.setBackground(ThemeHelper.getCheckDrawable(
                R.drawable.ic_random, R.drawable.ic_random, VectorDrawableCompat.class));
        mPreviousTrack.setBackground(ThemeHelper.getDefaultButtonDrawable(R.drawable.ic_previous));
        mNextTrack.setBackground(ThemeHelper.getDefaultButtonDrawable(R.drawable.ic_next));
        mLoopingTrack.setBackground(ThemeHelper.getCheckDrawable(
                R.drawable.ic_repeat_all, R.drawable.ic_repeat_one, VectorDrawableCompat.class));
    }

    private void showEmptyContent() {
        int color = Theme.isDark() ? Theme.CONTEXT_LIGHT : Theme.CONTEXT_PRIMARY_LIGHT;
        mPlayerTrackAlbum.setBackgroundColor(color);
        mPlayerTrackArtist.setBackgroundColor(color);
        mPlayerAlbumImage.setBackgroundColor(color);
        mPlayerTrackName.setBackgroundColor(color);
        mTrackProgress.setBackgroundColor(color);
        mTrackDuration.setBackgroundColor(color);
    }

    public void repeatTrack(boolean isRepeat) {
        mLoopingTrack.setChecked(isRepeat);
    }

    @OnClick(R.id.playerLoopingTrack)
    protected void switchTrackLooping() {
        PlayerModel.setTrackLooping(!PlayerModel.isTrackLooping());
    }

    @OnClick(R.id.playerRandomTrack)
    protected void switchPlayRandomTrack() {
        PlayerModel.setPlayRandomTrack(!PlayerModel.isPlayRandomTrack());
    }

    public static String getTime(int millis) {
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
    private static String removeFirstUnit(@NonNull String result) {
        return result.split(":", 2)[1];
    }

    private static boolean isTimeUnitZero(@NonNull String timeUnit) {
        boolean isZero = true;
        for (int i = 0; i < timeUnit.length() && isZero; i++) {
            isZero = timeUnit.charAt(i) == '0';
        }
        return isZero;
    }
}
