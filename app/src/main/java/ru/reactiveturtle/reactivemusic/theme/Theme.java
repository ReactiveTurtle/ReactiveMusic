package ru.reactiveturtle.reactivemusic.theme;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import java.util.Objects;

import ru.reactiveturtle.reactivemusic.player.BackgroundGradientDrawable;
import ru.reactiveturtle.reactivemusic.player.DefaultAlbumCover;
import ru.reactiveturtle.reactivemusic.toolkit.ReactiveList;
import ru.reactiveturtle.reactivemusic.toolkit.Releasable;

public class Theme implements Releasable {
    private Context context;
    private ThemeRepository themeRepository;
    private ThemeContext themeContext;
    private IconManager iconManager;
    private ReactiveList<ThemeDependent> themeDependentList = new ReactiveList<>();

    public Theme(Context context) {
        this.context = context;
        themeRepository = new ThemeRepository(context);
        themeContext = new ThemeContext(this);
        iconManager = new IconManager(context.getResources(), context.getTheme(), themeContext);
        setColorSet(themeRepository.getThemeColorSet());
    }

    public void setColorSet(@NonNull ColorSet colorSet) {
        Objects.requireNonNull(colorSet);
        defaultAlbumCover = DefaultAlbumCover.get(context.getResources(), colorSet.getPrimary());
        defaultBackground = new BackgroundGradientDrawable(colorSet);
        iconManager.setColorSet(colorSet);
        themeDependentList.forEachP(ThemeDependent::onThemeUpdate);
    }

    public void addThemeDependent(ThemeDependent themeDependent) {
        Objects.requireNonNull(themeDependent);
        if (themeDependentList.contains(themeDependent)) {
            throw new IllegalStateException(String.format("Theme dependent object \"%s\" already tracked", themeDependent.getClass().getName()));
        }
        themeDependentList.add(themeDependent);
    }

    public void removeThemeDependent(ThemeDependent themeDependent) {
        Objects.requireNonNull(themeDependent);
        if (!themeDependentList.contains(themeDependent)) {
            throw new IllegalStateException(String.format("Theme dependent object \"%s\" now not tracked", themeDependent.getClass().getName()));
        }
        themeDependentList.remove(themeDependent);
    }

    private BitmapDrawable defaultAlbumCover = null;

    public BitmapDrawable getDefaultAlbumCover() {
        return defaultAlbumCover;
    }

    public BitmapDrawable getDefaultAlbumCoverCopy() {
        return null;
    }

    private BackgroundGradientDrawable defaultBackground = null;

    public BackgroundGradientDrawable getDefaultBackground() {
        return defaultBackground;
    }

    public void updateSeekBar(SeekBar seekBar) {
        int cursorColor = themeRepository.getThemeColorSet().getPrimary();
        int seekBarColor = getColorSet().getPrimaryDark();
        if (isDark()) {
            cursorColor = getColorSet().getPrimaryLight();
            seekBarColor = getColorSet().getPrimary();
        }
        if (isGrey(cursorColor) || !isDark() && isVeryBright(cursorColor)) {
            cursorColor = themeContext.getNegativePrimary();
            seekBarColor = themeContext.getNegativeSecondary();
        }
        changeColor(seekBar.getThumb(), cursorColor);
        changeColor(seekBar.getProgressDrawable(), seekBarColor);
    }


    @Override
    public void release() {

    }

    public boolean isDark() {
        return themeRepository.getThemeContext() == ColorContext.DARK;
    }

    public ColorSet getColorSet() {
        return themeRepository.getThemeColorSet();
    }

    public IconManager getIconManager() {
        return iconManager;
    }

    public ThemeContext getThemeContext() {
        return themeContext;
    }

    public static boolean isGrey(int color) {
        return Color.red(color) == Color.green(color) && Color.red(color) == Color.blue(color);
    }

    public static boolean isVeryBright(int color) {
        return Color.green(color) >= 232;
    }

    @NonNull
    public static Drawable changeColor(@NonNull Drawable drawable, int newColor) {
        drawable.setColorFilter(new LightingColorFilter(Color.BLACK, newColor));
        return drawable;
    }

    public static int setAlpha(String hexAlpha, int color) {
        return Color.parseColor("#" + hexAlpha
                + Integer.toHexString(color).substring(2));
    }

    public enum ColorContext {
        DARK, LIGHT
    }
}
