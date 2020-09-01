package ru.reactiveturtle.reactivemusic.player.mvp;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.List;

import ru.reactiveturtle.reactivemusic.player.BaseMusicContract;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ColorSet;

public interface PlayerContract {
    interface Repository extends BaseMusicContract.Repository {
        void setThemeContextDark(boolean isDark);

        boolean isThemeContextDark();

        void setThemeColorSet(@NonNull ColorSet colorSet);

        @NonNull
        ColorSet getThemeColorSet();

        boolean addPlaylistName(@NonNull String playlist);

        void removePlaylistName(@NonNull String playlist);

        void updatePlaylist(@NonNull String playlist, @NonNull List<String> playlistTracks);

        boolean renamePlaylist(@NonNull String oldName, @NonNull String newName);

        boolean addTrack(@NonNull String playlist, @NonNull String trackPath);

        void removeTrack(@NonNull String playlist, @NonNull String trackPath);

        @NonNull
        List<String> getPlaylists();

        @NonNull
        List<String> getCodedPlaylists();

        @NonNull
        List<String> getPlaylist(String playlist);

        @NonNull
        List<String> getCodedPlaylist(@NonNull String playlist);

        void release();
    }

    interface Model extends BaseMusicContract.Model {
        void setSelectedPage(int position);

        int getSelectedPage();

        void setPlaylistOpen(String playlist);

        String getPlaylistOpen();

        void setScrollToCurrentTrackLater(boolean isDoLater);

        boolean isScrollToCurrentTrackLater();
    }

    interface View {
        void pressBack();

        void sendPlayPreviousTrack();

        void sendPlayNextTrack();

        void sendRepeatTrack(boolean isRepeat);

        void unlockTrackProgress();

        void showPage(int position);

        void showCreateNameDialog();

        void showRenameDialog(String playlistName);

        void hideNameDialog();

        void showMusicSelector(List<String> playlist);

        void showSelectedTrack(@NonNull String path);

        void hideSelectedTrack(@NonNull String path);

        void showPlaylistActions(String playlistName);

        void showToolbarArrow();

        void hideToolbarArrow();

        void showTitle(@StringRes int stringId);

        void showTitle(@StringRes int stringId, String string);

        void showTitle(String name);

        void showToast(@StringRes int string);

        void updateTheme();

        void updateThemeContext();
    }

    interface Presenter extends ReceiverPresenter {
        void onView(View view);

        void onStop();

        void onScrollToCurrentTrackLater();

        void onPageSelected(int position);

        void onBackPressed();

        void onCreatePlaylist(String result);

        void onRenamePlaylist(String oldName, String newName);

        void onPlaylistActionSelected(String playlistName, int position);
    }

    interface ReceiverPresenter extends BaseMusicContract.Presenter {
        void onShowCurrentTrackInfo(MusicInfo musicInfo);

        void onUpdateTrackProgress(boolean isUnlockTrackProgress);

        void onPlay();

        void onPause();
    }
}
