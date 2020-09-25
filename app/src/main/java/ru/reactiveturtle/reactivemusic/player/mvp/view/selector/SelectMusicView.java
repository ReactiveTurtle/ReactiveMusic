package ru.reactiveturtle.reactivemusic.player.mvp.view.selector;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.mvp.PlayerContract;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;

public class SelectMusicView implements SelectorContract.View {
    private ViewPager2 mViewPager;
    private SelectMusicPagerAdapter mPagerAdapter;
    private MediaPlayer mTestMediaPlayer;

    @BindView(R.id.selectMusicToolbar)
    protected Toolbar mToolbar;

    public SelectMusicView(FragmentActivity activity) {
        mTestMediaPlayer = new MediaPlayer();
        ButterKnife.bind(this, activity.findViewById(R.id.drawer_layout));
        mToolbar.setNavigationIcon(Helper.getNavigationIcon(activity.getResources(), R.drawable.ic_arrow_upward, -90));
        mToolbar.setNavigationOnClickListener(item -> {
            ((DrawerLayout) activity.findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.END);
        });

        mViewPager = activity.findViewById(R.id.selectMusicViewPager);
        TabLayout tabLayout = activity.findViewById(R.id.selectMusicTabLayout);
        mPagerAdapter = new SelectMusicPagerAdapter(activity);
        mPagerAdapter.add(new SelectMusicListFragment(), new SelectMusicFilesFragment());

        mViewPager.setAdapter(mPagerAdapter);
        new TabLayoutMediator(tabLayout, mViewPager, (tab, position) -> {
            mViewPager.setCurrentItem(position);
            tab.setText(mPagerAdapter.getPageTitle(position));
        }).attach();
        mViewPager.setCurrentItem(0);
        mViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0) {
                    ((DrawerLayout) activity.findViewById(R.id.drawer_layout))
                            .setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED, GravityCompat.END);
                } else {
                    ((DrawerLayout) activity.findViewById(R.id.drawer_layout))
                            .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN, GravityCompat.END);
                }
            }
        });

        mPagerAdapter.getFilesFragment(SelectMusicFilesFragment.class).setOnCheckListener(new SelectMusicFilesFragment.OnCheckListener() {
            @Override
            public void onChecked(String path, boolean exists) {
                if (exists) {
                    mPresenter.onPlaylistTrackChanged(path, false);
                    mPagerAdapter.getMusicFragment(SelectMusicListFragment.class).removeSelectedTrack(path);
                } else {
                    try {
                        mTestMediaPlayer.reset();
                        mTestMediaPlayer.setDataSource(path);
                        mTestMediaPlayer.prepare();
                        mPresenter.onPlaylistTrackChanged(path, true);
                        mPagerAdapter.getMusicFragment(SelectMusicListFragment.class).addSelectedTrack(path);
                    } catch (IOException ignored) {
                        mPagerAdapter.getFilesFragment(SelectMusicFilesFragment.class).removeSelectedFile(path);
                        Toast.makeText(mViewPager.getContext(), "В файле не записан звук", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onUpdate(List<String> selectedFiles) {
                mPagerAdapter.getMusicFragment(SelectMusicListFragment.class).setSelectedItems(selectedFiles);
                mPagerAdapter.getFilesFragment(SelectMusicFilesFragment.class).setSelectedItems(selectedFiles);
                mPresenter.onPlaylistUpdate(selectedFiles);
            }
        });
        mPagerAdapter.getMusicFragment(SelectMusicListFragment.class).setOnCheckListener(new SelectMusicListFragment.OnCheckListener() {
            @Override
            public void onChecked(String path, boolean exists) {
                mPresenter.onPlaylistTrackChanged(path, !exists);
                if (exists) {
                    mPagerAdapter.getFilesFragment(SelectMusicFilesFragment.class).removeSelectedFile(path);
                } else {
                    mPagerAdapter.getFilesFragment(SelectMusicFilesFragment.class).addSelectedFile(path);
                }
            }

            @Override
            public void onUpdate(List<String> playlist) {
                mPagerAdapter.getMusicFragment(SelectMusicListFragment.class).setSelectedItems(playlist);
                mPagerAdapter.getFilesFragment(SelectMusicFilesFragment.class).setSelectedItems(playlist);
                mPresenter.onPlaylistUpdate(playlist);
            }
        });
    }

    public void onDestroy() {
        mTestMediaPlayer.release();
    }

    public void setViewPagerPosition(int position) {
        mViewPager.setCurrentItem(position);
    }

    private SelectorContract.Presenter mPresenter;

    @Override
    public void setPresenter(@NonNull PlayerContract.Presenter presenter) {
        mPresenter = (SelectorContract.Presenter) presenter;
    }

    @Override
    public void setSelectedItems(List<String> playlist) {
        mPagerAdapter.getMusicFragment(SelectMusicListFragment.class).setSelectedItems(playlist);
        mPagerAdapter.getFilesFragment(SelectMusicFilesFragment.class).setSelectedItems(playlist);
    }

    @Override
    public void showSelectedItem(@NonNull String path) {
        mPagerAdapter.getMusicFragment(SelectMusicListFragment.class).addSelectedTrack(path);
        mPagerAdapter.getFilesFragment(SelectMusicFilesFragment.class).addSelectedFile(path);
    }

    @Override
    public void hideSelectedItem(@NonNull String path) {
        mPagerAdapter.getMusicFragment(SelectMusicListFragment.class).removeSelectedTrack(path);
        mPagerAdapter.getFilesFragment(SelectMusicFilesFragment.class).removeSelectedFile(path);
    }

    @Override
    public void onResume() {
        mTestMediaPlayer = new MediaPlayer();
    }

    @Override
    public void onPause() {
        mTestMediaPlayer.release();
    }

    @Override
    public void updateTheme() {
        mToolbar.setBackgroundColor(Theme.getColorSet().getPrimary());
        Drawable drawable = Helper.getNavigationIcon(
                mToolbar.getResources(), R.drawable.ic_arrow_upward, -90);
        mToolbar.setNavigationIcon(drawable);
        if (Theme.isVeryBright(Theme.getColorSet().getPrimary())) {
            mToolbar.setTitleTextColor(Color.BLACK);
        } else {
            Theme.changeColor(drawable, Color.WHITE);
            mToolbar.setTitleTextColor(Color.WHITE);
        }
        mPagerAdapter.getMusicFragment(SelectMusicListFragment.class).updateTheme();
        mPagerAdapter.getFilesFragment(SelectMusicFilesFragment.class).updateTheme();
    }

    @Override
    public void updateThemeContext() {
        mViewPager.setBackgroundColor(Theme.CONTEXT_PRIMARY);
        mPagerAdapter.getFilesFragment(SelectMusicFilesFragment.class).updateThemeContext();
    }
}
