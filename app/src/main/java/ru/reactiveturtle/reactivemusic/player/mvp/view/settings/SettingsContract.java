package ru.reactiveturtle.reactivemusic.player.mvp.view.settings;

import ru.reactiveturtle.reactivemusic.player.BaseMusicContract;

public interface SettingsContract {
    interface Fragment extends BaseMusicContract.Fragment {
        void showSettings();
    }

    interface Presenter extends BaseMusicContract.BasePresenter {
        void onSettingsFragmentAvailable(Fragment view);

        void onUpdateTheme();

        void onUpdateContextTheme();
    }
}
