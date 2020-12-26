package ru.reactiveturtle.reactivemusic.player.mvp.view.playlist;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ThemeHelper;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistHolder> {
    private List<String> playlists = new ArrayList<>();

    @NonNull
    @Override
    public PlaylistHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaylistHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.playlist_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistHolder holder, int position) {
        holder.name.setText(playlists.get(position));
        holder.name.setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);
        holder.bottomDivider.setBackgroundColor(ThemeHelper.setAlpha("42", Theme.CONTEXT_LIGHT));
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    public void setPlaylists(List<String> playlists) {
        this.playlists.clear();
        this.playlists.addAll(playlists);
        notifyDataSetChanged();
    }

    public void addPlaylist(String result) {
        this.playlists.add(result);
        notifyItemInserted(getItemCount() - 1);
    }

    public void renamePlaylist(String oldName, String newName) {
        int index = playlists.indexOf(oldName);
        playlists.set(index, newName);
        notifyItemChanged(index);
    }

    public void removePlaylist(String playlistName) {
        notifyItemRemoved(playlists.indexOf(playlistName));
        this.playlists.remove(playlistName);
    }

    protected class PlaylistHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.playlistName)
        protected TextView name;

        @BindView(R.id.playlistBottomDivider)
        protected View bottomDivider;

        public PlaylistHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.playlistClicker)
        protected void itemClick() {
            Objects.requireNonNull(onItemClickListener);
            onItemClickListener.onClick(playlists.get(getLayoutPosition()));
        }

        @OnLongClick(R.id.playlistClicker)
        protected void itemLongClick() {
            Objects.requireNonNull(onItemClickListener);
            onItemClickListener.onLongClick(playlists.get(getLayoutPosition()));
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(String playlistName);

        void onLongClick(String playlistName);
    }
}
