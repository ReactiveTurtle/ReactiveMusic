package ru.reactiveturtle.reactivemusic.player.mvp.view;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
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
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.Permissions;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerModel;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerRepository;
import ru.reactiveturtle.reactivemusic.player.mvp.view.selector.SelectMusicView;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ColorSet;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ThemeHelper;
import ru.reactiveturtle.reactivemusic.player.service.Bridges;
import ru.reactiveturtle.reactivemusic.player.service.MusicModel;
import ru.reactiveturtle.reactivemusic.player.service.MusicService;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;
import ru.reactiveturtle.tools.widget.selection.SelectionDialog;
import ru.reactiveturtle.tools.widget.text.TextDialog;
import ru.reactiveturtle.tools.widget.warning.MessageDialog;

public class PlayerActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    protected Toolbar mToolbar;
    @BindView(R.id.drawer_layout)
    protected DrawerLayout mDrawerLayout;

    @BindView(R.id.playerViewPager)
    protected ViewPager2 mPlayerViewPager;
    private PlayerPagerAdapter mPlayerPagerAdapter;

    @BindView(R.id.playerBottomNavigationView)
    protected BottomNavigationView mBottomNavigationView;

    private PlayerRepository mRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //enableScreenMode();
        if (Permissions.hasExternalStorage(this)) {
            create();
        } else {
            Permissions.requestExternalStorage(this);
        }
    }

    private void create() {
        mRepository = new PlayerRepository(this);
        if (ReactiveArchitect.getBridge(Bridges.MusicService_To_Init) == null) {
            Theme.init(getResources(), mRepository.getThemeColorSet(), mRepository.isThemeContextDark());
            PlayerModel.initialize(mRepository);
            ReactiveArchitect.createBridge(Bridges.MusicService_To_Init).connect(() -> {
                runOnUiThread(this::init);
            });
            startService(new Intent(this, MusicService.class));
        } else {
            init();
        }
    }

    private void enableScreenshotMode() {
        int height = getResources().getDisplayMetrics().heightPixels;
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        getWindow().getAttributes().height = 1920 - statusBarHeight;
        getWindow().getAttributes().y = getWindow().getAttributes().height - height;
    }

    private void init() {
        setContentView(R.layout.player_activity);
        MobileAds.initialize(getApplicationContext(), initializationStatus -> {

        });

        MusicInfo.initDefault(getResources());

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

        initPlayerViewPager();

        ReactiveArchitect.createStringBridge(Bridges.PlaylistAddClick_To_ShowCreateNameDialog).connect((value) -> {
            String openPlaylist = PlayerModel.getOpenPlaylistName();
            if (openPlaylist != null) {
                showMusicSelector(mRepository.getPlaylist(openPlaylist));
            } else {
                showCreateNameDialog();
            }
        });

        ReactiveArchitect.createStringBridge(Bridges.PlaylistFragment_To_ShowTrackActions)
                .connect(this::showTrackActions);

        ReactiveArchitect.createStringBridge(Bridges.PlaylistOpen_To_ShowPlaylist)
                .connect(param -> {
                    PlayerModel.setOpenPlaylistName(param);
                    PlayerModel.setOpenPlaylist(mRepository.getPlaylist(param));
                    showTitle(R.string.playlists, " -> " + param);
                    showToolbarArrow();
                });

        ReactiveArchitect.createBridge(Bridges.PlaylistClick_To_FindTrack)
                .connect(() -> {
                    goToCurrentTrack(mRepository.getCurrentPlaylist(), MusicModel.getCurrentTrackPath());
                });

        ReactiveArchitect.createBridge(Bridges.PlaylistClick_To_FindTrackInMainPlaylist)
                .connect(() -> {
                    goToCurrentTrack(null, MusicModel.getCurrentTrackPath());
                });
        ReactiveArchitect.createStringBridge(Bridges.SettingsClick_To_UpdateToolbarArrow)
                .connect(param -> {
                    showToolbarArrow();
                });


        ReactiveArchitect.getStateKeeper(PlayerModel.PLAYLIST_ACTIONS_PLAYLIST_NAME).subscribe((view, value) -> {
            if (value != null) {
                showPlaylistActions((String) value);
            }
        });

        ReactiveArchitect.getStateKeeper(Theme.COLOR_SET).subscribe((view, value) ->
                mRepository.setThemeColorSet((ColorSet) value));

        PlayerModel.setPlaylists(mRepository.getPlaylists());

        initSelectMusicView();
        showDrawer();
        ReactiveArchitect.getStateKeeper(Theme.COLOR_SET).subscribe((view, value) -> {
            updateTheme();
        }).call();
        ReactiveArchitect.getStateKeeper(Theme.IS_DARK).subscribe((view, value) -> {
            mRepository.setThemeContextDark((Boolean) value);
            updateThemeContext();
        }).call();
        ReactiveArchitect.getStateKeeper(MusicModel.CURRENT_TRACK_COVER).subscribe((view, value) -> updateWindowBackground());
    }

    private SelectMusicView selectMusicView;

    private void initSelectMusicView() {
        selectMusicView = new SelectMusicView(this);
        Helper.initSelectMusic(this, mDrawerLayout);
        ReactiveArchitect.createBridge(Bridges.SelectMusicListFragment_To_ViewLoaded)
                .connect(this::bindMusicLists);
        ReactiveArchitect.createStringBridge(Bridges.SelectMusicListFragment_To_AddTrack).connect(param -> {
            String playlistName = Objects.requireNonNull(PlayerModel.getOpenPlaylistName());
            mRepository.addTrack(playlistName, param);
        });
        ReactiveArchitect.createStringBridge(Bridges.SelectMusicListFragment_To_RemoveTrack).connect(param -> {
            String playlistName = Objects.requireNonNull(PlayerModel.getOpenPlaylistName());
            mRepository.removeTrack(playlistName, param);
        });
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                selectMusicView.getSelectMusicList().setSelectedItems(
                        mRepository.getPlaylist(PlayerModel.getOpenPlaylistName()));
                selectMusicView.getSelectMusicList().getListAdapter().notifyDataSetChanged();
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Permissions.hasExternalStorage(this)) {
            PlayerModel.setActivityActive(true);
            if (MusicModel.isInitialized()) {
                PlayerModel.setPlay(MusicModel.isTrackPlay());
            }
            if (mPlayerPagerAdapter != null) {
                mPlayerPagerAdapter.notifyDataSetChanged();
            }
        } else {
            Permissions.requestExternalStorage(this);
        }
    }

    @Override
    protected void onPause() {
        if (Permissions.hasExternalStorage(this)) {
            PlayerModel.setActivityActive(false);
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ReactiveArchitect.removeBridge(Bridges.PlaylistAddClick_To_ShowCreateNameDialog);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            mDrawerLayout.closeDrawer(GravityCompat.END);
        } else {
            if (PlayerModel.getSelectedPage() == 1
                    && PlayerModel.getOpenPlaylistName() != null) {
                PlayerModel.setOpenPlaylistName(null);
                PlayerModel.setPlaylists(mRepository.getPlaylists());
                showTitle(R.string.playlists);
                hideToolbarArrow();
            } else if (PlayerModel.getSelectedPage() == 3 &&
                    mPlayerPagerAdapter.getSettingsFragment().pressBack()) {
                hideToolbarArrow();
            } else {
                super.onBackPressed();
            }
        }
    }

    private void initPlayerViewPager() {
        mPlayerViewPager.setOffscreenPageLimit(4);
        mPlayerPagerAdapter = new PlayerPagerAdapter(this);
        mPlayerViewPager.setAdapter(mPlayerPagerAdapter);
        mDrawerLayout.setVisibility(View.VISIBLE);
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
                PlayerModel.setSelectedPage(position);
                if (position == 1) {
                    if (PlayerModel.getOpenPlaylistName() != null) {
                        showToolbarArrow();
                    } else {
                        hideToolbarArrow();
                    }
                } else if (position == 3) {
                    if (mPlayerPagerAdapter.getSettingsFragment().isPressBack()) {
                        //showToolbarArrow();
                    } else {
                        hideToolbarArrow();
                    }
                } else
                    hideToolbarArrow();
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
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (requestCode == 0) {
                    create();
                }
            } else {
                if (requestCode == 0) {
                    Permissions.requestExternalStorage(this);
                }
            }
        }
    }

    public void showDrawer() {
        updateWindowBackground();
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
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                updateThemeContext();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mDrawerLayout.startAnimation(animationSet);
    }

    private TextDialog mTextDialog;

    private TextDialog.Builder mCreateNameDialogBuilder;

    public void showCreateNameDialog() {
        mTextDialog = mCreateNameDialogBuilder.build();
        mTextDialog.setOnClickListener(new TextDialog.OnClickListener() {
            @Override
            public void onPositiveButtonClicked(String result) {
                super.onPositiveButtonClicked(result);
                if (mRepository.addPlaylistName(result)) {
                    hideNameDialog();
                    ReactiveArchitect.getStringBridge(Bridges.PlaylistCreated_To_UpdateFragment)
                            .pull(result);
                } else {
                    showToast(R.string.playlistExists);
                }
            }
        });
        mTextDialog.show(getSupportFragmentManager(), "nameDialog");
    }

    private TextDialog.Builder mRenameDialogBuilder;

    public void showRenameDialog(String playlistName) {
        mRenameDialogBuilder.setText(playlistName);
        mTextDialog = mRenameDialogBuilder.build();
        mTextDialog.setOnClickListener(new TextDialog.OnClickListener() {
            @Override
            public void onPositiveButtonClicked(String result) {
                super.onPositiveButtonClicked(result);
                if (mRepository.renamePlaylist(playlistName, result)) {
                    if (PlayerModel.getOpenPlaylistName() == null) {
                        ReactiveArchitect.getStringBridge(Bridges.RenameDialog_To_RenamePlaylistList).pull(result);
                    }
                    hideNameDialog();
                } else {
                    showToast(R.string.playlistExists);
                }
            }
        });
        mTextDialog.show(getSupportFragmentManager(), "renameDialog");
    }

    public void hideNameDialog() {
        mTextDialog.dismiss();
    }

    public void bindMusicLists() {
        if (selectMusicView.getSelectMusicList().getListAdapter() != null) {
            mPlayerPagerAdapter.getListFragment().bindLists(selectMusicView.getSelectMusicList().getListAdapter());
        }
    }

    public void showMusicSelector(List<String> playlist) {
        if (Permissions.hasExternalStorage(this)) {
            if (!mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.openDrawer(GravityCompat.END);
            }
        } else {
            Permissions.requestPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE, 1);
        }
    }

    public void showPlaylistActions(String playlistName) {
        Spannable rename = new SpannableString("•  " + getResources().getString(R.string.rename));
        rename.setSpan(new ForegroundColorSpan(Theme.CONTEXT_NEGATIVE_PRIMARY),
                0, rename.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Spannable remove = new SpannableString("•  " + getResources().getString(R.string.delete));
        remove.setSpan(new ForegroundColorSpan(Color.RED),
                0, remove.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SelectionDialog selectionDialog = new SelectionDialog.Builder()
                .setBackground(new ColorDrawable(
                        Theme.isDark() ? Theme.CONTEXT_PRIMARY_LIGHT : Theme.CONTEXT_PRIMARY))
                .setTitle(getResources().getString(R.string.choose_an_action))
                .setDividerColor(ThemeHelper.setAlpha("8a", Theme.CONTEXT_LIGHT))
                .setTitleColor(Theme.CONTEXT_NEGATIVE_PRIMARY)
                .addItems(rename, remove)
                .build();
        selectionDialog.setOnItemClickListener((position, result) -> {
            selectionDialog.dismiss();
            switch (position) {
                case 0:
                    showRenameDialog(playlistName);
                    break;
                case 1:
                    showWarningDialog(Arrays.asList(
                            new Object[]{R.string.you_really_want, false},
                            new Object[]{" ", false},
                            new Object[]{R.string.delete, true},
                            new Object[]{" ", false},
                            new Object[]{R.string.playlist, true},
                            new Object[]{" \"", false},
                            new Object[]{playlistName, false},
                            new Object[]{"\"", false},
                            new Object[]{"?", false}
                    ), playlistName, 0);
                    break;
            }
        });
        selectionDialog.show(getSupportFragmentManager(), "selectionDialog");
    }

    public void showTrackActions(String trackPath) {
        Spannable rename = new SpannableString("•  " + getResources().getString(R.string.delete_from_playlist));
        rename.setSpan(new ForegroundColorSpan(Color.parseColor("#FF0000")),
                0, rename.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        Spannable remove = new SpannableString("•  " + getResources().getString(R.string.delete_from_storage));
        remove.setSpan(new ForegroundColorSpan(Color.parseColor("#AA0000")),
                0, remove.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SelectionDialog selectionDialog = new SelectionDialog.Builder()
                .setBackground(new ColorDrawable(
                        Theme.isDark() ? Theme.CONTEXT_PRIMARY_LIGHT : Theme.CONTEXT_PRIMARY))
                .setTitle(getResources().getString(R.string.choose_an_action))
                .setDividerColor(ThemeHelper.setAlpha("8a", Theme.CONTEXT_LIGHT))
                .setTitleColor(Theme.CONTEXT_NEGATIVE_PRIMARY)
                .addItems(rename, remove)
                .build();
        selectionDialog.setOnItemClickListener((position, result) -> {
            selectionDialog.dismiss();
            List<Object[]> text = new ArrayList<>(Arrays.asList(
                    new Object[]{R.string.you_really_want, false},
                    new Object[]{" ", false},
                    new Object[]{R.string.delete, true},
                    new Object[]{" \"", false},
                    new Object[]{trackPath, false},
                    new Object[]{"\" ", false},
                    new Object[]{"?", false}));
            switch (position) {
                case 0:
                    text.add(text.size() - 1, new Object[]{R.string.from_playlist, true});
                    showWarningDialog(text, trackPath, 1);
                    break;
                case 1:
                    text.add(text.size() - 1, new Object[]{R.string.from_storage, true});
                    showWarningDialog(text, trackPath, 2);
                    break;
            }
        });
        selectionDialog.show(getSupportFragmentManager(), "selectionDialog");
    }

    private MessageDialog.Builder mMessageDialogBuilder;

    public void showWarningDialog(List<Object[]> stringBuilder, String parameter, int requestCode) {
        StringBuilder builder = new StringBuilder();
        for (Object[] object : stringBuilder) {
            String string = "";
            if (object[0] instanceof Integer) {
                string = getResources().getString((Integer) object[0]);
            } else if (object[0] instanceof String) {
                string = (String) object[0];
            }
            if ((Boolean) object[1]) {
                string = string.toLowerCase();
            }
            builder.append(string);
        }
        mMessageDialogBuilder.setTitle(builder.toString().trim());
        MessageDialog messageDialog = mMessageDialogBuilder.buildDialog();
        messageDialog.setOnClickListener(new MessageDialog.OnClickListener() {
            @Override
            public void onPositiveButtonClicked() {
                messageDialog.dismiss();
                switch (requestCode) {
                    case 0:
                        mRepository.removePlaylistName(parameter);
                        if (mRepository.getCurrentPlaylist() != null
                                && mRepository.getCurrentPlaylist().equals(parameter)) {
                            mRepository.setCurrentPlaylist(null);
                        }
                        mPlayerPagerAdapter.getPlaylistFragment().removePlaylist(parameter);
                        break;
                    case 1:

                        break;
                    case 2:
                        File file = new File(parameter);
                        if (file.delete()) {
                            mPlayerPagerAdapter.getListFragment().removeTrack(parameter);
                        }
                        break;
                }
            }
        });
        messageDialog.show(getSupportFragmentManager(), "MessageDialog");
    }

    public void goToCurrentTrack(String playlistName, String trackPath) {
        List<String> playlist =
                mRepository.getPlaylist(playlistName);
        int index = playlist.indexOf(trackPath);
        if (index > -1) {
            if (playlistName != null) {
                if (PlayerModel.getSelectedPage() != 1) {
                    mPlayerViewPager.setCurrentItem(1);
                }
                if (PlayerModel.getOpenPlaylistName() == null) {
                    ReactiveArchitect.getStringBridge(Bridges.PlaylistOpen_To_ShowPlaylist).pull(playlistName);
                }
                ReactiveArchitect.getStringBridge(Bridges.FindTrack_To_PlaylistScrollToTrack).pull(trackPath);
            } else {
                if (PlayerModel.getSelectedPage() != 2) {
                    mPlayerViewPager.setCurrentItem(2);
                }
                ReactiveArchitect.getStringBridge(Bridges.FindTrack_To_MusicListScrollToTrack).pull(trackPath);
            }
        } else {
            showToast(R.string.track_not_found);
        }
    }

    public void showToolbarArrow() {
        Drawable drawable = Helper.getNavigationIcon(getResources(), R.drawable.ic_arrow_upward, -90);
        if (!Theme.isVeryBright(Theme.getColorSet().getPrimary())) {
            ThemeHelper.changeColor(drawable, Color.WHITE);
        }
        mToolbar.setNavigationIcon(drawable);
    }

    public void hideToolbarArrow() {
        mToolbar.setNavigationIcon(null);
    }

    public void showTitle(int stringId) {
        showTitle(getString(stringId));
    }

    public void showTitle(int stringId, String string) {
        showTitle(getString(stringId) + string);
    }

    public void showTitle(String name) {
        mPlayerPagerAdapter.setTitle(mPlayerViewPager.getCurrentItem(), name);
        mToolbar.setTitle(name);
    }

    public void showToast(int string) {
        Toast.makeText(getApplicationContext(), string, Toast.LENGTH_SHORT).show();
    }

    public void updateWindowBackground() {
        if (true) {
            Bitmap bitmap;
            Canvas canvas;
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            if (MusicModel.getCurrentTrackCover() == null ||
                    MusicModel.getCurrentTrackCover().getBitmap().equals(
                            Theme.getDefaultAlbumCover().getBitmap())) {
                bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);
                paint.setShader(new RadialGradient(0, 0, 512,
                        new int[]{
                                Theme.getColorSet().getPrimaryDark(),
                                Theme.getColorSet().getPrimaryDark(),
                                Theme.isDark() ?
                                        Theme.CONTEXT_PRIMARY_LIGHT : Theme.CONTEXT_PRIMARY
                        },
                        new float[]{
                                0, 0.5f, 1
                        }, Shader.TileMode.CLAMP));
                canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);

                paint.setShader(new RadialGradient(256, 256, 256,
                        new int[]{
                                Theme.getColorSet().getPrimary(),
                                Color.TRANSPARENT
                        },
                        new float[]{
                                0, 1
                        }, Shader.TileMode.CLAMP));
                canvas.drawRect(0, 0, bitmap.getWidth(), bitmap.getHeight(), paint);
                paint.setShader(null);
            } else {
                bitmap = MusicModel.getCurrentTrackCover().getBitmap()
                        .copy(Bitmap.Config.ARGB_8888, true);
            }
            DisplayMetrics dm = getResources().getDisplayMetrics();
            Bitmap back;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                float aspectRatio = dm.widthPixels / (float) dm.heightPixels;
                back = Bitmap.createBitmap((int) (bitmap.getHeight() * aspectRatio),
                        bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            } else {
                float aspectRatio = (float) dm.heightPixels / dm.widthPixels;
                back = Bitmap.createBitmap(bitmap.getWidth(),
                        (int) (bitmap.getHeight() * aspectRatio), Bitmap.Config.ARGB_8888);
            }
            canvas = new Canvas(back);
            canvas.drawBitmap(bitmap, (back.getWidth() - bitmap.getWidth()) / 2f,
                    (back.getHeight() - bitmap.getHeight()) / 2f, paint);
            int color = ThemeHelper.setAlpha("6a", Theme.isDark() ?
                    Theme.CONTEXT_PRIMARY_LIGHT : Theme.CONTEXT_PRIMARY);
            paint.setColor(color);
            canvas.drawRect(0, 0, back.getWidth(), back.getHeight(), paint);
            BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(),
                    Helper.fastblur(back, 1, 32));
            mDrawerLayout.setBackground(bitmapDrawable);
            selectMusicView.updateBackground(new BitmapDrawable(getResources(), bitmapDrawable.getBitmap()));
        } else {
            mDrawerLayout.setBackground(new ColorDrawable(Theme.isDark() ?
                    Theme.CONTEXT_PRIMARY_LIGHT : Theme.CONTEXT_PRIMARY));
        }
    }

    public void updateTheme() {
        updateWindowBackground();
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
        updateDialogs();
        selectMusicView.updateTheme();
    }

    public void updateThemeContext() {
        updateWindowBackground();
        mBottomNavigationView.setBackgroundColor(Theme.isDark() ?
                Theme.CONTEXT_LIGHT : Theme.CONTEXT_PRIMARY);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Theme.isDark() ?
                    Theme.CONTEXT_LIGHT : Theme.CONTEXT_PRIMARY);
        }
        updateBottomNavigationView();
        updateDialogs();
        selectMusicView.updateThemeContext();
    }

    private void updateDialogs() {
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

        mMessageDialogBuilder =
                Theme.getMessageDialogBuilder()
                        .setPositiveText(getResources().getString(R.string.yes))
                        .setNegativeText(getResources().getString(R.string.cancel));
    }

    public void updateBottomNavigationView() {
        ColorStateList colorStateList = new ColorStateList(new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{-android.R.attr.state_checked}
        }, new int[]{Theme.isGrey(Theme.getColorSet().getPrimary()) ?
                Theme.CONTEXT_NEGATIVE_PRIMARY : (Theme.isDark() ?
                Theme.getColorSet().getPrimaryLight() : Theme.getColorSet().getPrimary()),
                Color.argb(Integer.parseInt("99", 16),
                        Color.red(Theme.CONTEXT_NEGATIVE_PRIMARY),
                        Color.green(Theme.CONTEXT_NEGATIVE_PRIMARY),
                        Color.blue(Theme.CONTEXT_NEGATIVE_PRIMARY))});
        mBottomNavigationView.setItemIconTintList(colorStateList);
        mBottomNavigationView.setItemTextColor(colorStateList);
    }
}