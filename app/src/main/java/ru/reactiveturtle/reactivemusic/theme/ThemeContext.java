package ru.reactiveturtle.reactivemusic.theme;

public class ThemeContext {
    private int primary;
    private int primaryLight;
    private int light;
    private int negativePrimary;
    private int negativeSecondary;

    public ThemeContext(Theme theme) {
        if (theme.isDark()) {
            primary = 0xff000000;
            primaryLight = 0xff121212;
            light = 0xff323232;
            negativePrimary = 0xffffffff;
            negativeSecondary = 0xffbebebe;
        } else {
            primary = 0xffffffff;
            primaryLight = 0xfff5f5f5;
            light = 0xffdddddd;
            negativePrimary = 0xff000000;
            negativeSecondary = 0xff323232;
        }
    }

    public int getPrimary() {
        return primary;
    }

    public int getPrimaryLight() {
        return primaryLight;
    }

    public int getLight() {
        return light;
    }

    public int getNegativePrimary() {
        return negativePrimary;
    }

    public int getNegativeSecondary() {
        return negativeSecondary;
    }
}
