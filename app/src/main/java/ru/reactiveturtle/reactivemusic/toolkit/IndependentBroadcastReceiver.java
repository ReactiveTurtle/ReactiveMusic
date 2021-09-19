package ru.reactiveturtle.reactivemusic.toolkit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import java.util.Objects;

public abstract class IndependentBroadcastReceiver extends BroadcastReceiver {
    private boolean isRegistered = false;

    public boolean register(Context context, String filterName) {
        if (!isRegistered) {
            Objects.requireNonNull(context);
            Objects.requireNonNull(filterName);
            context.registerReceiver(this, new IntentFilter(filterName));
            isRegistered = true;
            return true;
        }
        return false;
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            Objects.requireNonNull(context);
            isRegistered = false;
            context.unregisterReceiver(this);
            return true;
        }
        return false;
    }

    @Override
    public abstract void onReceive(Context context, Intent intent);
}