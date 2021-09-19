package ru.reactiveturtle.reactivemusic.view;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.reactiveturtle.reactivemusic.Permissions;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.musicservice.MusicService;
import ru.reactiveturtle.reactivemusic.player.loaders.MusicAlbumCoverLoader;
import ru.reactiveturtle.reactivemusic.player.MusicMetadata;
import ru.reactiveturtle.reactivemusic.musicservice.MusicServiceConnection;
import ru.reactiveturtle.reactivemusic.player.MusicPlayerProvider;
import ru.reactiveturtle.reactivemusic.player.shared.MusicAlbumCoverData;
import ru.reactiveturtle.reactivemusic.theme.Theme;

public class PlayerActivity extends AppCompatActivity {
    @BindView(R.id.toolbar)
    protected Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    protected DrawerLayout drawerLayout;

    @BindView(R.id.playerViewPager)
    protected ViewPager2 playerViewPager;
    private PlayerPagerAdapter playerPagerAdapter;

    @BindView(R.id.playerBottomNavigationView)
    protected BottomNavigationView bottomNavigationView;

    private MusicServiceConnection musicServiceConnection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //enableScreenshotMode();
        MusicMetadata.initDefault(getResources());
        MobileAds.initialize(getApplicationContext(), initializationStatus -> {

        });
        musicServiceConnection = new MusicServiceConnection(this);
        musicServiceConnection.setConnectionCallback(new MusicServiceConnection.ConnectionCallback() {
            private Theme theme;
            @Override
            public void onConnected(MusicService.Binder binder) {
                initView(binder);
                theme = binder.getTheme();
                binder.getPlayer().addMusicPlayerListener(musicPlayerListener);
            }

            @Override
            public void onDisconnected() {
                theme = null;
            }

            private MusicPlayerProvider.MusicPlayerListener musicPlayerListener = new MusicPlayerProvider.MusicPlayerListener() {
                @Override
                public void onPrepared(int duration) {

                }

                @Override
                public void onPlay() {

                }

                @Override
                public void onPause() {

                }

                @Override
                public void onLoopingChanged(boolean isLooping) {

                }

                @Override
                public void onPlayRandomTrackChanged(boolean isPlayRandomTrack) {

                }

                @Override
                public void onProgressChanged(int progress) {

                }

                @Override
                public void onMusicMetadataLoad(@NonNull MusicMetadata musicMetadata) {

                }

                @Override
                public void onMusicCoverDataLoad(@NonNull MusicAlbumCoverData musicAlbumCoverData) {
                    super.onMusicCoverDataLoad(musicAlbumCoverData);
                    if (!musicAlbumCoverData.isOwnCover()) {
                        playerViewPager.setBackground(theme.getDefaultBackground());
                    } else {
                        playerViewPager.setBackground(MusicAlbumCoverLoader.loadBackgroundAlbumCover(musicAlbumCoverData.getCover(), theme, getResources()));
                    }
                }
            };
        });
    }

    private void enableScreenshotMode() {
        int height = getResources().getDisplayMetrics().heightPixels;
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        getWindow().getAttributes().height = (int) (getWindow().getAttributes().width * (16 / 9f) - statusBarHeight);
        getWindow().getAttributes().y = getWindow().getAttributes().height - height;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startAndConnectWithService();
    }

    @Override
    protected void onPause() {
        if (Permissions.hasExternalStorage(this)) {
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void startAndConnectWithService() {
        if (!Permissions.hasExternalStorage(this)) {
            return;
        }
        if (!MusicService.isRunning()) {
            startService(new Intent(this, MusicService.class));
        }
        musicServiceConnection.bind();
    }

    private void initView(MusicService.Binder binder) {
        setContentView(R.layout.player_activity);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

        initPlayerViewPager(binder);
    }

    private void initPlayerViewPager(MusicService.Binder binder) {
        playerViewPager.setOffscreenPageLimit(4);
        playerPagerAdapter = new PlayerPagerAdapter(this, binder);
        playerViewPager.setAdapter(playerPagerAdapter);
        drawerLayout.setVisibility(View.VISIBLE);
        playerViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                toolbar.setTitle(playerPagerAdapter.getTitle(position));
                switch (position) {
                    case 0:
                        bottomNavigationView.setSelectedItemId(R.id.playerBottomPlayerItem);
                        break;
                    case 1:
                        bottomNavigationView.setSelectedItemId(R.id.playerBottomListPlaylists);
                        break;
                    case 2:
                        bottomNavigationView.setSelectedItemId(R.id.playerBottomListItem);
                        break;
                    case 3:
                        bottomNavigationView.setSelectedItemId(R.id.playerBottomSettingsItem);
                        break;
                }
            }
        });
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.playerBottomPlayerItem:
                    playerViewPager.setCurrentItem(0);
                    break;
                case R.id.playerBottomListPlaylists:
                    playerViewPager.setCurrentItem(1);
                    break;
                case R.id.playerBottomListItem:
                    playerViewPager.setCurrentItem(2);
                    break;
                case R.id.playerBottomSettingsItem:
                    playerViewPager.setCurrentItem(3);
                    break;
            }
            return true;
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0) {
            switch (requestCode) {
                case 0:
                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        startAndConnectWithService();
                    }
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }
    }
}