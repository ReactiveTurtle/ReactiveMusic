package ru.reactiveturtle.reactivemusic.player;

import androidx.annotation.NonNull;

public interface BaseMusicContract {
    interface Repository extends BaseModel {
        void setCurrentMusic(MusicInfo musicInfo);

        MusicInfo getCurrentMusic();

        void setCurrentPlaylist(String playlist);

        String getCurrentPlaylist();
    }

    interface Model extends BaseModel {
        void setRepeatTrack(boolean isRepeat);

        boolean isRepeatTrack();
    }

    interface BaseModel {

    }

    interface MusicControlView extends View {
        void showCurrentTrack(MusicInfo musicInfo);

        void clearCurrentMusic();

        void startMusic();

        void pauseMusic();
    }

    interface View {
        void updateTheme();

        void updateThemeContext();
    }

    interface Presenter extends BasePresenter {
        void onRepeatTrackChanged(boolean isRepeat);
    }

    interface BasePresenter {
        void onPreviousTrack();

        void onPlayPause();

        void onNextTrack();

        void onRepeatTrack();
    }

    interface MusicControlFragment extends Fragment {
        void showCurrentTrack(MusicInfo musicInfo);

        void clearCurrentMusic();

        void startMusic();

        void pauseMusic();
    }

    interface Fragment extends View {
        void setPresenter(@NonNull FragmentPresenter presenter);
    }

    interface FragmentPresenter extends BasePresenter {

    }
}