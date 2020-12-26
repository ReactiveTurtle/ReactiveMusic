package ru.reactiveturtle.reactivemusic.player.mvp.view;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.mvp.view.list.MusicListFragment;
import ru.reactiveturtle.reactivemusic.player.mvp.view.music.MusicFragment;
import ru.reactiveturtle.reactivemusic.player.mvp.view.playlist.PlaylistAdapter;
import ru.reactiveturtle.reactivemusic.player.mvp.view.playlist.PlaylistFragment;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.SettingsFragment;

public class PlayerPagerAdapter extends FragmentStateAdapter {
    private Fragment[] fragments = new Fragment[4];
    private String[] fragmentTitles = new String[4];

    public PlayerPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
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
                fragment = MusicFragment.newInstance();
                break;
            case 1:
                fragment = PlaylistFragment.newInstance();
                break;
            case 2:
                fragment = MusicListFragment.newInstance();
                break;
            case 3:
                fragment = new SettingsFragment();
                break;
            default:
                throw new IndexOutOfBoundsException(
                        "Fragment index " + position + " >= fragments counts " + getItemCount()
                );
        }
        fragments[position] = fragment;
        return (Fragment) fragment;
    }

    @Override
    public int getItemCount() {
        return fragments.length;
    }

    public MusicFragment getPlayerFragment() {
        return (MusicFragment) fragments[0];
    }

    public PlaylistFragment getPlaylistFragment() {
        return (PlaylistFragment) fragments[1];
    }

    public MusicListFragment getListFragment() {
        return (MusicListFragment) fragments[2];
    }

    public void setTitle(int currentItem, String name) {
        fragmentTitles[currentItem] = name;
    }

    public String getTitle(int position) {
        return fragmentTitles[position];
    }
}
