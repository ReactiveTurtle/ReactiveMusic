package ru.reactiveturtle.reactivemusic.player.mvp.view.settings;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.GlobalModel;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ColorPalette;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.BaseMusicContract;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ThemesAdapter;
import ru.reactiveturtle.reactivemusic.player.service.MusicModel;

public class SettingsFragment extends androidx.fragment.app.Fragment implements SettingsContract.Fragment {
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.settings_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        initSettings();

        mThemeContextSwitch = view.findViewById(R.id.themeContextSwitch);
        mThemeContextSwitch.setOnClickListener(view1 -> {
            Theme.updateContext(!Theme.IS_DARK);
            mPresenter.onUpdateContextTheme();
        });
        mThemesRecyclerView = mThemesRoot.findViewById(R.id.themesRecyclerView);
        mThemesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mThemesAdapter = new ThemesAdapter();
        mThemesRecyclerView.setAdapter(mThemesAdapter);
        mThemeName.setText(ColorPalette.getNames()[mThemeBrightness.getProgress()]);
        mThemesAdapter.setColorSets(Theme.getColors(mThemeBrightness.getProgress()));
        mThemesAdapter.setOnItemClickListener(colorSet -> {
            Theme.update(colorSet, GlobalModel.getTrackProgress());
            mPresenter.onUpdateTheme();
        });
        mPresenter.onSettingsFragmentAvailable(this);
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
        updateThemeContext();
        updateTheme();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initAd();
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
            mAdView.setAdListener(new AdListener(){
                private boolean isFirstLoad = true;

                @Override
                public void onAdFailedToLoad(LoadAdError loadAdError) {
                    super.onAdFailedToLoad(loadAdError);
                    System.out.println("Ad load failed");
                    loadAd();
                }

                @Override
                public void onAdLoaded() {
                    System.out.println("Ad loaded");
                    super.onAdLoaded();
                    if (isFirstLoad) {
                        ConstraintSet constraintSet = new ConstraintSet();
                        constraintSet.clone(contentRoot);
                        constraintSet.connect(R.id.settingsThemesFragment, ConstraintSet.TOP,
                                mAdView.getId(), ConstraintSet.BOTTOM);
                        constraintSet.applyTo(contentRoot);
                        isFirstLoad = false;
                    }
                }

                @Override
                public void onAdClosed() {
                    super.onAdClosed();
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

    private SettingsContract.Presenter mPresenter;

    @Override
    public void setPresenter(@NonNull BaseMusicContract.FragmentPresenter presenter) {
        mPresenter = (SettingsContract.Presenter) presenter;
    }

    @Override
    public void updateTheme() {
        Theme.updateSeekBar(mThemeBrightness);
        Theme.updateFab(mThemeContextSwitch);
    }

    @Override
    public void updateThemeContext() {
        mThemeName.setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);
        mThemeContextSwitch.setImageResource(Theme.IS_DARK ?
                R.drawable.ic_sun : R.drawable.ic_moon);
        Theme.updateSeekBar(mThemeBrightness);
        Theme.updateFab(mThemeContextSwitch);
        mThemesAdapter.notifyDataSetChanged();
    }

    private void initSettings() {
        mSettingsMenu.setVisibility(View.GONE);
        mThemesRoot.setVisibility(View.VISIBLE);

        mSettingsAdapter = new SettingsAdapter();
        mSettingsMenu.setLayoutManager(new LinearLayoutManager(getContext()));
        mSettingsMenu.setAdapter(mSettingsAdapter);

        mSettingsAdapter.setOnItemClickListener(index -> {
            mSettingsMenu.setVisibility(View.GONE);
            switch (index) {
                case 0:
                    mThemesRoot.setVisibility(View.VISIBLE);
                    break;
                case 1:
                    break;
            }
        });
        if (getActivity() != null) {
            SettingsItem themeItem = new SettingsItem(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.ic_style, getActivity().getTheme()),
                    getResources().getString(R.string.themes));
            themeItem.getIcon().setColorFilter(new LightingColorFilter(Color.BLACK,
                    ResourcesCompat.getColor(getResources(), android.R.color.holo_red_light, getActivity().getTheme())));
            mSettingsAdapter.addItem(themeItem);

            SettingsItem equalizerItem = new SettingsItem(ResourcesCompat.getDrawable(getResources(),
                    R.drawable.ic_equalizer, getActivity().getTheme()),
                    getResources().getString(R.string.equalizer));
            equalizerItem.getIcon().setColorFilter(new LightingColorFilter(Color.BLACK,
                    ResourcesCompat.getColor(getResources(), android.R.color.holo_orange_light, getActivity().getTheme())));
            mSettingsAdapter.addItem(equalizerItem);
        }
    }

    @Override
    public void showSettings() {
    }
}
