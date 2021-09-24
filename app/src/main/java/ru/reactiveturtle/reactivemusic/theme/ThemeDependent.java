package ru.reactiveturtle.reactivemusic.theme;

public interface ThemeDependent {
    void onThemeUpdate(Theme theme);

    void onThemeContextUpdate(Theme theme);
}
