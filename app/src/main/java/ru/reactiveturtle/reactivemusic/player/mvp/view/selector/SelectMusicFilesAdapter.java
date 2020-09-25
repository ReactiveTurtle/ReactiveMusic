package ru.reactiveturtle.reactivemusic.player.mvp.view.selector;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.Loaders;

public class SelectMusicFilesAdapter extends RecyclerView.Adapter<SelectMusicFilesAdapter.SelectFreeFilesHolder> {
    private List<File> mFiles = new ArrayList<>();
    private List<String> mFilesReplace = new ArrayList<>();
    private List<Drawable> mIconFiles = new ArrayList<>();
    private List<String> mSelectedFiles = new ArrayList<>();
    private OnItemClickListener mOnItemClickListener;

    @NonNull
    @Override
    public SelectFreeFilesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SelectFreeFilesHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.select_music_files_adapter_item, parent, false));
    }

    private static final ColorDrawable DEFAULT_GRAY = new ColorDrawable(Theme.CONTEXT_PRIMARY_LIGHT);
    @Override
    public void onBindViewHolder(@NonNull SelectFreeFilesHolder holder, int position) {
        if (mFiles.get(position).isDirectory()) {
            holder.type.setBackgroundResource(R.drawable.ic_folder_open);
            Theme.changeColor(holder.type.getBackground(), Theme.CONTEXT_NEGATIVE_PRIMARY);
        } else {
            if (mIconFiles.get(position) != null) {
                holder.type.setBackground(mIconFiles.get(position));
            } else {
                mIconFiles.set(position, DEFAULT_GRAY);
                holder.type.setBackground(DEFAULT_GRAY);
                Loaders.MusicInfoLoader musicInfoLoader =
                        new Loaders.MusicInfoLoader(holder.itemView.getContext(), mFiles.get(position).getAbsolutePath());
                musicInfoLoader.registerListener(0, (loader, musicInfo) -> {
                    if (musicInfo != null) {
                        Objects.requireNonNull(musicInfo);
                        mFilesReplace.set(position, musicInfo.getArtist() + " - " + musicInfo.getTitle());
                        holder.name.setText(mFilesReplace.get(position));
                        Loaders.AlbumCoverLoader albumCoverLoader = new Loaders.AlbumCoverLoader(
                                loader.getContext(), musicInfo.getPath(), Theme.getDefaultAlbumCover());
                        albumCoverLoader.registerListener(0, (loader1, data) -> {
                            mIconFiles.set(position, data);
                            holder.type.setBackground(data);
                        });
                        albumCoverLoader.forceLoad();
                    } else {
                        mIconFiles.set(position, ResourcesCompat.getDrawable(holder.itemView.getResources(),
                                R.drawable.ic_unknown_file, holder.itemView.getContext().getTheme()));
                        holder.type.setBackground(mIconFiles.get(position));
                        Theme.changeColor(holder.type.getBackground(), Theme.CONTEXT_NEGATIVE_PRIMARY);
                    }
                });
                musicInfoLoader.forceLoad();
            }
        }

        holder.name.setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);
        if (mFilesReplace.get(position) != null) {
            holder.name.setText(mFilesReplace.get(position));
        } else {
            holder.name.setText(mFiles.get(position).getName());
        }
        if (mFiles.get(position).isDirectory()) {
            holder.check.setVisibility(View.GONE);
            holder.check.setChecked(false);
        } else {
            holder.check.setVisibility(View.VISIBLE);
            String size = Helper.getFileSize(mFiles.get(position).length(), holder.itemView.getResources());
            holder.name.setText(holder.name.getText().toString() + "\n" + size);
            String fileName = mFiles.get(position).getAbsolutePath();
            boolean isChecked = mSelectedFiles.contains(fileName);
            holder.check.setChecked(isChecked);
        }
        holder.check.setBackground(Theme.getCheckDrawable(
                R.drawable.ic_check, R.drawable.ic_check, BitmapDrawable.class));
        holder.bottomDivider.setBackgroundColor(Theme.CONTEXT_LIGHT);
    }

    @Override
    public int getItemCount() {
        return mFiles.size();
    }

    public void setItems(List<File> files) {
        mFiles.clear();
        mFiles.addAll(files);
        mFilesReplace = new ArrayList<>(Arrays.asList(new String[files.size()]));
        mIconFiles.clear();
        mIconFiles.addAll(Arrays.asList(new BitmapDrawable[files.size()]));
        notifyDataSetChanged();
    }

    public void setSelectedItems(List<String> files) {
        mSelectedFiles.clear();
        mSelectedFiles.addAll(files);
        notifyDataSetChanged();
    }

    public void addSelectedItem(String path) {
        int index = indexOf(path);
        mSelectedFiles.add(path);
        if (index > -1) {
            notifyItemChanged(index);
        }
    }


    public void removeSelectedItem(String path) {
        int index = indexOf(path);
        mSelectedFiles.remove(path);
        if (index > -1) {
            notifyItemChanged(index);
        }
    }

    public List<String> getSelectedFiles() {
        return mSelectedFiles;
    }

    public int indexOf(String path) {
        for (int i = 0; i < mFiles.size(); i++) {
            if (mFiles.get(i).getAbsolutePath().equals(path)) {
                return i;
            }
        }
        return -1;
    }

    public List<File> getFiles() {
        return mFiles;
    }

    protected class SelectFreeFilesHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.directory_adapter_type)
        protected View type;
        @BindView(R.id.directory_adapter_name)
        protected TextView name;
        @BindView(R.id.directory_adapter_check_view)
        protected CheckBox check;
        @BindView(R.id.directoryAdapterBottomDivider)
        protected View bottomDivider;

        SelectFreeFilesHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.directory_adapter_mask_view)
        public void click() {
            Objects.requireNonNull(mOnItemClickListener);
            mOnItemClickListener.onDirClick(mFiles.get(getLayoutPosition()), getLayoutPosition());
        }
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onDirClick(File file, int position);
    }
}
