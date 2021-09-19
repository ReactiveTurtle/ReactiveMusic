package ru.reactiveturtle.reactivemusic;

import android.app.Application;
import android.util.Log;
import android.util.Pair;

import ru.reactiveturtle.reactivemusic.toolkit.ReactiveList;

public class App extends Application {
    private static final int LOG_HISTORY_LENGTH = 20;
    private static final ReactiveList<Pair<String, String>> logHistory= new ReactiveList<>();
    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void log(String tag, String message) {
        logHistory.add(new Pair<>(tag, message));
        if (logHistory.size() > LOG_HISTORY_LENGTH) {
            logHistory.remove(0);
        }
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        logHistory.forEachP(x -> Log.d(x.first, x.second));
        logHistory.clear();
    }
}
