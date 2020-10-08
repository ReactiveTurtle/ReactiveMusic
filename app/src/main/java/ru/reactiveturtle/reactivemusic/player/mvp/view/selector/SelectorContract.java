package ru.reactiveturtle.reactivemusic.player.mvp.view.selector;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import java.util.List;

import ru.reactiveturtle.reactivemusic.player.BaseMusicContract;
import ru.reactiveturtle.reactivemusic.player.mvp.PlayerContract;

public interface SelectorContract {
    interface View extends BaseMusicContract.View {
        void setPresenter(@NonNull PlayerContract.Presenter presenter);

        void setSelectedItems(List<String> playlist);

        void showSelectedItem(@NonNull String path);

        void hideSelectedItem(@NonNull String path);

        void onResume();

        void onPause();

        SelectMusicListFragment getSelectMusicList();

        void updateBackground(Drawable drawable);
    }

    interface Presenter {
        void onPlaylistTrackChanged(String path, boolean isChecked);

        void onPlaylistUpdate(List<String> playlist);
    }
}
