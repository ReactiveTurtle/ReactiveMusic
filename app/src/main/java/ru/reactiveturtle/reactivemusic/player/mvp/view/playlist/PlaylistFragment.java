package ru.reactiveturtle.reactivemusic.player.mvp.view.playlist;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.Unbinder;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.BaseMusicContract;
import ru.reactiveturtle.reactivemusic.player.GlobalModel;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.view.list.MusicListAdapter;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;

public class PlaylistFragment extends Fragment implements PlaylistContract.Fragment {
    private Unbinder unbinder;

    public static PlaylistFragment newInstance() {

        Bundle args = new Bundle();

        PlaylistFragment fragment = new PlaylistFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = GlobalModel.PLAYER_PRESENTER;
    }

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

    @BindView(R.id.playlistEmpty)
    protected TextView mPlaylistEmpty;

    @BindView(R.id.playlistsRecyclerView)
    protected RecyclerView mRecyclerView;
    private PlaylistAdapter mPlaylistAdapter;
    private MusicListAdapter mMusicListAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.playlist_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        mPlaylistAdapter = new PlaylistAdapter();
        mMusicListAdapter = new MusicListAdapter();
        mRecyclerView.setAdapter(mPlaylistAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mPresenter.onPlaylistAvailable(this);

        mPlaylistAdapter.setOnItemClickListener(new PlaylistAdapter.OnItemClickListener() {
            @Override
            public void onClick(String playlistName) {
                mPresenter.onOpenPlaylist(playlistName);
            }

            @Override
            public void onLongClick(String playlistName) {
                mPresenter.onShowPlaylistActions(playlistName);
            }
        });

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

        mMusicListAdapter.setOnItemClickListener(musicInfo ->
                mPresenter.onPlaylistMusicSelected(musicInfo));
        System.out.println(Integer.toHexString(mInfo.getTextColors().getDefaultColor()));
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
        unbinder.unbind();
    }

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
        mPlayPause.setBackground(Theme.getDefaultButtonDrawable(R.drawable.ic_pause));
    }

    @Override
    public void pauseMusic() {
        mPlayPause.setBackground(Theme.getDefaultButtonDrawable(R.drawable.ic_play));
    }

    @Override
    public void updateTheme() {
        Theme.updateFab(mPlaylistAdd);
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
        mPlaylistEmpty.setTextColor(Theme.CONTEXT_SECONDARY_TEXT);
        mTitle.setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);
        mInfo.setTextColor(Theme.CONTEXT_SECONDARY_TEXT);

        LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        if (llm != null && mRecyclerView.getAdapter() != null) {
            int first = llm.findFirstVisibleItemPosition();
            int last = llm.findLastVisibleItemPosition();
            for (int i = first; i < last + 1; i++) {
                if (mRecyclerView.getAdapter().equals(mMusicListAdapter)) {
                    mMusicListAdapter.notifyItemChanged(i);
                } else {
                    mPlaylistAdapter.notifyItemChanged(i);
                }
            }
        }
        if (getView() != null) {
            getView().findViewById(R.id.playerSmallDivider).setBackgroundColor(Theme.CONTEXT_LIGHT);
        }

        mPreviousTrack.setBackground(Theme.getDefaultButtonDrawable(R.drawable.ic_previous));
        mNextTrack.setBackground(Theme.getDefaultButtonDrawable(R.drawable.ic_next));
    }

    private PlaylistContract.Presenter mPresenter;

    @Override
    public void showPlaylists(List<String> playlists) {
        mPlaylistAdapter.setPlaylists(playlists);
        mPlaylistEmpty.setVisibility(playlists.size() > 0 ? View.GONE : View.VISIBLE);
        mPlaylistEmpty.setText(R.string.playlistsNotFound);
    }

    @Override
    public void addPlaylist(String result) {
        mPlaylistAdapter.addPlaylist(result);
        mPlaylistEmpty.setVisibility(View.GONE);
    }

    @Override
    public void renamePlaylist(String oldName, String newName) {
        mPlaylistAdapter.renamePlaylist(oldName, newName);
    }

    @Override
    public void removePlaylist(String playlistName) {
        mPlaylistAdapter.removePlaylist(playlistName);
        if (mPlaylistAdapter.getItemCount() == 0) {
            mPlaylistEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void showPlaylist(String name, List<String> playlist) {
        mRecyclerView.setAdapter(mMusicListAdapter);
        mMusicListAdapter.addPaths(playlist);
        mPlaylistEmpty.setVisibility(playlist.size() > 0 ? View.GONE : View.VISIBLE);
        mPlaylistEmpty.setText(R.string.playlistEmpty);
        mPlaylistAdd.setImageResource(R.drawable.ic_add);
    }

    @Override
    public void addTrack(String path) {
        mMusicListAdapter.addItem(path);
        mPlaylistEmpty.setVisibility(View.GONE);
    }

    @Override
    public void removeTrack(String path) {
        mMusicListAdapter.removeItem(path);
        if (mMusicListAdapter.getItemCount() == 0) {
            mPlaylistEmpty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void scrollToTrack(int index) {
        mRecyclerView.smoothScrollToPosition(index);
    }

    @Override
    public void closePlaylist() {
        mRecyclerView.setAdapter(mPlaylistAdapter);
        mPlaylistEmpty.setVisibility(mPlaylistAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
        mPlaylistEmpty.setText(R.string.playlistsNotFound);
        mPlaylistAdd.setImageResource(R.drawable.ic_add_playlist);
    }

    @Override
    public void scrollToPlaylist(int index) {

    }

    @BindView(R.id.playlistAdd)
    protected FloatingActionButton mPlaylistAdd;

    @OnClick(R.id.playlistAdd)
    protected void addClick() {
        Objects.requireNonNull(mPresenter);
        mPresenter.onPlaylistAdd();
    }

    @OnClick(R.id.playerSmallClicker)
    protected void scrollToCurrentTrack() {
        mPresenter.onFindTrack(mMusicListAdapter.getSelectedTrackPath());
    }

    @OnLongClick(R.id.playerSmallClicker)
    protected void findCurrentTrackInPrimaryPlaylist() {
        mPresenter.onFindTrackInPlaylist(null, mMusicListAdapter.getSelectedTrackPath());
    }
}
