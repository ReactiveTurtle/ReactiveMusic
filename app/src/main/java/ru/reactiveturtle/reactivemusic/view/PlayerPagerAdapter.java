package ru.reactiveturtle.reactivemusic.view;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.Objects;

import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.musicservice.MusicService;

public class PlayerPagerAdapter extends FragmentStateAdapter {
    private final MusicService.Binder binder;
    private Fragment[] fragments = new Fragment[4];
    private String[] fragmentTitles = new String[4];

    public PlayerPagerAdapter(@NonNull FragmentActivity fragmentActivity, MusicService.Binder binder) {
        super(fragmentActivity);
        Objects.requireNonNull(binder);
        this.binder = binder;
        Resources resources = fragmentActivity.getResources();
        fragmentTitles[0] = resources.getString(R.string.player);
        fragmentTitles[1] = resources.getString(R.string.playlists);
        fragmentTitles[2] = resources.getString(R.string.music);
        fragmentTitles[3] = resources.getString(R.string.settings);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                MusicFragment musicFragment = new MusicFragment();
                musicFragment.bind(binder);
                fragment = musicFragment;
                break;
            case 2:
                MusicListFragment musicListFragment = MusicListFragment.newInstance();
                musicListFragment.bind(binder);
                fragment = musicListFragment;
                break;
            case 1:
            case 3:
                fragment = new Fragment();
                break;
            default:
                throw new IndexOutOfBoundsException("Fragment index " + position + " >= fragments counts " + getItemCount());
        }
        fragments[position] = fragment;
        return (Fragment) fragment;
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }

    public void setTitle(int currentItem, String name) {
        fragmentTitles[currentItem] = name;
    }

    public String getTitle(int position) {
        return fragmentTitles[position];
    }
}
