package ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.reactiveturtle.reactivemusic.R;

public class ThemesAdapter extends RecyclerView.Adapter<ThemesAdapter.ThemeViewHolder> {
    private List<ColorSet> mColorSets = new ArrayList<>();
    private final int size;
    private static final int ITEMS_COUNT_IN_LINE = 3;

    public ThemesAdapter() {
        size = Resources.getSystem().getDisplayMetrics().widthPixels
                / ITEMS_COUNT_IN_LINE;
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThemeViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.themes_line_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        int index = position * ITEMS_COUNT_IN_LINE;
        holder.showColorSet(index, holder.mItem1);
        holder.showColorSet(index + 1, holder.mItem2);
        holder.showColorSet(index + 2, holder.mItem3);
    }

    @Override
    public int getItemCount() {
        return (int) Math.ceil((float) mColorSets.size()
                / ITEMS_COUNT_IN_LINE);
    }

    public void setColorSets(@NonNull List<ColorSet> set) {
        mColorSets.clear();
        mColorSets.addAll(set);
        notifyDataSetChanged();
    }

    protected class ThemeViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.themeItem1)
        protected ConstraintLayout mItem1;
        @BindView(R.id.themeItem2)
        protected ConstraintLayout mItem2;
        @BindView(R.id.themeItem3)
        protected ConstraintLayout mItem3;

        public ThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mItem1.getLayoutParams().height = size;
            mItem2.getLayoutParams().height = size;
            mItem3.getLayoutParams().height = size;
        }

        private void showColorSet(int index, ConstraintLayout item) {
            if (index < mColorSets.size()) {
                ColorSet colorSet = mColorSets.get(index);
                View primaryDark = item.findViewById(R.id.themePrimaryDark);
                View primary = item.findViewById(R.id.themePrimary);
                View primaryLight = item.findViewById(R.id.themePrimaryLight);

                primaryDark.setBackgroundColor(colorSet.getPrimaryDark());
                primary.setBackgroundColor(colorSet.getPrimary());
                primaryLight.setBackgroundColor(colorSet.getPrimaryLight());

                Objects.requireNonNull(onItemClickListener);
                item.findViewById(R.id.themeClicker).setOnClickListener(view -> {
                    onItemClickListener.onItemClick(colorSet);
                });
            }
        }

    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(ColorSet colorSet);
    }
}
