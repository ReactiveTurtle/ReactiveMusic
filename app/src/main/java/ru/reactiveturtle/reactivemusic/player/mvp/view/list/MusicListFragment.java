package ru.reactiveturtle.reactivemusic.player.mvp.view.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.Unbinder;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.GlobalModel;
import ru.reactiveturtle.reactivemusic.player.Loaders;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.PlayerPresenter;
import ru.reactiveturtle.reactivemusic.player.mvp.view.selector.SelectMusicListAdapter;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ThemeHelper;

public class MusicListFragment extends Fragment implements MusicListContract.Fragment {
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
        mPresenter = GlobalModel.PLAYER_PRESENTER;
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
        View view = inflater.inflate(R.layout.player_music_list_fragment, container);
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
                mMusicListAdapter.setOnItemClickListener(musicInfo ->
                        mPresenter.onMusicChanged(musicInfo, null));

                mPreviousTrack.setOnClickListener((v) -> {
                    Objects.requireNonNull(mPresenter);
                    mPresenter.onPreviousTrack();
                });

                mPlayPause.setOnClickListener((v) -> {
                    Objects.requireNonNull(mPresenter);
                    mPresenter.onPlayPause();
                });

                mNextTrack.setOnClickListener((v) -> {
                    Objects.requireNonNull(mPresenter);
                    mPresenter.onNextTrack();
                });
                if (mPresenter != null) {
                    mPresenter.onMusicListAvailable(this);
                }
            });
            mTracksPathsLoader.forceLoad();
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateThemeContext();
        updateTheme();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mTracksPathsLoader != null) {
            mTracksPathsLoader.cancelLoad();
            mTracksPathsLoader = null;
        }
        unbinder.unbind();
        mPresenter = null;
    }

    private MusicListContract.Presenter mPresenter;

    @Override
    public void showCurrentTrack(MusicInfo musicInfo) {
        mTitle.setText(musicInfo.getTitle());
        String info = musicInfo.getArtist() + " | " + musicInfo.getAlbum();
        mInfo.setText(info);
        mMusicListAdapter.setSelectedTrackPath(musicInfo.getPath());
    }

    @Override
    public void clearCurrentMusic() {

    }

    @Override
    public void startMusic() {
        mPlayPause.setBackground(ThemeHelper.getDefaultButtonDrawable(R.drawable.ic_pause));
    }

    @Override
    public void pauseMusic() {
        mPlayPause.setBackground(ThemeHelper.getDefaultButtonDrawable(R.drawable.ic_play));
    }

    @Override
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
    }

    @Override
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
            getView().findViewById(R.id.playerSmallDivider).setBackgroundColor(Theme.CONTEXT_LIGHT);
        }

        mPreviousTrack.setBackground(ThemeHelper.getDefaultButtonDrawable(R.drawable.ic_previous));
        mNextTrack.setBackground(ThemeHelper.getDefaultButtonDrawable(R.drawable.ic_next));
    }

    @OnClick(R.id.playerSmallClicker)
    protected void scrollToCurrentTrack() {
        mPresenter.onFindTrack(mMusicListAdapter.getSelectedTrackPath());
    }

    @OnLongClick(R.id.playerSmallClicker)
    protected void findCurrentTrackInPrimaryPlaylist() {
        mPresenter.onFindTrackInPlaylist(null, mMusicListAdapter.getSelectedTrackPath());
    }

    @Override
    public void scrollToPosition(int index) {
        mRecyclerView.smoothScrollToPosition(index);
    }

    @Override
    public void bindLists(SelectMusicListAdapter adapter) {
        mMusicListAdapter.bind(adapter);
    }
}
