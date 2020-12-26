package ru.reactiveturtle.tools.reactiveuvm.fragment;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import ru.reactiveturtle.tools.reactiveuvm.Bridge;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;
import ru.reactiveturtle.tools.reactiveuvm.StateKeeper;

public abstract class ArchitectFragment extends Fragment {
    private List<StateKeeper.Binder> binders = new ArrayList<>();
    private List<Bridge> bridges = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onInitializeBinders(binders);
        onInitializeBridges(bridges);
    }

    protected abstract void onInitializeBinders(List<StateKeeper.Binder> container);

    protected abstract void onInitializeBridges(List<Bridge> container);

    @Override
    public void onStop() {
        for (StateKeeper.Binder binder : binders) {
            binder.unsubscribe();
        }
        binders.clear();
        for (Bridge bridge : bridges) {
            ReactiveArchitect.removeBridge(bridge.getName());
        }
        bridges.clear();
        super.onStop();
    }
}
