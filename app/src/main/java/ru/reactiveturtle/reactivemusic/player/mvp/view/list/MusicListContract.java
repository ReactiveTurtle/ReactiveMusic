package ru.reactiveturtle.reactivemusic.player.mvp.view.list;

import ru.reactiveturtle.reactivemusic.player.BaseMusicContract;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.view.selector.SelectMusicListAdapter;

public interface MusicListContract {
    interface Fragment extends BaseMusicContract.MusicControlFragment {
        void scrollToPosition(int index);

        void bindLists(SelectMusicListAdapter adapter);
    }

    interface Presenter extends BaseMusicContract.FragmentPresenter {
        void onMusicListAvailable(Fragment fragment);

        void onMusicChanged(MusicInfo musicInfo, String playlistName);

        void onFindTrack(String track);

        void onFindTrackInPlaylist(String playlistName, String track);
    }
}
