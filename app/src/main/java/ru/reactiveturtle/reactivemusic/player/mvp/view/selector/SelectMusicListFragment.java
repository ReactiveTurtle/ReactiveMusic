package ru.reactiveturtle.reactivemusic.player.mvp.view.selector;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.MusicInfo;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.tools.widget.wait.WaitDailog;

public class SelectMusicListFragment extends Fragment {
    @BindView(R.id.selectMusicListRecyclerView)
    protected RecyclerView mRecyclerView;
    private SelectMusicListAdapter mMusicListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @BindView(R.id.selectMusicAddAll)
    protected FloatingActionButton mAddAll;
    @BindView(R.id.selectMusicClearAll)
    protected FloatingActionButton mClearAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_music_list_fragment, container);
        ButterKnife.bind(this, view);

        LinearLayoutManager llm = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(llm);
        mMusicListAdapter = new SelectMusicListAdapter(llm);
        mRecyclerView.setAdapter(mMusicListAdapter);
        mRecyclerView.setItemAnimator(null);
        if (getContext() != null) {
            mMusicListAdapter.setOnItemClickListener((musicInfo, isChecked) -> {
                if (isChecked) {
                    mMusicListAdapter.removeSelectedItem(musicInfo.getPath());
                } else {
                    mMusicListAdapter.addSelectedItem(musicInfo.getPath());
                }
                Objects.requireNonNull(mOnCheckListener);
                mOnCheckListener.onChecked(musicInfo.getPath(), isChecked);
            });
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateTheme();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void addSelectedTrack(String path) {
        mMusicListAdapter.addSelectedItem(path);
    }

    public void removeSelectedTrack(String path) {
        mMusicListAdapter.removeSelectedItem(path);
    }

    private WaitDailog mWaitDialog;

    @OnClick(R.id.selectMusicAddAll)
    protected void addAllClick() {
        mWaitDialog = Theme.getWaitDialogBuilder().build();
        mWaitDialog.show(getFragmentManager(), "waitDialog");
        int scroll = mRecyclerView.getScrollY();
        CheckTask checkTask = new CheckTask(getContext(), true);
        checkTask.registerListener(0, new Loader.OnLoadCompleteListener<Void>() {
            @Override
            public void onLoadComplete(@NonNull Loader<Void> loader, @Nullable Void data) {
                checkTask.unregisterListener(this);
                mWaitDialog.dismiss();
                mMusicListAdapter.notifyDataSetChanged();
                mRecyclerView.setScrollY(scroll);
            }
        });
        checkTask.forceLoad();
    }

    @OnClick(R.id.selectMusicClearAll)
    protected void clearAllClick() {
        mWaitDialog = Theme.getWaitDialogBuilder().build();
        mWaitDialog.show(getFragmentManager(), "waitDialog");
        int scroll = mRecyclerView.getScrollY();
        CheckTask checkTask = new CheckTask(getContext(), false);
        checkTask.registerListener(0, new Loader.OnLoadCompleteListener<Void>() {
            @Override
            public void onLoadComplete(@NonNull Loader<Void> loader, @Nullable Void data) {
                checkTask.unregisterListener(this);
                mWaitDialog.dismiss();
                mMusicListAdapter.notifyDataSetChanged();
                mRecyclerView.setScrollY(scroll);
            }
        });
        checkTask.forceLoad();
    }


    public void updateTheme() {
        if (getView() != null) {
            Theme.updateFab(mAddAll);
            Theme.updateFab(mClearAll);
        }
    }

    private OnCheckListener mOnCheckListener;

    public void setOnCheckListener(OnCheckListener onCheckListener) {
        mOnCheckListener = onCheckListener;
    }

    public void setSelectedItems(List<String> playlist) {
        mMusicListAdapter.setSelectedItems(playlist);
    }

    public SelectMusicListAdapter getListAdapter() {
        return mMusicListAdapter;
    }

    public interface OnCheckListener {
        void onChecked(String path, boolean exists);

        void onUpdate(List<String> playlist);
    }

    protected class CheckTask extends AsyncTaskLoader<Void> {
        private final boolean isAdd;

        public CheckTask(Context context, boolean isAdd) {
            super(context);
            this.isAdd = isAdd;
        }

        private MediaPlayer mTestMediaPlayer;

        @Nullable
        @Override
        public Void loadInBackground() {
            mTestMediaPlayer = new MediaPlayer();
            List<MusicInfo> tracks = mMusicListAdapter.getTracks();
            for (MusicInfo musicInfo : tracks) {
                Helper.goToMainLooper(() ->
                        mWaitDialog.showText(musicInfo.getPath()));
                List<String> strings = mMusicListAdapter.getSelectedFiles();
                boolean exists = strings.contains(musicInfo.getPath());
                if (!exists && isAdd) {
                    mMusicListAdapter.getSelectedFiles().add(musicInfo.getPath());
                } else if (exists && !isAdd) {
                    mMusicListAdapter.getSelectedFiles().remove(musicInfo.getPath());
                }
            }
            Helper.goToMainLooper(() ->
                    mOnCheckListener.onUpdate(new ArrayList<>(mMusicListAdapter.getSelectedFiles())));
            return null;
        }
    }
}
