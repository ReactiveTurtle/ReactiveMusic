package ru.reactiveturtle.reactivemusic.player.mvp.view.settings;

import android.graphics.drawable.Drawable;

public class SettingsItem {
    private Drawable icon;
    private String title;

    public SettingsItem(String title) {
        this.title = title;
    }

    public SettingsItem(Drawable icon, String title) {
        this.icon = icon;
        this.title = title;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
