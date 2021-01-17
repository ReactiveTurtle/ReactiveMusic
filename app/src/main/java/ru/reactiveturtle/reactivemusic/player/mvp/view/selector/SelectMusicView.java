package ru.reactiveturtle.reactivemusic.player.mvp.view.selector;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ThemeHelper;
import ru.reactiveturtle.reactivemusic.player.service.Bridges;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;

public class SelectMusicView {
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
        mPagerAdapter.add(new SelectMusicListFragment());

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
        mPagerAdapter.getMusicFragment(SelectMusicListFragment.class).setOnCheckListener(new SelectMusicListFragment.OnCheckListener() {
            @Override
            public void onChecked(String path, boolean exists) {
                if (!exists) {
                    ReactiveArchitect.getStringBridge(Bridges.SelectMusicListFragment_To_AddTrack).pull(path);
                    ReactiveArchitect.getStringBridge(Bridges.SelectMusicListFragment_To_AddTrackPlaylist).pull(path);
                } else {
                    ReactiveArchitect.getStringBridge(Bridges.SelectMusicListFragment_To_RemoveTrack).pull(path);
                    ReactiveArchitect.getStringBridge(Bridges.SelectMusicListFragment_To_RemoveTrackPlaylist).pull(path);
                }
            }

            @Override
            public void onUpdate(List<String> playlist) {

            }
        });
    }

    public void onResume() {
        mTestMediaPlayer = new MediaPlayer();
    }

    public void onPause() {
        mTestMediaPlayer.release();
    }

    public SelectMusicListFragment getSelectMusicList() {
        return (SelectMusicListFragment) mPagerAdapter.getFragment(0);
    }

    public void updateTheme() {
        mToolbar.setBackgroundColor(Theme.getColorSet().getPrimary());
        Drawable drawable = Helper.getNavigationIcon(
                mToolbar.getResources(), R.drawable.ic_arrow_upward, -90);
        mToolbar.setNavigationIcon(drawable);
        if (Theme.isVeryBright(Theme.getColorSet().getPrimary())) {
            mToolbar.setTitleTextColor(Color.BLACK);
        } else {
            ThemeHelper.changeColor(drawable, Color.WHITE);
            mToolbar.setTitleTextColor(Color.WHITE);
        }
        mPagerAdapter.getMusicFragment(SelectMusicListFragment.class).updateTheme();
    }

    public void updateThemeContext() {
    }

    public void updateBackground(Drawable drawable) {
        mViewPager.setBackground(drawable);
    }
}
