package ru.reactiveturtle.tools.reactiveuvm.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.reactiveturtle.tools.reactiveuvm.Bridge;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;
import ru.reactiveturtle.tools.reactiveuvm.StateKeeper;

public abstract class ArchitectService extends Service {
    private List<StateKeeper.Binder> binders = new ArrayList<>();
    private List<Bridge> bridges = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        onInitializeBinders(binders);
        onInitializeBridges(bridges);
    }

    protected abstract void onInitializeBinders(List<StateKeeper.Binder> container);

    protected abstract void onInitializeBridges(List<Bridge> container);

    @Override
    public void onDestroy() {
        for (StateKeeper.Binder binder : binders) {
            binder.unsubscribe();
        }
        binders.clear();
        for (Bridge bridge : bridges) {
            ReactiveArchitect.removeBridge(bridge.getName());
        }
        bridges.clear();
        super.onDestroy();
    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
