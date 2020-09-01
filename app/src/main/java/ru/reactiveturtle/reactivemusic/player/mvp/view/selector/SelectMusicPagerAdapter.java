package ru.reactiveturtle.reactivemusic.player.mvp.view.selector;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.reactiveturtle.reactivemusic.R;

public class SelectMusicPagerAdapter extends FragmentStateAdapter {
    private List<Fragment> fragments = new ArrayList<>();

    public SelectMusicPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    public void add(Fragment... fragments) {
        this.fragments.addAll(Arrays.asList(fragments));
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }

    public <T> T getMusicFragment(Class<T> c) {
        return c.cast(fragments.get(0));
    }

    public <T> T getFilesFragment(Class<T> c) {
        return c.cast(fragments.get(1));
    }

    @StringRes
    public int getPageTitle(int position) {
        switch (position) {
            case 0:
                return R.string.music;
            case 1:
                return R.string.files;
        }
        return android.R.string.unknownName;
    }

    public Fragment getFragment(int position) {
        return fragments.get(position);
    }
}
