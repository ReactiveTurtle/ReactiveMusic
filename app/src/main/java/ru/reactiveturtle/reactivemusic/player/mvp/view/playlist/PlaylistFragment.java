package ru.reactiveturtle.reactivemusic.player.mvp.view.playlist;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import butterknife.Unbinder;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerModel;
import ru.reactiveturtle.reactivemusic.player.mvp.view.list.MusicListAdapter;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ThemeHelper;
import ru.reactiveturtle.reactivemusic.player.service.Bridges;
import ru.reactiveturtle.reactivemusic.player.service.MusicModel;
import ru.reactiveturtle.tools.reactiveuvm.Bridge;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;
import ru.reactiveturtle.tools.reactiveuvm.StateKeeper;
import ru.reactiveturtle.tools.reactiveuvm.fragment.ArchitectFragment;

public class PlaylistFragment extends ArchitectFragment {
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
        return inflater.inflate(R.layout.playlist_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        unbinder = ButterKnife.bind(this, view);
        mPlaylistAdapter = new PlaylistAdapter();
        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mMusicListAdapter = new MusicListAdapter(llm);
        mRecyclerView.setLayoutManager(llm);

        mPlaylistAdapter.setOnItemClickListener(new PlaylistAdapter.OnItemClickListener() {
            @Override
            public void onClick(String playlistName) {
                ReactiveArchitect.getStringBridge(Bridges.PlaylistOpen_To_ShowPlaylist).pull(playlistName);
            }

            @Override
            public void onLongClick(String playlistName) {
                PlayerModel.setPlaylistActionsPlaylistName(playlistName);
            }
        });

        mPreviousTrack.setOnClickListener((v) ->
                ReactiveArchitect.getBridge(Bridges.PreviousTrackClick_To_PlayTrack).pull());
        mPlayPause.setOnClickListener((v) -> PlayerModel.switchPlayPause());
        mNextTrack.setOnClickListener((v) ->
                ReactiveArchitect.getBridge(Bridges.NextTrackClick_To_PlayTrack).pull());

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

        updateThemeContext();
        updateTheme();
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
                                mMusicListAdapter.setSelectedTrackPath((String) value)),

                ReactiveArchitect.getStateKeeper(MusicModel.IS_TRACK_PLAY)
                        .subscribe(mPlayPauseCallback).call(),

                ReactiveArchitect.getStateKeeper(PlayerModel.PLAYLISTS)
                        .subscribe((view13, value) -> showPlaylists((List) value)).call(),

