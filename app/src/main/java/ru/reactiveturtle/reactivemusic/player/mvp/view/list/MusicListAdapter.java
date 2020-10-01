package ru.reactiveturtle.reactivemusic.player.mvp.view.list;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.mvp.view.music.MusicFragment;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.Loaders;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicViewHolder> {
    protected List<MusicInfo> mMusicList = new ArrayList<>();

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MusicViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_music_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        MusicInfo track = mMusicList.get(position);
        if (track.getAlbumImage() != null || track.getDuration() < 0) {
            holder.showInfo(track);
        } else {
            holder.showGray();
            Loaders.AlbumCoverLoader albumCoverLoader =
                    new Loaders.AlbumCoverLoader(holder.itemView.getContext(), track.getPath(), Theme.getDefaultAlbumCover());
            albumCoverLoader.registerListener(0, (loader, data) -> {
                Loaders.MusicInfoLoader musicInfoLoader = new Loaders.MusicInfoLoader(loader.getContext(), track.getPath());
                musicInfoLoader.registerListener(0, (loader1, musicInfo) -> {
                    if (musicInfo == null) {
                        track.setDuration(-1);
                    } else {
                        musicInfo.setAlbumImage(data);
                        if (position < mMusicList.size()) {
                            mMusicList.set(position, musicInfo);
                        }
                        holder.showTransparent();
                        holder.showInfo(musicInfo);
                    }
                });
                musicInfoLoader.forceLoad();
            });
            albumCoverLoader.forceLoad();
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
            tracks.add(new MusicInfo(allTracks.get(i)));
        }
        mMusicList.addAll(tracks);
        notifyDataSetChanged();
    }

    public void addItem(String path) {
        mMusicList.add(new MusicInfo(path));
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
                albumImage.setBackground(track.getAlbumImage());
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

        private void showGray() {
            int color = Theme.IS_DARK ? Theme.CONTEXT_LIGHT : Theme.CONTEXT_PRIMARY_LIGHT;
            albumImage.setBackground(new ColorDrawable(color));
            title.setText("");
            title.setBackground(new ColorDrawable(color));
            info.setText("");
            info.setBackground(new ColorDrawable(color));
        }

        private void showTransparent() {
            title.setBackground(new ColorDrawable(Color.TRANSPARENT));
            info.setBackground(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(MusicInfo musicInfo);
    }
}
