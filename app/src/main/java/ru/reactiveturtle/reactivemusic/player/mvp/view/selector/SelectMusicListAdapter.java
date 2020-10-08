package ru.reactiveturtle.reactivemusic.player.mvp.view.selector;

import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.view.list.MusicListAdapter;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.ThemeHelper;

public class SelectMusicListAdapter extends MusicListAdapter {
    private List<String> mSelectedFiles = new ArrayList<>();

    public SelectMusicListAdapter(@NonNull LinearLayoutManager linearLayoutManager) {
        super(linearLayoutManager);
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SelectMusicViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.select_music_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ((SelectMusicViewHolder) holder).checkBox.setBackground(ThemeHelper.getCheckDrawable(
                R.drawable.ic_check, R.drawable.ic_check, BitmapDrawable.class));
        ((SelectMusicViewHolder) holder).checkBox.setChecked(
                mSelectedFiles.contains(mMusicList.get(position).getPath()));
    }

    public void setSelectedItems(List<String> files) {
        mSelectedFiles.clear();
        mSelectedFiles.addAll(files);
        notifyDataSetChanged();
    }

    public void addSelectedItem(String path) {
        mSelectedFiles.add(path);
        notifyItemChanged(indexOf(path));
    }

    public void removeSelectedItem(String path) {
        mSelectedFiles.remove(path);
        notifyItemChanged(indexOf(path));
    }

    public List<String> getSelectedFiles() {
        return mSelectedFiles;
    }

    public class SelectMusicViewHolder extends MusicListAdapter.MusicViewHolder {
        @BindView(R.id.selectMusicListCheckBox)
        protected CheckBox checkBox;

        public SelectMusicViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @OnClick(R.id.selectMusicListItemClicker)
        @Override
        protected void itemClick() {
            Objects.requireNonNull(onItemClickListener);
            onItemClickListener.onClick(mMusicList.get(getLayoutPosition()), checkBox.isChecked());
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onClick(MusicInfo musicInfo, boolean isChecked);
    }
}