                ReactiveArchitect.getStateKeeper(PlayerModel.OPEN_PLAYLIST)
                        .subscribe((view13, value) -> showPlaylist((List) value)),
                ReactiveArchitect.getStateKeeper(Theme.COLOR_SET).subscribe((view, value) -> updateTheme()),
                ReactiveArchitect.getStateKeeper(Theme.IS_DARK).subscribe((view, value) -> updateThemeContext()).call()
        ));
    }

    @Override
    protected void onInitializeBridges(List<Bridge> container) {
        container.addAll(Arrays.asList(
                ReactiveArchitect.createStringBridge(Bridges.PlaylistCreated_To_UpdateFragment).connect(this::addPlaylist),
                ReactiveArchitect.createStringBridge(Bridges.FindTrack_To_PlaylistScrollToTrack)
                        .connect(param -> scrollToTrack(mMusicListAdapter.indexOf(param))),
                ReactiveArchitect.createStringBridge(Bridges.RenameDialog_To_RenamePlaylistList)
                        .connect(param -> renamePlaylist(PlayerModel.getPlaylistActionsPlaylistName(), param))
        ));
    }

    private StateKeeper.Binder.Callback mPlayPauseCallback = (view1, value) ->
            mPlayPause.setBackground(ThemeHelper.getDefaultRoundButtonDrawable(
                    ((boolean) value) ? R.drawable.ic_pause : R.drawable.ic_play));


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public void showCurrentTrackInfo(String artist, String album) {
        String info = (artist == null ? "" : (artist))
                + (album != null && artist != null ? " | " : "")
                + (album == null ? "" : album);
        mInfo.setText(info);
    }

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
        mPlayPause.setBackground(ThemeHelper.getDefaultRoundButtonDrawable(
                MusicModel.isTrackPlay() ? R.drawable.ic_pause : R.drawable.ic_play));
    }

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
            getView().findViewById(R.id.playerSmallDivider)
                    .setBackgroundColor(ThemeHelper.setAlpha("42", Theme.CONTEXT_LIGHT));
        }

        mPreviousTrack.setBackground(ThemeHelper.getDefaultButtonDrawable(R.drawable.ic_previous));
        mNextTrack.setBackground(ThemeHelper.getDefaultButtonDrawable(R.drawable.ic_next));
    }

    public void showPlaylists(List<String> playlists) {
        mRecyclerView.setAdapter(mPlaylistAdapter);
        mPlaylistEmpty.setText(R.string.playlistsNotFound);
        mPlaylistAdd.setImageResource(R.drawable.ic_add_playlist);

        mPlaylistAdapter.setPlaylists(playlists);
        mPlaylistEmpty.setVisibility(playlists.size() > 0 ? View.GONE : View.VISIBLE);
    }

    public void addPlaylist(String result) {
        mPlaylistAdapter.addPlaylist(result);
        mPlaylistEmpty.setVisibility(View.GONE);
    }

    public void renamePlaylist(String oldName, String newName) {
        mPlaylistAdapter.renamePlaylist(oldName, newName);
    }

    public void removePlaylist(String playlistName) {
        mPlaylistAdapter.removePlaylist(playlistName);
        if (mPlaylistAdapter.getItemCount() == 0) {
            mPlaylistEmpty.setVisibility(View.VISIBLE);
        }
    }

    public void showPlaylist(List<String> playlist) {
        mRecyclerView.setAdapter(mMusicListAdapter);
        mMusicListAdapter.addPaths(playlist);
        mPlaylistEmpty.setVisibility(playlist.size() > 0 ? View.GONE : View.VISIBLE);
        mPlaylistEmpty.setText(R.string.playlistEmpty);
        mPlaylistAdd.setImageResource(R.drawable.ic_add);
    }

    public void addTrack(String path) {
        mMusicListAdapter.addItem(path);
        mPlaylistEmpty.setVisibility(View.GONE);
    }

    public void removeTrack(String path) {
        mMusicListAdapter.removeItem(path);
        if (mMusicListAdapter.getItemCount() == 0) {
            mPlaylistEmpty.setVisibility(View.VISIBLE);
        }
    }

    public void scrollToTrack(int index) {
        mRecyclerView.smoothScrollToPosition(index);
    }

    public void closePlaylist() {
        mRecyclerView.setAdapter(mPlaylistAdapter);
        mPlaylistEmpty.setVisibility(mPlaylistAdapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
        mPlaylistEmpty.setText(R.string.playlistsNotFound);
        mPlaylistAdd.setImageResource(R.drawable.ic_add_playlist);
    }

    public void scrollToPlaylist(int index) {

    }

    @BindView(R.id.playlistAdd)
    protected FloatingActionButton mPlaylistAdd;

    @OnClick(R.id.playlistAdd)
    protected void addClick() {
        ReactiveArchitect.getBridge(Bridges.PlaylistAddClick_To_ShowCreateNameDialog).pull();
    }

    @OnClick(R.id.playerSmallClicker)
    protected void scrollToCurrentTrack() {
        ReactiveArchitect.getBridge(Bridges.PlaylistClick_To_FindTrack).pull();
    }

    @OnLongClick(R.id.playerSmallClicker)
    protected void findCurrentTrackInPrimaryPlaylist() {
        ReactiveArchitect.getBridge(Bridges.PlaylistClick_To_FindTrackInMainPlaylist).pull();
    }
}
