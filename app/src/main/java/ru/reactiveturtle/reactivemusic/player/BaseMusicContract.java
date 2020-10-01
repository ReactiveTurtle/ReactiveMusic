package ru.reactiveturtle.reactivemusic.player;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface BaseMusicContract {
    interface Repository extends BaseModel {
        void setCurrentMusic(@Nullable MusicInfo musicInfo);

        @Nullable
        MusicInfo getCurrentMusic();

        void setCurrentPlaylist(String playlist);

        @Nullable
        String getCurrentPlaylist();

        void setRepeatTrack(boolean isRepeat);

        boolean isRepeatTrack();

        void setPlayRandomTrack(boolean isRandom);

        boolean isPlayRandomTrack();
    }

    interface Model extends BaseModel {
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
    }

    interface FragmentPresenter extends BasePresenter {

    }
}