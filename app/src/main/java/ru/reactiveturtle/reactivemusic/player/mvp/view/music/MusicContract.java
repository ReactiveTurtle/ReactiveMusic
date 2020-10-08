package ru.reactiveturtle.reactivemusic.player.mvp.view.music;

import android.graphics.drawable.BitmapDrawable;

import ru.reactiveturtle.reactivemusic.player.BaseMusicContract;

public interface MusicContract {
    interface Fragment extends BaseMusicContract.MusicControlFragment {
        void unlockTrackProgress();

        void updateTrackProgressSafely(int progress);

        void updateTrackProgress(int progress);

        void updateTrackCover(BitmapDrawable cover);

        void repeatTrack(boolean isRepeat);

        void playRandomTrack(boolean isRandom);
    }

    interface Presenter extends BaseMusicContract.FragmentPresenter {
        void onMusicFragmentAvailable(Fragment fragment);
    }
}
