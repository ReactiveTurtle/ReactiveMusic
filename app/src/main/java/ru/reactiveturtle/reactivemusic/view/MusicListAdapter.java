package ru.reactiveturtle.reactivemusic.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.AllMusicDataManager;
import ru.reactiveturtle.reactivemusic.player.MusicData;
import ru.reactiveturtle.reactivemusic.player.MusicMetadata;
import ru.reactiveturtle.reactivemusic.player.shared.MusicAlbumCoverData;
import ru.reactiveturtle.reactivemusic.theme.Theme;

public class MusicListAdapter extends RecyclerView.Adapter<MusicListAdapter.MusicViewHolder> {
    private AllMusicDataManager allMusicDataManager;

    @ColorInt
    private int grayColor;

    @ColorInt
    private int textColor;

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MusicViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_music_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        MusicData musicData = allMusicDataManager.getMusicData(position);
        if (musicData.getMetadata() == null || musicData.getCoverData() == null) {
            holder.showGray();
        }
        if (musicData.getCoverData() == null) {
            allMusicDataManager.requestData(allMusicDataManager.getPaths().get(position), new AllMusicDataManager.RequestCallback() {
                @Override
                public void onDataLoad(MusicData musicData) {
                    notifyItemChanged(position);
                }

                @Override
                public void onMetadataLoad(MusicMetadata musicMetadata) {
                }

                @Override
                public void onCoverDataLoad(MusicAlbumCoverData cover) {
                    notifyItemChanged(position);
                }
            });
        } else {
            holder.showCover(musicData);
        }
        if (musicData.getMetadata() != null) {
            holder.showInfo(musicData);
        }
    }

    @Override
    public void onViewRecycled(@NonNull MusicViewHolder holder) {
        super.onViewRecycled(holder);
        allMusicDataManager.recycle(holder.getLayoutPosition());
    }

    @Override
    public int getItemCount() {
        return allMusicDataManager == null ? 0 : allMusicDataManager.getPaths().size();
    }

    private String currentTrackPath;

    public void setCurrentTrackPath(String path) {
        int prevIndex = -1;
        if (currentTrackPath != null) {
            prevIndex = indexOf(currentTrackPath);
        }

        currentTrackPath = path;
        if (prevIndex > -1) {
            notifyItemChanged(prevIndex);
        }

        if (currentTrackPath != null) {
            int index = indexOf(currentTrackPath);
            if (index > -1) {
                notifyItemChanged(index);
            }
        }
    }

    public void updateTheme(Theme theme) {
        grayColor = theme.isDark() ? theme.getThemeContext().getLight() : theme.getThemeContext().getPrimaryLight();
        textColor = theme.isDark() ? theme.getThemeContext().getNegativePrimary() : theme.getThemeContext().getNegativePrimary();
        notifyDataSetChanged();
    }

    public void setAllMusicDataManager(AllMusicDataManager allMusicDataManager) {
        if (this.allMusicDataManager != null) {
            this.allMusicDataManager.removePathsLoadListener(pathsLoadListener);
        }

        this.allMusicDataManager = allMusicDataManager;
        if (allMusicDataManager != null) {
            allMusicDataManager.addPathsLoadListener(pathsLoadListener);
        }
    }

    private AllMusicDataManager.PathsLoadListener pathsLoadListener = this::notifyDataSetChanged;

    private int indexOf(@NonNull String path) {
        return allMusicDataManager.getPaths().indexOf(x -> x.equals(path));
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

            title.setTextColor(textColor);
            info.setTextColor(textColor);
        }

        @OnClick(R.id.player_music_list_item_clicker)
        protected void itemClick() {
            Objects.requireNonNull(onItemClickListener);
            allMusicDataManager.requestData(getLayoutPosition(), new AllMusicDataManager.RequestCallback() {
                @Override
                public void onDataLoad(MusicData musicData) {
                    onItemClickListener.onClick(musicData);
                }

                @Override
                public void onMetadataLoad(MusicMetadata musicMetadata) {
                }

                @Override
                public void onCoverDataLoad(MusicAlbumCoverData cover) {
                }
            });
        }

        private void showGray() {
            albumImage.setBackground(new ColorDrawable(grayColor));
            title.setText(" ");
            title.setBackground(new ColorDrawable(grayColor));
            info.setText(" ");
            info.setBackground(new ColorDrawable(grayColor));
        }

        private void showCover(MusicData musicData) {
            assert musicData.getCoverData() != null;
            albumImage.setBackground(musicData.getCoverData().getCover());
        }

        private void showInfo(MusicData musicData) {
            assert musicData.getMetadata() != null;

            title.setBackground(new ColorDrawable(Color.TRANSPARENT));
            info.setBackground(new ColorDrawable(Color.TRANSPARENT));
            title.setText(musicData.getMetadata().getTitle());
            info.setText(String.format("%s | %s", musicData.getMetadata().getArtist(), musicData.getMetadata().getAlbum()));
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(MusicData musicData);
    }
}