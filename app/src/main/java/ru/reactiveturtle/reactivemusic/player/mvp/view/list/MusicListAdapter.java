package ru.reactiveturtle.reactivemusic.player.mvp.view.list;

import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.mvp.view.music.MusicFragment;
import ru.reactiveturtle.reactivemusic.player.mvp.view.selector.SelectMusicListAdapter;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.Loaders;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.tools.BaseAsyncTask;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicViewHolder> {
    protected List<MusicInfo> mMusicList = new ArrayList<>();

    private LinearLayoutManager mLinearLayoutManager;

    public MusicListAdapter(@NonNull LinearLayoutManager linearLayoutManager) {
        mLinearLayoutManager = linearLayoutManager;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MusicViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_music_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        MusicInfo track = mMusicList.get(position);
        if (track.getAlbumImage() == null) {
            holder.showGray();
            holder.loadAlbumCover(track, position);
        } else {
            holder.showCover(track.getAlbumImage());
        }
        if (track.getDuration() >= 0) {
            holder.showTransparent();
            holder.showInfo(track);
        } else {
            holder.showGray();
            Loaders.MusicInfoLoader musicInfoLoader = new Loaders.MusicInfoLoader(
                    holder.itemView.getContext(), track.getPath());
            musicInfoLoader.setFinishCallback(musicInfo -> {
                if (musicInfo == null) {
                    track.setDuration(-1);
                } else {
                    track.setAlbum(musicInfo.getAlbum());
                    track.setArtist(musicInfo.getArtist());
                    track.setTitle(musicInfo.getTitle());
                    track.setDuration(musicInfo.getDuration());
                    if (track.getAlbumImage() != null) {
                        notifyItemChanged(position);
                    }
                }
            });
            musicInfoLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public int getItemCount() {
        return mMusicList.size();
    }

    private OnItemClickListener onItemClickListener;

    public void addPaths(List<String> allTracks) {
        mMusicList.clear();
        List<MusicInfo> tracks = new ArrayList<>();
        for (int i = 0; i < allTracks.size(); i++) {
            MusicInfo musicInfo = MusicInfo.getEmpty();
            musicInfo.setPath(allTracks.get(i));
            tracks.add(musicInfo);
        }
        mMusicList.addAll(tracks);
        notifyDataSetChanged();
    }

    public void addItem(String path) {
        MusicInfo musicInfo = MusicInfo.getEmpty();
        musicInfo.setPath(path);
        mMusicList.add(musicInfo);
        notifyItemInserted(getItemCount() - 1);
    }

    public void removeItem(String path) {
        int index = indexOf(path);
        mMusicList.remove(index);
        notifyItemRemoved(index);
    }

    private String mPath;

    public void setSelectedTrackPath(String path) {
        int prevIndex = -1;
        if (mPath != null) {
            prevIndex = indexOf(mPath);
        }
        mPath = path;
        if (prevIndex > -1) {
            notifyItemChanged(prevIndex);
        }
        if (mPath != null) {
            int index = indexOf(mPath);
            if (index > -1) {
                notifyItemChanged(index);
            }
        }
    }

    public String getSelectedTrackPath() {
        return mPath;
    }

    public int indexOf(@NonNull String path) {
        int index = -1;
        for (int i = 0; i < mMusicList.size(); i++) {
            if (mMusicList.get(i).getPath().equals(path)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public List<MusicInfo> getTracks() {
        return new ArrayList<>(mMusicList);
    }

    public void bind(SelectMusicListAdapter adapter) {
        adapter.mMusicList = mMusicList;
    }

    protected class MusicViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.player_music_list_item_album)
        protected View albumImage;

        @BindView(R.id.player_music_list_item_title)
        protected TextView title;

        @BindView(R.id.player_music_list_item_info)
        protected TextView info;

        @BindView(R.id.playerMusicListBottomDivider)
        protected View bottomDivider;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.player_music_list_item_clicker)
        protected void itemClick() {
            Objects.requireNonNull(onItemClickListener);
            onItemClickListener.onClick(mMusicList.get(getLayoutPosition()));
        }

        private void showInfo(MusicInfo track) {
            Drawable background = track.getPath().equals(mPath) ?
                    Theme.getProgressDrawable() :
                    new ColorDrawable(Color.TRANSPARENT);
            itemView.setBackground(background);

            if (track.getDuration() < 0) {
                title.setText(track.getPath());
            } else {
                String titleString = track.getTitle();
                if (!track.getArtist().equals(MusicInfo.UNKNOWN_ARTIST)) {
                    titleString += " - " + track.getArtist();
                }
                title.setText(titleString);
                String infoText = track.getAlbum() + " • " + MusicFragment.getTime(track.getDuration());
                info.setText(infoText);

                title.setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);
                info.setTextColor(Theme.CONTEXT_SECONDARY_TEXT);
            }
            bottomDivider.setBackgroundColor(Theme.CONTEXT_LIGHT);
        }

        public void showCover(BitmapDrawable cover) {
            int color = Color.BLACK;
            albumImage.setBackground(cover == null ? new ColorDrawable(color) : cover);
        }

        private void showGray() {
            int color = Theme.IS_DARK ? Theme.CONTEXT_LIGHT : Theme.CONTEXT_PRIMARY_LIGHT;
            albumImage.setBackground(new ColorDrawable(color));
            title.setText(" ");
            title.setBackground(new ColorDrawable(color));
            info.setText(" ");
            info.setBackground(new ColorDrawable(color));
        }

        private void showTransparent() {
            title.setBackground(new ColorDrawable(Color.TRANSPARENT));
            info.setBackground(new ColorDrawable(Color.TRANSPARENT));
        }

        public void loadAlbumCover(MusicInfo track, int position) {
            Loaders.AlbumCoverLoader albumCoverLoader =
                    new Loaders.AlbumCoverLoader(itemView.getContext(), track.getPath(),
                            Theme.getDefaultAlbumCover());
            albumCoverLoader.setFinishCallback(bitmapDrawable -> {
                if (bitmapDrawable != null) {
                    track.setAlbumImage(bitmapDrawable);
                    if (track.getDuration() > -1) {
                        notifyItemChanged(position);
                    }
                }
            });
            albumCoverLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(MusicInfo musicInfo);
    }
}
