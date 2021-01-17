package ru.reactiveturtle.reactivemusic.player.mvp.view.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.Unbinder;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.Loaders;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerModel;
import ru.reactiveturtle.reactivemusic.player.mvp.view.selector.SelectMusicListAdapter;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ThemeHelper;
import ru.reactiveturtle.reactivemusic.player.service.Bridges;
import ru.reactiveturtle.reactivemusic.player.service.MusicModel;
import ru.reactiveturtle.tools.reactiveuvm.Bridge;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;
import ru.reactiveturtle.tools.reactiveuvm.StateKeeper;
import ru.reactiveturtle.tools.reactiveuvm.fragment.ArchitectFragment;

public class MusicListFragment extends ArchitectFragment {
    private Unbinder unbinder;

    public static MusicListFragment newInstance() {

        Bundle args = new Bundle();

        MusicListFragment fragment = new MusicListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @BindView(R.id.player_music_list_recycler_view)
    protected RecyclerView mRecyclerView;
    private MusicListAdapter mMusicListAdapter;

    @BindView(R.id.musicListEmpty)
    protected TextView mEmpty;

    @BindView(R.id.playerSmallPreviousTrack)
    protected Button mPreviousTrack;
    @BindView(R.id.playerSmallPlayPause)
    protected Button mPlayPause;
    @BindView(R.id.playerSmallNextTrack)
    protected Button mNextTrack;

    @BindView(R.id.playerSmallTitle)
    protected TextView mTitle;
    @BindView(R.id.playerSmallInfo)
    protected TextView mInfo;

    private Loaders.TracksPathsLoader mTracksPathsLoader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.player_music_list_fragment, container);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, view);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(llm);
        mMusicListAdapter = new MusicListAdapter(llm);
        mRecyclerView.setAdapter(mMusicListAdapter);
        if (getContext() != null) {
            mTracksPathsLoader = new Loaders.TracksPathsLoader(getContext());
            mTracksPathsLoader.registerListener(0, (loader, tracks) -> {
                mTracksPathsLoader = null;
                assert tracks != null;
                if (tracks.size() <= 0) {
                    mEmpty.setVisibility(View.VISIBLE);
                }
                mMusicListAdapter.addPaths(tracks);
                mMusicListAdapter.setOnItemClickListener(new MusicListAdapter.OnItemClickListener() {
                    @Override
                    public void onClick(MusicInfo musicInfo) {
                        MusicModel.setCurrentTrackPath(musicInfo.getPath());
                    }

                    @Override
                    public void onLongClick(String trackPath) {
                        ReactiveArchitect.getStringBridge(Bridges.PlaylistFragment_To_ShowTrackActions)
                                .pull(trackPath);
                    }
                });

                mPreviousTrack.setOnClickListener((v) ->
                        ReactiveArchitect.getBridge(Bridges.PreviousTrackClick_To_PlayTrack).pull());
                mPlayPause.setOnClickListener((v) -> PlayerModel.switchPlayPause());
                mNextTrack.setOnClickListener((v) ->
                        ReactiveArchitect.getBridge(Bridges.NextTrackClick_To_PlayTrack).pull());
            });
            mTracksPathsLoader.forceLoad();
        }
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void onInitializeBinders(List<StateKeeper.Binder> container) {
        StateKeeper.Binder.Callback artistAlbumCallback = (view1, value) ->
                showCurrentTrackInfo(MusicModel.getCurrentTrackArtist(), MusicModel.getCurrentTrackAlbum());
        container.addAll(Arrays.asList(
                ReactiveArchitect.getStateKeeper(MusicModel.CURRENT_TRACK_NAME)
                        .subscribe(mTitle, "setText", CharSequence.class).call(),
                ReactiveArchitect.getStateKeeper(MusicModel.CURRENT_TRACK_ARTIST)
                        .subscribe(mInfo, "setText", CharSequence.class, artistAlbumCallback).call(),
                ReactiveArchitect.getStateKeeper(MusicModel.CURRENT_TRACK_ALBUM)
                        .subscribe(mInfo, "setText", CharSequence.class, artistAlbumCallback).call(),
                ReactiveArchitect.getStateKeeper(MusicModel.CURRENT_TRACK_PATH)
                        .subscribe((view12, value) ->
                                mMusicListAdapter.setSelectedTrackPath((String) value)).call(),
                ReactiveArchitect.getStateKeeper(MusicModel.IS_TRACK_PLAY)
                        .subscribe(mPlayPauseCallback).call(),
                ReactiveArchitect.getStateKeeper(Theme.COLOR_SET).subscribe((view, value) -> updateTheme()),
                ReactiveArchitect.getStateKeeper(Theme.IS_DARK).subscribe((view, value) -> updateThemeContext()).call()
        ));
    }

    @Override
    protected void onInitializeBridges(List<Bridge> container) {
        container.addAll(Arrays.asList(
                ReactiveArchitect.createStringBridge(Bridges.FindTrack_To_MusicListScrollToTrack)
                        .connect(param -> scrollToTrack(mMusicListAdapter.indexOf(param)))
        ));
    }

    private StateKeeper.Binder.Callback mPlayPauseCallback = (view1, value) ->
            mPlayPause.setBackground(ThemeHelper.getDefaultRoundButtonDrawable(
                    ((boolean) value) ? R.drawable.ic_pause : R.drawable.ic_play));

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mTracksPathsLoader != null) {
            mTracksPathsLoader.cancelLoad();
            mTracksPathsLoader = null;
        }
        unbinder.unbind();
    }

    public void showCurrentTrackInfo(String artist, String album) {
        String info = (artist == null ? "" : (artist))
                + (album != null && artist != null ? " | " : "")
                + (album == null ? "" : album);
        mInfo.setText(info);
    }

    public void updateTheme() {
        LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if (llm != null && mRecyclerView.getAdapter() != null
                && mRecyclerView.getAdapter().equals(mMusicListAdapter)) {
            int first = llm.findFirstVisibleItemPosition();
            int last = llm.findLastVisibleItemPosition();
            int index = mMusicListAdapter.indexOf(mMusicListAdapter.getSelectedTrackPath());
            if (index >= first && index <= last) {
                mMusicListAdapter.notifyItemChanged(index);
            }
        }
        mPlayPause.setBackground(ThemeHelper.getDefaultRoundButtonDrawable(
                MusicModel.isTrackPlay() ? R.drawable.ic_pause : R.drawable.ic_play));
    }

    public void updateThemeContext() {
        mEmpty.setTextColor(Theme.CONTEXT_SECONDARY_TEXT);
        mTitle.setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);
        mInfo.setTextColor(Theme.CONTEXT_SECONDARY_TEXT);

        LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if (llm != null && mRecyclerView.getAdapter() != null) {
            int first = llm.findFirstVisibleItemPosition();
            int last = llm.findLastVisibleItemPosition();
            for (int i = first; i < last + 2; i++) {
                if (i < mMusicListAdapter.getItemCount()) {
                    mMusicListAdapter.notifyItemChanged(i);
                }
            }
        }
        if (getView() != null) {
            getView().findViewById(R.id.playerSmallDivider)
                    .setBackgroundColor(ThemeHelper.setAlpha("42", Theme.CONTEXT_LIGHT));
        }

        mPreviousTrack.setBackground(ThemeHelper.getDefaultButtonDrawable(R.drawable.ic_previous));
        mNextTrack.setBackground(ThemeHelper.getDefaultButtonDrawable(R.drawable.ic_next));
    }

    @OnClick(R.id.playerSmallClicker)
    protected void scrollToCurrentTrack() {
        ReactiveArchitect.getBridge(Bridges.PlaylistClick_To_FindTrack).pull();
    }

    @OnLongClick(R.id.playerSmallClicker)
    protected void findCurrentTrackInPrimaryPlaylist() {
        ReactiveArchitect.getBridge(Bridges.PlaylistClick_To_FindTrackInMainPlaylist).pull();
    }

    public void scrollToTrack(int index) {
        mRecyclerView.smoothScrollToPosition(index);
    }

    public void bindLists(SelectMusicListAdapter adapter) {
        mMusicListAdapter.bind(adapter);
    }

    public void removeTrack(String path) {
        mMusicListAdapter.removeItem(path);
    }
}
