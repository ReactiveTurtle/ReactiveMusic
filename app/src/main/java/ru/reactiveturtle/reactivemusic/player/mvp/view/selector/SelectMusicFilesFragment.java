package ru.reactiveturtle.reactivemusic.player.mvp.view.selector;

import android.Manifest;
import android.content.Context;
import android.content.res.ColorStateList;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.fragment.app.Fragment;
import androidx.loader.content.AsyncTaskLoader;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.Permissions;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.Sorter;
import ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme;
import ru.reactiveturtle.tools.WaitDailog;

public class SelectMusicFilesFragment extends Fragment {
    private View mBackItem;
    private RecyclerView mRecyclerView;
    private SelectMusicFilesAdapter mSelectMusicFilesAdapter;
    private AppCompatEditText mEditText;
    private File root = null;
    private OnCheckListener mOnCheckListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSelectMusicFilesAdapter = new SelectMusicFilesAdapter();
        if (Permissions.hasExternalStorage(getContext())) {
            setRoot("/storage");
        }
    }

    @BindView(R.id.directoryAddAll)
    protected FloatingActionButton mAddAll;
    @BindView(R.id.directoryClearAll)
    protected FloatingActionButton mClearAll;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_music_files_fragment, container, false);
        ButterKnife.bind(this, view);

        mBackItem = view.findViewById(R.id.directory_dialog_back);
        View icon = mBackItem.findViewById(R.id.directory_adapter_type);
        icon.setBackgroundResource(R.drawable.ic_arrow_upward);
        icon.setScaleX(0.75f);
        icon.setScaleY(0.75f);
        Theme.changeColor(icon.getBackground(), Theme.CONTEXT_NEGATIVE_PRIMARY);
        ((TextView) mBackItem.findViewById(R.id.directory_adapter_name)).setText("...");
        mBackItem.findViewById(R.id.directory_adapter_mask_view).setOnClickListener(v -> {
            if (root != null && root.getParentFile() != null
                    && !root.equals(Environment.getExternalStorageDirectory())
                    && !root.getAbsolutePath().equals("/storage")) {
                root = root.getParentFile();
                if (!new File(mEditText.getText().toString()).exists() || !new File(mEditText.getText().toString()).isDirectory()) {
                    mEditText.setText(String.format("%s/%s", root.getAbsolutePath(), new File(mEditText.getText().toString()).getName()));
                } else {
                    mEditText.setText(root.getAbsolutePath());
                }
            } else if (root != null) {
                root = new File("/storage");
                mEditText.setText(root.getAbsolutePath());
            }
            if (root != null) {
                File[] files = root.listFiles();
                if (files == null) {
                    files = new File[0];
                }
                Arrays.sort(files, new Sorter.NameComaparator());
                Arrays.sort(files, new Sorter.FileComaparator());
                mSelectMusicFilesAdapter.setItems(Arrays.asList(files));
            }
        });

        mRecyclerView = view.findViewById(R.id.directory_dialog_recycler_view);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mRecyclerView.setAdapter(mSelectMusicFilesAdapter);
        mSelectMusicFilesAdapter.setOnItemClickListener((file, position) -> {
            if (file.isDirectory()) {
                if (file.getAbsolutePath().equals("/storage/emulated")) {
                    file = new File(file.getAbsolutePath() + "/0");
                }
                File[] files = file.listFiles();
                if (files != null) {
                    root = file;
                    if (!new File(mEditText.getText().toString()).exists() || !new File(mEditText.getText().toString()).isDirectory()) {
                        mEditText.setText(String.format("%s/%s", root.getAbsolutePath(), new File(mEditText.getText().toString()).getName()));
                    } else {
                        mEditText.setText(root.getAbsolutePath());
                    }
                    Arrays.sort(files, new Sorter.NameComaparator());
                    Arrays.sort(files, new Sorter.FileComaparator());
                    mSelectMusicFilesAdapter.setItems(Arrays.asList(files));
                }
            } else {
                List<String> strings = mSelectMusicFilesAdapter.getSelectedFiles();
                boolean exists = strings.contains(file.getAbsolutePath());
                if (!exists) {
                    mSelectMusicFilesAdapter.addSelectedItem(file.getAbsolutePath());
                } else {
                    mSelectMusicFilesAdapter.removeSelectedItem(file.getAbsolutePath());
                }
                if (mOnCheckListener != null) {
                    mOnCheckListener.onChecked(file.getAbsolutePath(), exists);
                }
            }
        });

        mEditText = view.findViewById(R.id.directory_dialog_edit_text);
        mEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                File file = new File(mEditText.getText().toString());
                File[] files;
                if (file.exists() && (files = file.listFiles()) != null) {
                    root = file;
                    if (!new File(mEditText.getText().toString()).exists() || !new File(mEditText.getText().toString()).isDirectory()) {
                        mEditText.setText(String.format("%s/%s", root.getAbsolutePath(), new File(mEditText.getText().toString()).getName()));
                    } else {
                        mEditText.setText(root.getAbsolutePath());
                    }
                    Arrays.sort(files, new Sorter.NameComaparator());
                    Arrays.sort(files, new Sorter.FileComaparator());
                    mSelectMusicFilesAdapter.setItems(Arrays.asList(files));
                } else {
                    Toast.makeText(getContext(), R.string.incorrect_path, Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateThemeContext();
        updateTheme();
    }

    private WaitDailog mWaitDialog;

    @OnClick(R.id.directoryAddAll)
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
                mSelectMusicFilesAdapter.notifyDataSetChanged();
                mRecyclerView.setScrollY(scroll);
            }
        });
        checkTask.forceLoad();
    }

    @OnClick(R.id.directoryClearAll)
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
                mSelectMusicFilesAdapter.notifyDataSetChanged();
                mRecyclerView.setScrollY(scroll);
            }
        });
        checkTask.forceLoad();
    }

    public void setRoot(String path) {
        root = new File(path);
        updateRoot();
    }

    public void updateRoot() {
        if (root.exists()) {
            File[] files = root.listFiles();
            if (files == null) {
                files = new File[0];
            }
            Arrays.sort(files, new Sorter.NameComaparator());
            Arrays.sort(files, new Sorter.FileComaparator());
            mSelectMusicFilesAdapter.setItems(Arrays.asList(files));
            if (mEditText != null && Permissions.hasExternalStorage(getContext() )) {
                mEditText.setText(root.getAbsolutePath());
            }
        } else {
            Toast.makeText(getContext(), R.string.incorrect_path, Toast.LENGTH_LONG).show();
        }
    }

    public void setSelectedFiles(List<String> files) {
        mSelectMusicFilesAdapter.setSelectedItems(files);
    }

    public void addSelectedFile(String path) {
        mSelectMusicFilesAdapter.addSelectedItem(path);
    }

    public void removeSelectedFile(String path) {
        mSelectMusicFilesAdapter.removeSelectedItem(path);
    }

    public void scrollTo(String path) {
        int index = mSelectMusicFilesAdapter.indexOf(path);
        if (index > -1 && index < mSelectMusicFilesAdapter.getItemCount()) {
            LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            assert llm != null;
            llm.scrollToPositionWithOffset(index, 20);
        }
    }

    public void setOnCheckListener(OnCheckListener onCheckListener) {
        mOnCheckListener = onCheckListener;
    }

    public void setSelectedItems(List<String> playlist) {
        mSelectMusicFilesAdapter.setSelectedItems(playlist);
    }

    public void updateThemeContext() {
        if (getView() != null) {
            Theme.changeColor(mBackItem.findViewById(R.id.directory_adapter_type)
                    .getBackground(), Theme.CONTEXT_NEGATIVE_PRIMARY);
            ((TextView) mBackItem.findViewById(R.id.directory_adapter_name))
                    .setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY);

            mEditText.setTextColor(ColorStateList.valueOf(Theme.CONTEXT_NEGATIVE_PRIMARY));
            Theme.changeColor(mEditText.getBackground(), Theme.CONTEXT_LIGHT);
        }
    }

    public void updateTheme() {
        if (getView() != null) {
            Theme.updateFab(mAddAll);
            Theme.updateFab(mClearAll);
        }
    }

    public interface OnCheckListener {
        void onChecked(String path, boolean exists);

        void onUpdate(List<String> selectedFiles);
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
            List<File> files = mSelectMusicFilesAdapter.getFiles();
            for (File file : files) {
                if (!file.isDirectory()) {
                    Helper.goToMainLooper(() ->
                            mWaitDialog.showText(file.getAbsolutePath()));
                    List<String> strings = mSelectMusicFilesAdapter.getSelectedFiles();
                    boolean exists = strings.contains(file.getAbsolutePath());
                    if (!exists && isAdd) {
                        try {
                            mTestMediaPlayer.reset();
                            mTestMediaPlayer.setDataSource(file.getAbsolutePath());
                            mTestMediaPlayer.prepare();
                            mSelectMusicFilesAdapter.getSelectedFiles().add(file.getAbsolutePath());
                        } catch (IOException ignored) {
                        }
                    } else if (exists && !isAdd) {
                        mSelectMusicFilesAdapter.getSelectedFiles().remove(file.getAbsolutePath());
                    }
                }
            }
            Helper.goToMainLooper(() ->
                    mOnCheckListener.onUpdate(new ArrayList<>(mSelectMusicFilesAdapter.getSelectedFiles())));
            return null;
        }
    }
}
