package ru.reactiveturtle.reactivemusic.player.mvp.view.music;

import ru.reactiveturtle.reactivemusic.player.BaseMusicContract;

public interface MusicContract {
    interface Fragment extends BaseMusicContract.MusicControlFragment {
        void unlockTrackProgress();

        void updateTrackProgressSafely(int progress);

        void updateTrackProgress(int progress);

        void repeatTrack(boolean isRepeat);
    }

    interface Presenter extends BaseMusicContract.FragmentPresenter {
        void onMusicFragmentAvailable(Fragment fragment);
    }
}