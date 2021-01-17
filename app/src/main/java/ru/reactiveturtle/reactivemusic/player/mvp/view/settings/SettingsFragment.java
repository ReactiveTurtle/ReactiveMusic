package ru.reactiveturtle.reactivemusic.player.mvp.view.settings;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.GlobalModel;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ColorPalette;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ThemesAdapter;
import ru.reactiveturtle.reactivemusic.player.service.Bridges;
import ru.reactiveturtle.tools.reactiveuvm.Bridge;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;
import ru.reactiveturtle.tools.reactiveuvm.StateKeeper;
import ru.reactiveturtle.tools.reactiveuvm.fragment.ArchitectFragment;

public class SettingsFragment extends ArchitectFragment {
    private Unbinder unbinder;
    @BindView(R.id.settingsMenu)
    protected RecyclerView mSettingsMenu;
    private SettingsAdapter mSettingsAdapter;

    @BindView(R.id.settingsThemesFragment)
    protected ConstraintLayout mThemesRoot;
    private RecyclerView mThemesRecyclerView;
    private ThemesAdapter mThemesAdapter;
    @BindView(R.id.themeName)
    protected TextView mThemeName;
    @BindView(R.id.themeBrightess)
    protected SeekBar mThemeBrightness;
    @BindView(R.id.themeContextSwitch)
    protected FloatingActionButton mThemeContextSwitch;

    private AdView mAdView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.settings_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);
        initSettings();
        initTheme();
        initAd();
    }

    @Override
    protected void onInitializeBinders(List<StateKeeper.Binder> container) {
        container.addAll(Arrays.asList(
                ReactiveArchitect.getStateKeeper(Theme.COLOR_SET).subscribe((view, value) -> updateTheme()).call(),
                ReactiveArchitect.getStateKeeper(Theme.IS_DARK).subscribe((view, value) -> updateThemeContext()).call()
        ));
    }

    @Override
    protected void onInitializeBridges(List<Bridge> container) {

    }

    private void initAd() {
        if (getContext() != null && getView() != null) {
            mAdView = new AdView(getContext());
            mAdView.setId(android.R.id.widget_frame);
            ConstraintLayout contentRoot = getView().findViewById(R.id.settingsRoot);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(contentRoot);
            constraintSet.connect(mAdView.getId(), ConstraintSet.TOP, contentRoot.getId(), ConstraintSet.TOP);
            constraintSet.connect(mAdView.getId(), ConstraintSet.START, contentRoot.getId(), ConstraintSet.START);
            constraintSet.connect(mAdView.getId(), ConstraintSet.END, contentRoot.getId(), ConstraintSet.END);
            constraintSet.applyTo(contentRoot);
            contentRoot.addView(mAdView);

            AdSize adSize = getAdSize();
            mAdView.setAdUnitId(getString(R.string.settings_banner_unit_id));
            mAdView.getLayoutParams().width = adSize.getWidthInPixels(getContext());
            mAdView.getLayoutParams().height = adSize.getHeightInPixels(getContext());
            mAdView.setAdSize(adSize);
            mAdView.setAdListener(new AdListener() {
                private boolean isFirstLoad = true;

                @Override
                public void onAdLoaded() {
                    System.out.println("Ad loaded");
                    super.onAdLoaded();
                    if (isFirstLoad) {
                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(contentRoot);
                        constraintSet.connect(R.id.settingsSections, ConstraintSet.TOP,
                                mAdView.getId(), ConstraintSet.BOTTOM);
                        constraintSet.applyTo(contentRoot);
                        isFirstLoad = false;
                    }
                }
            });
            loadAd();
            System.out.println("Ad initialized");
        }
    }

    private void loadAd() {
        AdRequest adRequest =
                new AdRequest.Builder()
                        .addTestDevice("FFA965BABCDD7ADE92691337BB3BA99D")
                        .addTestDevice("15CA2294E6C8A0A64A5F0E7D3A8ECA8C") // MEMU Emulator
                        .build();
        mAdView.loadAd(adRequest);
    }

    private AdSize getAdSize() {
        DisplayMetrics outMetrics = getResources().getDisplayMetrics();

        float widthPixels = outMetrics.widthPixels;
        float density = outMetrics.density;

        int adWidth = (int) (widthPixels / density);

        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(getContext(), adWidth);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void updateTheme() {
        Theme.updateSeekBar(mThemeBrightness);
        Theme.updateFab(mThemeContextSwitch);
    }

    public void updateThemeContext() {
        mThemeName.setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);
        mThemeContextSwitch.setImageResource(Theme.isDark() ?
                R.drawable.ic_sun : R.drawable.ic_moon);
        Theme.updateSeekBar(mThemeBrightness);
        Theme.updateFab(mThemeContextSwitch);
        mThemesAdapter.notifyDataSetChanged();
        mSettingsAdapter.notifyDataSetChanged();
    }

    private void initSettings() {
        mSettingsAdapter = new SettingsAdapter();
        mSettingsMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        mSettingsMenu.setAdapter(mSettingsAdapter);
        mSettingsMenu.setVisibility(View.GONE);

        mSettingsAdapter.setOnItemClickListener(index -> {
            mSettingsMenu.setVisibility(View.GONE);
            switch (index) {
                case 0:
                    break;
                case 1:
                    mThemesRoot.setVisibility(View.VISIBLE);
                    break;
            }
            ReactiveArchitect.getIntBridge(Bridges.SettingsClick_To_UpdateToolbarArrow).pull(index);
        });
        if (getActivity() != null) {
            SettingsItem equalizerItem = new SettingsItem(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.ic_equalizer, getActivity().getTheme()),
                    getResources().getString(R.string.equalizer));
            equalizerItem.getIcon().setColorFilter(new LightingColorFilter(Color.BLACK,
                    ResourcesCompat.getColor(getResources(), android.R.color.holo_orange_light, getActivity().getTheme())));
            mSettingsAdapter.addItem(equalizerItem);

            SettingsItem themeItem = new SettingsItem(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.ic_style, getActivity().getTheme()),
                    getResources().getString(R.string.themes));
            themeItem.getIcon().setColorFilter(new LightingColorFilter(Color.BLACK,
                    ResourcesCompat.getColor(getResources(), android.R.color.holo_red_light, getActivity().getTheme())));
            mSettingsAdapter.addItem(themeItem);
        }
    }

    private void initTheme() {
        mThemesRoot.setVisibility(View.VISIBLE);
        mThemeContextSwitch.setOnClickListener(view1 -> {
            Theme.switchThemeContext();
        });
        mThemesRecyclerView = mThemesRoot.findViewById(R.id.themesRecyclerView);
        mThemesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mThemesAdapter = new ThemesAdapter();
        mThemesRecyclerView.setAdapter(mThemesAdapter);
        mThemeName.setText(ColorPalette.getNames()[mThemeBrightness.getProgress()]);
        mThemesAdapter.setColorSets(Theme.getColors(mThemeBrightness.getProgress()));
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mThemesAdapter.notifyDataSetChanged();
                    }
                });
            }
        }, 3000);

        mThemesAdapter.setOnItemClickListener(Theme::setColorSet);
        mThemeBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                mThemesAdapter.setColorSets(Theme.getColors(i));
                mThemeName.setText(ColorPalette.getNames()[i]);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public boolean isPressBack() {
        return mSettingsMenu.getVisibility() == View.GONE;
    }

    public boolean pressBack() {
        if (true) {
            return false;
        }
        mThemesRoot.setVisibility(View.GONE);
        if (mSettingsMenu.getVisibility() == View.GONE) {
            mSettingsMenu.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    private SettingsAdapter.OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(SettingsAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }
}
