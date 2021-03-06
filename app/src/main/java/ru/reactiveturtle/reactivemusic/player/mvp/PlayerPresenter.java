package ru.reactiveturtle.reactivemusic.player.mvp;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.GlobalModel;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerModel;
import ru.reactiveturtle.reactivemusic.player.mvp.view.list.MusicListContract;
import ru.reactiveturtle.reactivemusic.player.mvp.view.music.MusicContract;
import ru.reactiveturtle.reactivemusic.player.mvp.view.playlist.PlaylistContract;
import ru.reactiveturtle.reactivemusic.player.mvp.view.selector.SelectorContract;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.SettingsContract;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;

public class PlayerPresenter implements PlayerContract.Presenter,
        MusicContract.Presenter, MusicListContract.Presenter,
        PlaylistContract.Presenter, SelectorContract.Presenter,
        SettingsContract.Presenter {
    public static final int GLOBAL_MODEL_LISTENER = 1;
    private PlayerContract.View mView;
    private PlayerContract.Model mModel;
    private PlayerContract.Repository mRepository;

    public PlayerPresenter(@NonNull PlayerContract.Repository repository) {
        mModel = new PlayerModel();
        mRepository = repository;
    }

    @Override
    public void onRecreate() {
        GlobalModel.registerListener(GLOBAL_MODEL_LISTENER, new GlobalModel.OnModelUpdateListener() {
            @Override
            public void onTrackTextUpdated(MusicInfo currentTrack) {
                onShowCurrentTrackInfo(currentTrack);
                if (GlobalModel.isFirstTrackLoad()) {
                    GlobalModel.setFirstTrackLoad(false);
                    new Timer().schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Helper.goToMainLooper(() -> {
                                mView.showDrawer();
                            });
                        }
                    }, 1000);
                }
            }

            @Override
            public void onTrackCoverUpdated(MusicInfo currentTrack) {
                updateTrackCover();
            }

            @Override
            public void onTrackProgressUpdate(int progress, boolean isUnlockTrackProgress) {
                onUpdateTrackProgress(isUnlockTrackProgress);
            }

            @Override
            public void onTrackPlayUpdate(boolean isPlay) {
                if (isPlay) {
                    onPlay();
                } else {
                    onPause();
                }
            }

            @Override
            public void onRepeatTrack(boolean isRepeat) {
                if (mMusicFragment != null) {
                    mMusicFragment.repeatTrack(isRepeat);
                }
            }

            @Override
            public void onPlayRandomTrack(boolean isRandom) {
                if (mMusicFragment != null) {
                    mMusicFragment.playRandomTrack(isRandom);
                }
            }
        });
    }

    @Override
    public void onPreviousTrack() {
        mView.sendPlayPreviousTrack();
    }

    @Override
    public void onPlayPause() {
        GlobalModel.setTrackPlay(!GlobalModel.isTrackPlay());
    }

    private MusicContract.Fragment mMusicFragment;

    @Override
    public void onMusicFragmentAvailable(MusicContract.Fragment fragment) {
        mMusicFragment = fragment;
        updateCurrentTrackInfo();
        updateTrackCover();
    }


    private void updateTrackCover() {
        if (mMusicFragment != null) {
            mMusicFragment.updateTrackCover(GlobalModel.getCurrentTrack().getAlbumImage());
        }
        if (mView != null) {
            mView.updateWindowBackground();
        }
    }

    private MusicListContract.Fragment mMusicListFragment;

    @Override
    public void onMusicListAvailable(MusicListContract.Fragment fragment) {
        mMusicListFragment = fragment;
        mView.bindMusicLists();
        onShowCurrentTrackInfo(GlobalModel.getCurrentTrack());
    }

    @Override
    public void onNextTrack() {
        mView.sendPlayNextTrack();
    }

    @Override
    public void onMusicChanged(MusicInfo musicInfo, String playlist) {
        mRepository.setCurrentPlaylist(playlist);
        GlobalModel.updateCurrentTrack(musicInfo.getPath(), 1);
        GlobalModel.setTrackPlay(true, 1);
    }

    @Override
    public void onFindTrack(String track) {
        onFindTrackInPlaylist(mRepository.getCurrentPlaylist(), track);
    }

    @Override
    public void onFindTrackInPlaylist(String playlistName, String track) {
        List<String> playlist =
                mRepository.getPlaylist(playlistName);
        int index = playlist.indexOf(track);
        if (index > -1) {
            if (playlistName != null) {
                if (mModel.getSelectedPage() != 1) {
                    mView.showPage(1);
                }
                if (mModel.getPlaylistOpen() == null) {
                    onOpenPlaylist(playlistName);
                }
                mPlaylistFragment.scrollToTrack(index);
            } else {
                if (mModel.getSelectedPage() != 2) {
                    mView.showPage(2);
                }
                mMusicListFragment.scrollToPosition(index);
            }
        } else {
            mView.showToast(R.string.track_not_found);
        }
    }

    @Override
    public void onShowPlaylistActions(String playlistName) {
        mView.showPlaylistActions(playlistName);
    }

    @Override
    public void onView(@NonNull PlayerContract.View view) {
        mView = view;
        if (!GlobalModel.isFirstTrackLoad()) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Helper.goToMainLooper(() -> {
                        mView.showDrawer();
                    });
                }
            }, 1000);
        }
    }

    @Override
    public void clearAllLinks() {
        mView = null;
        mMusicFragment = null;
        mPlaylistFragment = null;
        mMusicListFragment = null;
        mSettingsFragment = null;
    }

    @Override
    public void onStop() {
        GlobalModel.unregisterListener(1);
        clearAllLinks();
    }

    @Override
    public void onScrollToCurrentTrackLater() {
        mModel.setScrollToCurrentTrackLater(true);
    }

    @Override
    public void onPageSelected(int position) {
        mModel.setSelectedPage(position);
        if (position == 1) {
            if (mModel.getPlaylistOpen() != null) {
                mView.showToolbarArrow();
            } else {
                mView.hideToolbarArrow();
            }
        } else {
            mView.hideToolbarArrow();
        }
    }

    @Override
    public void onBackPressed() {
        if (mModel.getSelectedPage() == 1
                && mModel.getPlaylistOpen() != null) {
            mModel.setPlaylistOpen(null);
            mPlaylistFragment.closePlaylist();
            mView.showTitle(R.string.playlists);
            mView.hideToolbarArrow();
        } else {
            mView.pressBack();
        }
    }

    @Override
    public void onCreatePlaylist(String result) {
        if (mRepository.addPlaylistName(result)) {
            mPlaylistFragment.addPlaylist(result);
            mView.hideNameDialog();
            mPlaylistFragment.scrollToPlaylist(mRepository.getPlaylists().indexOf(result));
        } else {
            mView.showToast(R.string.playlistExists);
        }
    }

    @Override
    public void onRenamePlaylist(String oldName, String newName) {
        if (mRepository.renamePlaylist(oldName, newName)) {
            if (mModel.getPlaylistOpen() == null) {
                mPlaylistFragment.renamePlaylist(oldName, newName);
            }
            mView.hideNameDialog();
        } else {
            mView.showToast(R.string.playlistExists);
        }
    }

    @Override
    public void onPlaylistActionSelected(String playlistName, int position) {
        switch (position) {
            case 0:
                mView.showRenameDialog(playlistName);
                break;
            case 1:
                mView.showWarningDialog(new Object[][]{
                        new Object[]{R.string.you_really_want, false},
                        new Object[]{" ", false},
                        new Object[]{R.string.delete, true},
                        new Object[]{" ", false},
                        new Object[]{R.string.playlist, true},
                        new Object[]{" \"", false},
                        new Object[]{playlistName, false},
                        new Object[]{"\"", false},
                        new Object[]{"?", false},
                }, playlistName, 0);
                break;
        }
    }

    @Override
    public void onWarningClickedPositive(String parameter, int requestCode) {
        switch (requestCode) {
            case 0:
                mRepository.removePlaylistName(parameter);
                if (mRepository.getCurrentPlaylist() != null
                        && mRepository.getCurrentPlaylist().equals(parameter)) {
                    mRepository.setCurrentPlaylist(null);
                }
                mPlaylistFragment.removePlaylist(parameter);
        }
    }

    @Override
    public void onShowCurrentTrackInfo(MusicInfo musicInfo) {
        updateCurrentTrackInfo();
        if (mModel.isScrollToCurrentTrackLater()
                && mPlaylistFragment != null
                && mMusicListFragment != null) {
            onFindTrack(GlobalModel.getCurrentTrack().getPath());
            mModel.setScrollToCurrentTrackLater(false);
        }
    }

    @Override
    public void onUpdateTrackProgress(boolean isUnlockTrackProgress) {
        if (isUnlockTrackProgress) {
            mView.unlockTrackProgress();
        }
        if (mMusicFragment != null) {
            mMusicFragment.updateTrackProgressSafely(GlobalModel.getTrackProgress());
        }
        float progressPercent = (float) GlobalModel.getTrackProgress() / GlobalModel.getCurrentTrack().getDuration();
        Theme.updateProgressDrawable(progressPercent);
    }

    @Override
    public void onPlay() {
        if (mMusicFragment != null) {
            mMusicFragment.startMusic();
        }
        if (mPlaylistFragment != null) {
            mPlaylistFragment.startMusic();
        }
        if (mMusicListFragment != null) {
            mMusicListFragment.startMusic();
        }
    }

    @Override
    public void onPause() {
        if (mMusicFragment != null) {
            mMusicFragment.pauseMusic();
        }
        if (mPlaylistFragment != null) {
            mPlaylistFragment.pauseMusic();
        }
        if (mMusicListFragment != null) {
            mMusicListFragment.pauseMusic();
        }
    }

    @Override
    public void onRepeatTrack() {
        GlobalModel.setRepeatTrack(!GlobalModel.isRepeatTrack());
    }

    @Override
    public void onRepeatTrackChanged(boolean isRepeat) {

    }

    private PlaylistContract.Fragment mPlaylistFragment;

    @Override
    public void onPlaylistAvailable(PlaylistContract.Fragment fragment) {
        mPlaylistFragment = fragment;
        onShowCurrentTrackInfo(GlobalModel.getCurrentTrack());
        mPlaylistFragment.showPlaylists(mRepository.getPlaylists());
    }

    @Override
    public void onPlaylistAdd() {
        if (mModel.getPlaylistOpen() != null) {
            mView.showMusicSelector(mRepository.getPlaylist(mModel.getPlaylistOpen()));
        } else {
            mView.showCreateNameDialog();
        }
    }

    @Override
    public void onOpenPlaylist(String name) {
        mModel.setPlaylistOpen(name);
        mPlaylistFragment.showPlaylist(name, mRepository.getPlaylist(name));
        mView.showTitle(R.string.playlists, " -> " + name);
        mView.showToolbarArrow();
    }

    @Override
    public void onPlaylistMusicSelected(MusicInfo musicInfo) {
        onMusicChanged(musicInfo, mModel.getPlaylistOpen());
    }

    @Override
    public void onPlaylistTrackChanged(String path, boolean isChecked) {
        if (isChecked) {
            if (mRepository.addTrack(mModel.getPlaylistOpen(), path)) {
                mPlaylistFragment.addTrack(path);
                mView.showSelectedTrack(path);
            }
        } else {
            mRepository.removeTrack(mModel.getPlaylistOpen(), path);
            mPlaylistFragment.removeTrack(path);
            mView.hideSelectedTrack(path);
        }
    }

    @Override
    public void onPlaylistUpdate(List<String> playlist) {
        mRepository.updatePlaylist(mModel.getPlaylistOpen(), playlist);
        mPlaylistFragment.showPlaylist(mModel.getPlaylistOpen(), playlist);
    }

    private void updateCurrentTrackInfo() {
        MusicInfo musicInfo = GlobalModel.getCurrentTrack();
        if (mMusicFragment != null) {
            mMusicFragment.showCurrentTrack(musicInfo);
        }
        if (mPlaylistFragment != null) {
            mPlaylistFragment.showCurrentTrack(musicInfo);
        }
        if (mMusicListFragment != null) {
            mMusicListFragment.showCurrentTrack(musicInfo);
        }

        if (GlobalModel.isTrackPlay()) {
            onPlay();
        } else {
            onPause();
        }
        if (mMusicFragment != null) {
            mMusicFragment.repeatTrack(GlobalModel.isRepeatTrack());
            mMusicFragment.playRandomTrack(GlobalModel.isPlayRandomTrack());
        }
    }

    private SettingsContract.Fragment mSettingsFragment;

    @Override
    public void onSettingsFragmentAvailable(SettingsContract.Fragment view) {
        mSettingsFragment = view;
    }

    @Override
    public void onUpdateTheme() {
        mRepository.setThemeColorSet(Theme.getColorSet());
        mView.updateTheme();
        mMusicFragment.updateTheme();
        mPlaylistFragment.updateTheme();
        mMusicListFragment.updateTheme();
        mSettingsFragment.updateTheme();
    }

    @Override
    public void onUpdateContextTheme() {
        mRepository.setThemeContextDark(Theme.IS_DARK);
        mView.updateThemeContext();
        mMusicFragment.updateThemeContext();
        mPlaylistFragment.updateThemeContext();
        mMusicListFragment.updateThemeContext();
        mSettingsFragment.updateThemeContext();
        if (GlobalModel.isTrackPlay()) {
            onPlay();
        } else {
            onPause();
        }
    }
}
