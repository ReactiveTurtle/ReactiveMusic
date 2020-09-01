package ru.reactiveturtle.reactivemusic.player.mvp.view.playlist;

import java.util.List;

import ru.reactiveturtle.reactivemusic.player.BaseMusicContract;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;

public interface PlaylistContract {
    interface Fragment extends BaseMusicContract.MusicControlFragment {
        void showPlaylists(List<String> playlists);

        void addPlaylist(String result);

        void renamePlaylist(String oldName, String newName);

        void removePlaylist(String playlistName);

        void showPlaylist(String name, List<String> playlist);

        void closePlaylist();

        void scrollToPlaylist(int index);

        void addTrack(String path);

        void removeTrack(String path);

        void scrollToTrack(int index);
    }

    interface Presenter extends BaseMusicContract.BasePresenter {
        void onPlaylistAvailable(Fragment fragment);

        void onPlaylistAdd();

        void onOpenPlaylist(String name);

        void onPlaylistMusicSelected(MusicInfo musicInfo);

        void onShowPlaylistActions(String playlistName);

        void onFindTrack(String track);

        void onFindTrackInPlaylist(String playlistName, String track);
    }
}
