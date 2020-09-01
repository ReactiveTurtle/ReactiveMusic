package ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class ColorSet {
    @ColorInt
    private int primaryDark;
    @ColorInt
    private int primary;
    @ColorInt
    private int primaryLight;

    public ColorSet(@ColorInt int primaryDark,
                    @ColorInt int primary,
                    @ColorInt int primaryLight) {
        this.primaryDark = primaryDark;
        this.primary = primary;
        this.primaryLight = primaryLight;
    }

    public ColorSet(@NonNull String string) {
        String[] colors = string.split("\\|", 3);
        primaryDark = Integer.parseInt(colors[0]);
        primary = Integer.parseInt(colors[1]);
        primaryLight = Integer.parseInt(colors[2]);
    }

    @ColorInt
    public int getPrimaryDark() {
        return primaryDark;
    }

    @ColorInt
    public int getPrimary() {
        return primary;
    }

    @ColorInt
    public int getPrimaryLight() {
        return primaryLight;
    }

    @NonNull
    @Override
    public String toString() {
        return primaryDark + "|" + primary + "|" + primaryLight;
    }
}
