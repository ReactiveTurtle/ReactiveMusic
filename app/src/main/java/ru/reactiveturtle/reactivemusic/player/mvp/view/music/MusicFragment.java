package ru.reactiveturtle.reactivemusic.player.mvp.view.music;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
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
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.GlobalModel;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ColorSet;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ThemeHelper;

public class MusicFragment extends Fragment implements MusicContract.Fragment {
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
        mPresenter = GlobalModel.PLAYER_PRESENTER;
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

        showEmptyContent();
        if (mPresenter != null) {
            mPresenter.onMusicFragmentAvailable(this);
        }
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
        updateThemeContext();
        updateTheme();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private MusicContract.Presenter mPresenter;

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
        mPlayerTrackName.setText(musicInfo.getTitle());

        mSeekBar.setMax(musicInfo.getDuration());
        mTrackDuration.setText(getTime(musicInfo.getDuration()));
    }

    @Override
    public void clearCurrentMusic() {

    }

    @Override
    public void startMusic() {
        mPlayPause.setBackground(ThemeHelper.getDefaultRoundButtonDrawable(R.drawable.ic_pause));
    }

    @Override
    public void pauseMusic() {
        mPlayPause.setBackground(ThemeHelper.getDefaultRoundButtonDrawable(R.drawable.ic_play));
    }

    @Override
    public void updateTheme() {
        Theme.updateSeekBar(mSeekBar);
        mRandomTrack.setBackground(ThemeHelper.getCheckDrawable(
                R.drawable.ic_random, R.drawable.ic_random, VectorDrawableCompat.class));
        mRepeatTrack.setBackground(ThemeHelper.getCheckDrawable(
                R.drawable.ic_repeat_all, R.drawable.ic_repeat_one, VectorDrawableCompat.class));
        if (mPlayerAlbumImage.getBackground() instanceof BitmapDrawable &&
                Theme.getDefaultAlbumCover().getBitmap()
                        .equals(((BitmapDrawable) mPlayerAlbumImage.getBackground()).getBitmap())) {
            mPlayerAlbumImage.setBackground(Theme.getDefaultAlbumCoverCopy());
        }
        if (GlobalModel.isTrackPlay()) {
            startMusic();
        } else {
            pauseMusic();
        }
    }

    @Override
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
        mRepeatTrack.setBackground(ThemeHelper.getCheckDrawable(
                R.drawable.ic_repeat_all, R.drawable.ic_repeat_one, VectorDrawableCompat.class));
    }

    private void showEmptyContent() {
        int color = Theme.IS_DARK ? Theme.CONTEXT_LIGHT : Theme.CONTEXT_PRIMARY_LIGHT;
        mPlayerTrackAlbum.setBackgroundColor(color);
        mPlayerTrackArtist.setBackgroundColor(color);
        mPlayerAlbumImage.setBackgroundColor(color);
        mPlayerTrackName.setBackgroundColor(color);
        mTrackProgress.setBackgroundColor(color);
        mTrackDuration.setBackgroundColor(color);
    }

    @Override
    public void updateTrackProgress(int progress) {
        setProgress(progress);
    }

    @Override
    public void updateTrackCover(BitmapDrawable cover) {
        if (Theme.getDefaultAlbumCover().getBitmap()
                .equals(cover.getBitmap())) {
            mPlayerAlbumImage.setBackground(Theme.getDefaultAlbumCoverCopy());
        } else {
            mPlayerAlbumImage.setBackground(cover);
        }
    }

    @Override
    public void repeatTrack(boolean isRepeat) {
        mRepeatTrack.setChecked(isRepeat);
    }

    @Override
    public void playRandomTrack(boolean isRandom) {
        mRandomTrack.setChecked(isRandom);
    }

    private Timer animTimer;

    @OnClick(R.id.playerAlbumImage)
    protected void albumImageClick() {
        List<ColorSet> colorSets = Theme.getColors(13);
        if (colorSets != null) {
            final int[] i = {0, 0};
            if (animTimer != null) {
                animTimer.cancel();
            }
            animTimer = new Timer();
            animTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (i[0] < colorSets.size()) {
                        Bitmap original = Bitmap.createScaledBitmap(GlobalModel.getCurrentTrack().getAlbumImage().getBitmap(),
                                512, 512, true);
                        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                        Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
                        paint.setColor(colorSets.get(colorSets.size() - i[0] - 1).getPrimary());

                        int[] colors = new int[colorSets.size()];
                        for (int j = 0; j < i[0]; j++) {
                            colors[j] = Color.TRANSPARENT;
                        }

                        for (int j = i[0]; j < colors.length; j++) {
                            colors[j] = colorSets.get(j - i[0]).getPrimary();
                        }

                        float[] positions = new float[]{
                                0, 1f / 15, 2f / 15,
                                3f / 15, 4f / 15, 5f / 15,
                                6f / 15, 7f / 15, 8f / 15,
                                9f / 15, 10f / 15, 11f / 15,
                                12f / 15, 13f / 15, 14f / 15,
                                1f
                        };
                        Canvas canvas = new Canvas(bitmap);
                        canvas.drawBitmap(original, (bitmap.getWidth() - original.getWidth()) / 2f,
                                (bitmap.getHeight() - original.getHeight()) / 2f, paint);
                        paint.setShader(new RadialGradient(256, 256, 256,
                                colors,
                                positions, Shader.TileMode.CLAMP));
                        canvas.drawRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()), paint);
                        Helper.goToMainLooper(() ->
                                mPlayerAlbumImage.setBackground(new BitmapDrawable(getResources(), bitmap)));
                        i[0]++;
                    } else {
                        Theme.updateDefaultAlbumCover();
                        BitmapDrawable cover = GlobalModel.getCurrentTrack().getAlbumImage();
                        mPlayerAlbumImage.setBackground(new BitmapDrawable(getResources(),
                                cover.getBitmap().copy(Bitmap.Config.ARGB_8888, false)));
                        cancel();
                    }
                }
            }, 0, 17);
        }
    }

    @OnClick(R.id.playerRandomTrack)
    protected void switchPlayRandomTrack() {
        GlobalModel.setPlayRandomTrack(!GlobalModel.isPlayRandomTrack());
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
