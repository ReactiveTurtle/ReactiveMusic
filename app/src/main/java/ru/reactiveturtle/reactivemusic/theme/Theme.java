package ru.reactiveturtle.reactivemusic.theme;

import android.content.Context;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ru.reactiveturtle.reactivemusic.player.BackgroundGradientDrawable;
import ru.reactiveturtle.reactivemusic.player.DefaultAlbumCover;
import ru.reactiveturtle.reactivemusic.toolkit.ReactiveList;
import ru.reactiveturtle.reactivemusic.toolkit.Releasable;

public class Theme implements Releasable {
    private Context context;
    private ThemeRepository themeRepository;
    private ThemeContextColorContainer themeContextColorContainer;
    private IconManager iconManager;
    private ReactiveList<ThemeDependent> themeDependentList = new ReactiveList<>();
    private ColorSet currentColorSet;

    public Theme(Context context) {
        this.context = context;
        initContextMap();
        themeRepository = new ThemeRepository(context);
        themeContextColorContainer = new ThemeContextColorContainer(this, themeRepository.getThemeContext());
        iconManager = new IconManager(context.getResources(), context.getTheme(), themeContextColorContainer);
        setThemeContext(themeRepository.getThemeContext());
    }

    public void setThemeContext(ThemeContext themeContext) {
        this.themeRepository.setThemeContext(themeContext);
        this.setColorSet(themeRepository.getThemeColorType(), contextMap.get(themeContext).get(themeRepository.getThemeColorType()));
    }

    public void setColorSet(@NonNull ColorType colorType, @NonNull ColorSet colorSet) {
        Objects.requireNonNull(colorSet);
        currentColorSet = colorSet;
        defaultAlbumCover = DefaultAlbumCover.get(context.getResources(), MaterialColorPalette.M_A700.get(colorType).getPrimary());
        defaultBackground = new BackgroundGradientDrawable(colorSet);
        iconManager.setColorSet(colorSet);
        themeDependentList.forEachP((themeDependent, theme) -> themeDependent.onThemeUpdate(this));
    }

    public void addThemeDependent(ThemeDependent themeDependent) {
        Objects.requireNonNull(themeDependent);
        if (themeDependentList.contains(themeDependent)) {
            throw new IllegalStateException(String.format("Theme dependent object \"%s\" already tracked", themeDependent.getClass().getName()));
        }
        themeDependentList.add(themeDependent);
    }

    public void addThemeDependentAndCall(ThemeDependent themeDependent) {
        addThemeDependent(themeDependent);
        themeDependent.onThemeContextUpdate(this);
        themeDependent.onThemeUpdate(this);
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
        int cursorColor = getColorSet().getPrimary();
        int seekBarColor = getColorSet().getPrimaryDark();
        if (isDark()) {
            cursorColor = getColorSet().getPrimaryLight();
            seekBarColor = getColorSet().getPrimary();
        }
        if (isGrey(cursorColor) || !isDark() && isVeryBright(cursorColor)) {
            cursorColor = themeContextColorContainer.getNegativePrimary();
            seekBarColor = themeContextColorContainer.getNegativeSecondary();
        }
        changeColor(seekBar.getThumb(), cursorColor);
        changeColor(seekBar.getProgressDrawable(), seekBarColor);
    }


    @Override
    public void release() {

    }

    public boolean isDark() {
        return themeRepository.getThemeContext() == ThemeContext.DARK;
    }

    public ColorSet getColorSet() {
        return currentColorSet;
    }

    public ColorType getColorType() {
        return themeRepository.getThemeColorType();
    }

    public IconManager getIconManager() {
        return iconManager;
    }

    public ThemeContextColorContainer getThemeContext() {
        return themeContextColorContainer;
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

    public enum ThemeContext {
        DARK, LIGHT
    }

    private final Map<ThemeContext, Map<ColorType, ColorSet>> contextMap = new HashMap<>();
    private void initContextMap() {
        contextMap.put(ThemeContext.DARK, MaterialColorPalette.M_700);
        contextMap.put(ThemeContext.LIGHT, MaterialColorPalette.M_A700);
    }
}
