package ru.reactiveturtle.reactivemusic.theme;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import java.util.Objects;

public final class ColorSet {
    private final int primaryDark;
    private final int primary;
    private final int primaryLight;

    public ColorSet(@ColorInt int primaryDark,
                    @ColorInt int primary,
                    @ColorInt int primaryLight) {
        this.primaryDark = primaryDark;
        this.primary = primary;
        this.primaryLight = primaryLight;
    }

    @ColorInt
    public final int getPrimaryDark() {
        return primaryDark;
    }

    @ColorInt
    public final int getPrimary() {
        return primary;
    }

    @ColorInt
    public final int getPrimaryLight() {
        return primaryLight;
    }

    @NonNull
    @Override
    public String toString() {
        return primaryDark + "|" + primary + "|" + primaryLight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColorSet colorSet = (ColorSet) o;
        return primaryDark == colorSet.primaryDark &&
                primary == colorSet.primary &&
                primaryLight == colorSet.primaryLight;
    }

    @Override
    public int hashCode() {
        return Objects.hash(primaryDark, primary, primaryLight);
    }
}
