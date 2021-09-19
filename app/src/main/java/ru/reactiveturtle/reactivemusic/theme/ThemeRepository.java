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

    public void setThemeContext(Theme.ColorContext themeContext) {
        this.themeContext = themeContext;
        getEditor().putString(THEME_CONTEXT, themeContext.toString()).apply();
    }

    private Theme.ColorContext themeContext;

    public Theme.ColorContext getThemeContext() {
        if (themeContext == null) {
            themeContext = Theme.ColorContext.valueOf(getPreferences().getString(THEME_CONTEXT, Theme.ColorContext.DARK.toString()));
        }
        return themeContext;
    }

    public void setThemeColorSet(@NonNull ColorSet colorSet) {
        themeColorSet = colorSet;
        getEditor().putString(THEME_COLOR, colorSet.toString()).apply();
    }

    private ColorSet themeColorSet;

    @NonNull
    public ColorSet getThemeColorSet() {
        if (themeColorSet == null) {
            String colorString = getPreferences().getString(THEME_COLOR, null);
            if (colorString == null) {
                colorString = Objects.requireNonNull(MaterialColorPalette.M_A700.get(ColorType.GREEN)).toString();
            }
            String[] colorsAsString = colorString.split("\\|");
            themeColorSet = new ColorSet(
                    Integer.parseInt(colorsAsString[0]),
                    Integer.parseInt(colorsAsString[1]),
                    Integer.parseInt(colorsAsString[2]));
        }
        return themeColorSet;
    }
}
