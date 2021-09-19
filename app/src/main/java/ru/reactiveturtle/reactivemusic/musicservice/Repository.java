package ru.reactiveturtle.reactivemusic.musicservice;

import android.content.Context;
import android.content.SharedPreferences;

public abstract class Repository {
    private SharedPreferences preferences;

    public Repository(String name, Context context) {
        preferences = context.getSharedPreferences(name, 0);
    }

    protected final SharedPreferences getPreferences() {
        return preferences;
    }

    protected final SharedPreferences.Editor getEditor() {
        return preferences.edit();
    }
}
