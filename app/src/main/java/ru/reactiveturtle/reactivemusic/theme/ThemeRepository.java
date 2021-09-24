package ru.reactiveturtle.reactivemusic.theme;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.Objects;

import ru.reactiveturtle.reactivemusic.musicservice.Repository;

public class ThemeRepository extends Repository {
    private static final String THEME_CONTEXT = "THEME_CONTEXT";
    private static final String THEME_COLOR = "THEME_COLOR";

    public ThemeRepository(Context context) {
        super("THEME_REPOSITORY", context);
    }

    public void setThemeContext(Theme.ThemeContext themeContext) {
        this.themeContext = themeContext;
        getEditor().putString(THEME_CONTEXT, themeContext.toString()).apply();
    }

    private Theme.ThemeContext themeContext;

    public Theme.ThemeContext getThemeContext() {
        if (themeContext == null) {
            themeContext = Theme.ThemeContext.valueOf(getPreferences().getString(THEME_CONTEXT, Theme.ThemeContext.DARK.toString()));
        }
        return themeContext;
    }

    public void setThemeColorType(@NonNull ColorType colorType) {
        themeColorType = colorType;
        getEditor().putString(THEME_COLOR, colorType.toString()).apply();
    }

    private ColorType themeColorType;

    @NonNull
    public ColorType getThemeColorType() {
        if (themeColorType == null) {
            String colorType = getPreferences().getString(THEME_COLOR, ColorType.YELLOW.toString());
            themeColorType = ColorType.valueOf(colorType);
        }
        return themeColorType;
    }
}
