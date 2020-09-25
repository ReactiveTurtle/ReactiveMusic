package ru.reactiveturtle.reactivemusic.player.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.Permissions;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.GlobalModel;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.mvp.PlayerContract;
import ru.reactiveturtle.reactivemusic.player.mvp.PlayerPresenter;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerRepository;
import ru.reactiveturtle.reactivemusic.player.mvp.view.selector.SelectMusicView;
import ru.reactiveturtle.reactivemusic.player.mvp.view.selector.SelectorContract;
import ru.reactiveturtle.reactivemusic.player.service.MusicBroadcastReceiver;
import ru.reactiveturtle.reactivemusic.player.service.MusicService;
import ru.reactiveturtle.tools.text.TextDialog;
import ru.reactiveturtle.tools.selection.SelectionDialog;

public class PlayerActivity extends AppCompatActivity implements PlayerContract.View {
    private PlayerContract.Presenter mPresenter;
    private SelectorContract.View mSelectMusicView;

    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.drawer_layout)
    protected DrawerLayout mDrawerLayout;

    @BindView(R.id.playerViewPager)
    protected ViewPager2 mPlayerViewPager;
    private PlayerPagerAdapter mPlayerPagerAdapter;

    @BindView(R.id.playerBottomNavigationView)
    protected BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void init() {
        setContentView(R.layout.player_activity);
        MobileAds.initialize(getApplicationContext(), initializationStatus -> {

        });

        MusicInfo.initDefault(getResources());
        PlayerRepository playerRepository = new PlayerRepository(this);
        Theme.init(getResources(), playerRepository.getThemeColorSet(), playerRepository.isThemeContextDark());

        ButterKnife.bind(this);

        mCreateNameDialogBuilder =
                Theme.getNameDialogBuilder()
                        .setHint(getResources().getString(R.string.type_playlist_name))
                        .setPositiveText(getResources().getString(R.string.create))
                        .setNegativeText(getResources().getString(R.string.cancel))
                        .setTitle(getResources().getString(R.string.playlist_creation))
                        .setTextSize(16)
                        .setText(getResources().getString(R.string.new_playlist))
                        .setTextSelected(true);

        mRenameDialogBuilder =
                Theme.getNameDialogBuilder()
                        .setHint(getResources().getString(R.string.type_playlist_name))
                        .setPositiveText(getResources().getString(R.string.save))
                        .setNegativeText(getResources().getString(R.string.cancel))
                        .setTitle(getResources().getString(R.string.change_playlist))
                        .setTextSize(16)
                        .setTextSelected(true);

        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(view -> {
            mPresenter.onBackPressed();
        });

        PlayerPresenter playerPresenter = new PlayerPresenter(playerRepository);
        mPresenter = playerPresenter;
        playerPresenter.onView(this);
        initPlayerViewPager();
        initSelectMusicView();
        mPlayerPagerAdapter.setPresenter(playerPresenter);
    }

    private void initSelectMusicView() {
        mSelectMusicView = new SelectMusicView(this);
        mSelectMusicView.setPresenter(mPresenter);
        Helper.initSelectMusic(this, mDrawerLayout);
        updateThemeContext();
        updateTheme();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Permissions.hasExternalStorage(this)) {
            if (mPresenter == null) {
                init();
            }
            if (!GlobalModel.isServiceRunning()) {
                startService(new Intent(this, MusicService.class));
            }
            GlobalModel.setActivityActive(GlobalModel.ActivityState.RESUMED);
            mSelectMusicView.onResume();
        } else {
            Permissions.requestExternalStorage(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Permissions.hasExternalStorage(this)) {
            GlobalModel.setActivityActive(GlobalModel.ActivityState.PAUSED);
            mSelectMusicView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        GlobalModel.setActivityActive(GlobalModel.ActivityState.STOPPED);
        if (mPresenter != null) {
            mPresenter.onStop();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
        } else {
            mPresenter.onBackPressed();
        }
    }

    private void initPlayerViewPager() {
        mPlayerViewPager.setOffscreenPageLimit(4);
        mPlayerPagerAdapter = new PlayerPagerAdapter(this);
        mPlayerViewPager.setAdapter(mPlayerPagerAdapter);
        mPlayerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                mToolbar.setTitle(mPlayerPagerAdapter.getTitle(position));
                switch (position) {
                    case 0:
                        mBottomNavigationView.setSelectedItemId(R.id.playerBottomPlayerItem);
                        break;
                    case 1:
                        mBottomNavigationView.setSelectedItemId(R.id.playerBottomListPlaylists);
                        break;
                    case 2:
                        mBottomNavigationView.setSelectedItemId(R.id.playerBottomListItem);
                        break;
                    case 3:
                        mBottomNavigationView.setSelectedItemId(R.id.playerBottomSettingsItem);
                        break;
                }
                mPresenter.onPageSelected(position);
            }
        });
        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.playerBottomPlayerItem:
                    mPlayerViewPager.setCurrentItem(0);
                    break;
                case R.id.playerBottomListPlaylists:
                    mPlayerViewPager.setCurrentItem(1);
                    break;
                case R.id.playerBottomListItem:
                    mPlayerViewPager.setCurrentItem(2);
                    break;
                case R.id.playerBottomSettingsItem:
                    mPlayerViewPager.setCurrentItem(3);
                    break;
            }
            return true;
        });
        if (getIntent().getIntExtra(Helper.ACTION_EXTRA, -1) == MusicBroadcastReceiver.SHOW_ACTIVITY) {
            mPresenter.onScrollToCurrentTrackLater();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == 0) {
                    startService(new Intent(this, MusicService.class));
                }
            }
        }
    }

    @Override
    public void pressBack() {
        super.onBackPressed();
    }

    @Override
    public void showDrawer() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        TranslateAnimation translateAnimation = new TranslateAnimation(
                dm.widthPixels, 0f, 0f, 0f);

        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setDuration(350);
        animationSet.addAnimation(translateAnimation);

        animationSet.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                mDrawerLayout.setVisibility(View.VISIBLE);
                mDrawerLayout.setBackgroundColor(Theme.IS_DARK ?
                        Theme.CONTEXT_PRIMARY_LIGHT : Theme.CONTEXT_PRIMARY);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                updateThemeContext();
                mDrawerLayout.setBackground(null);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mDrawerLayout.startAnimation(animationSet);
    }

    @Override
    public void sendPlayPreviousTrack() {
        sendIntent(MusicBroadcastReceiver.PLAY_PREVIOUS_TRACK);
    }

    @Override
    public void sendPlayNextTrack() {
        sendIntent(MusicBroadcastReceiver.PLAY_NEXT_TRACK);
    }

    @Override
    public void unlockTrackProgress() {
        mPlayerPagerAdapter.getPlayerFragment().unlockTrackProgress();
    }

    @Override
    public void showPage(int position) {
        mPlayerViewPager.setCurrentItem(position, true);
    }

    private TextDialog mTextDialog;

    private TextDialog.Builder mCreateNameDialogBuilder;

    @Override
    public void showCreateNameDialog() {
        mTextDialog = mCreateNameDialogBuilder.build();
        mTextDialog.setOnClickListener(new TextDialog.OnClickListener() {
            @Override
            public void onPositiveButtonClicked(String result) {
                super.onPositiveButtonClicked(result);
                mPresenter.onCreatePlaylist(result);
            }
        });
        mTextDialog.show(getSupportFragmentManager(), "nameDialog");
    }

    private TextDialog.Builder mRenameDialogBuilder;

    @Override
    public void showRenameDialog(String playlistName) {
        mRenameDialogBuilder.setText(playlistName);
        mTextDialog = mRenameDialogBuilder.build();
        mTextDialog.setOnClickListener(new TextDialog.OnClickListener() {
            @Override
            public void onPositiveButtonClicked(String result) {
                super.onPositiveButtonClicked(result);
                mPresenter.onRenamePlaylist(playlistName, result);
            }
        });
        mTextDialog.show(getSupportFragmentManager(), "renameDialog");
    }

    @Override
    public void hideNameDialog() {
        mTextDialog.dismiss();
    }

    @Override
    public void showMusicSelector(List<String> playlist) {
        if (Permissions.hasSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if (!mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.openDrawer(GravityCompat.END);
            }
            mSelectMusicView.setSelectedItems(playlist);
        } else {
            Permissions.requestPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, 1);
        }
    }

    @Override
    public void showSelectedTrack(@NonNull String path) {
        mSelectMusicView.showSelectedItem(path);
    }

    @Override
    public void hideSelectedTrack(@NonNull String path) {
        mSelectMusicView.hideSelectedItem(path);
    }

    @Override
    public void showPlaylistActions(String playlistName) {
        Spannable rename = new SpannableString(getResources().getString(R.string.rename));
        rename.setSpan(new ForegroundColorSpan(Theme.CONTEXT_NEGATIVE_PRIMARY),
                0, rename.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Spannable remove = new SpannableString(getResources().getString(R.string.delete));
        remove.setSpan(new ForegroundColorSpan(Color.RED),
                0, remove.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SelectionDialog selectionDialog = new SelectionDialog.Builder()
                .setBackground(new ColorDrawable(
                        Theme.IS_DARK ? Theme.CONTEXT_PRIMARY_LIGHT : Theme.CONTEXT_PRIMARY))
                .setTitle(getResources().getString(R.string.choose_an_action))
                .setDividerColor(Theme.setAlpha("8a", Theme.CONTEXT_LIGHT))
                .setTitleColor(Theme.CONTEXT_NEGATIVE_PRIMARY)
                .addItems(rename, remove)
                .build();
        selectionDialog.setOnItemClickListener((position, result) -> {
            mPresenter.onPlaylistActionSelected(playlistName, position);
            selectionDialog.dismiss();
        });
        selectionDialog.show(getSupportFragmentManager(), "selectionDialog");
    }

    @Override
    public void showToolbarArrow() {
        Drawable drawable = Helper.getNavigationIcon(getResources(), R.drawable.ic_arrow_upward, -90);
        if (!Theme.isVeryBright(Theme.getColorSet().getPrimary())) {
            Theme.changeColor(drawable, Color.WHITE);
        }
        mToolbar.setNavigationIcon(drawable);
    }

    @Override
    public void hideToolbarArrow() {
        mToolbar.setNavigationIcon(null);
    }

    @Override
    public void showTitle(int stringId) {
        showTitle(getString(stringId));
    }

    @Override
    public void showTitle(int stringId, String string) {
        showTitle(getString(stringId) + string);
    }

    @Override
    public void showTitle(String name) {
        mPlayerPagerAdapter.setTitle(mPlayerViewPager.getCurrentItem(), name);
        mToolbar.setTitle(name);
    }

    @Override
    public void showToast(int string) {
        Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateTheme() {
        mToolbar.setBackgroundColor(Theme.getColorSet().getPrimary());
        if (Theme.isVeryBright(Theme.getColorSet().getPrimary())) {
            mToolbar.setTitleTextColor(Color.BLACK);
        } else {
            mToolbar.setTitleTextColor(Color.WHITE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Theme.getColorSet().getPrimary());
        }
        updateBottomNavigationView();
        mSelectMusicView.updateTheme();
    }

    @Override
    public void updateThemeContext() {
        if (!GlobalModel.isFirstTrackLoad()) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Theme.IS_DARK ?
                    Theme.CONTEXT_PRIMARY_LIGHT : Theme.CONTEXT_PRIMARY));
        }
        mBottomNavigationView.setBackgroundColor(Theme.IS_DARK ?
                Theme.CONTEXT_LIGHT : Theme.CONTEXT_PRIMARY);
        updateBottomNavigationView();
        mSelectMusicView.updateThemeContext();
    }

    public void updateBottomNavigationView() {
        ColorStateList colorStateList = new ColorStateList(new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        }, new int[]{Theme.isGrey(Theme.getColorSet().getPrimary()) ?
                Theme.CONTEXT_NEGATIVE_PRIMARY : (Theme.IS_DARK ?
                Theme.getColorSet().getPrimaryLight() : Theme.getColorSet().getPrimary()),
                Color.argb(Integer.parseInt("99", 16),
                        Color.red(Theme.CONTEXT_NEGATIVE_PRIMARY),
                        Color.green(Theme.CONTEXT_NEGATIVE_PRIMARY),
                        Color.blue(Theme.CONTEXT_NEGATIVE_PRIMARY))});
        mBottomNavigationView.setItemIconTintList(colorStateList);
        mBottomNavigationView.setItemTextColor(colorStateList);
    }

    private void sendIntent(int action) {
        Intent intent = new Intent(MusicService.class.getName());
        intent.putExtra(Helper.ACTION_EXTRA, action);
        sendBroadcast(intent);
    }
}