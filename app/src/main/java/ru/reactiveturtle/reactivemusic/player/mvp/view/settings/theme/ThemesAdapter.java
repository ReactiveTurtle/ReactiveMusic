package ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import ru.reactiveturtle.reactivemusic.R;

public class ThemesAdapter extends RecyclerView.Adapter<ThemesAdapter.ThemeViewHolder> {
    private List<ColorSet> mColorSets = new ArrayList<>();
    private int size;
    private int ITEMS_COUNT_IN_LINE;

    public ThemesAdapter() {
        if (Resources.getSystem().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ITEMS_COUNT_IN_LINE = 6;
        } else {
            ITEMS_COUNT_IN_LINE = 3;
        }
    }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ThemeViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.themes_line_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position) {
        System.out.println("bind: " + position + ", count: " + getItemCount());
        size = Resources.getSystem().getDisplayMetrics().widthPixels
                / ITEMS_COUNT_IN_LINE;
        int index = position * ITEMS_COUNT_IN_LINE;
        holder.showColorSet(index, holder.mItem1);
        holder.showColorSet(index + 1, holder.mItem2);
        holder.showColorSet(index + 2, holder.mItem3);
        if (ITEMS_COUNT_IN_LINE > 3) {
            holder.showColorSet(index + 3, holder.mItem4);
            holder.showColorSet(index + 4, holder.mItem5);
            holder.showColorSet(index + 5, holder.mItem6);
        }
    }

    @Override
    public int getItemCount() {
        return (int) Math.ceil((float) mColorSets.size()
                / ITEMS_COUNT_IN_LINE);
    }

    public void setColorSets(@NonNull List<ColorSet> colorSetList) {
        mColorSets.clear();
        mColorSets.addAll(colorSetList);
        notifyDataSetChanged();
    }

    protected class ThemeViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.themeItem1)
        protected ConstraintLayout mItem1;
        @BindView(R.id.themeItem2)
        protected ConstraintLayout mItem2;
        @BindView(R.id.themeItem3)
        protected ConstraintLayout mItem3;
        protected ConstraintLayout mItem4;
        protected ConstraintLayout mItem5;
        protected ConstraintLayout mItem6;

        public ThemeViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mItem4 = itemView.findViewById(R.id.themeItem4);
            mItem5 = itemView.findViewById(R.id.themeItem5);
            mItem6 = itemView.findViewById(R.id.themeItem6);
            mItem1.getLayoutParams().height = size;
            mItem2.getLayoutParams().height = size;
            mItem3.getLayoutParams().height = size;
            if (ITEMS_COUNT_IN_LINE > 3) {
                mItem4.getLayoutParams().height = size;
                mItem5.getLayoutParams().height = size;
                mItem6.getLayoutParams().height = size;
            }
        }

        private void showColorSet(int index, ConstraintLayout item) {
            if (item != null) {
                System.out.println(index);
                View colorSetView = item.findViewById(R.id.themeColorSet);
                Drawable background = null;
                if (index > -1 && index < mColorSets.size()) {
                    ColorSet colorSet = mColorSets.get(index);
                    System.out.println(colorSet.toString());
                    background = ThemeHelper.getColorSetDrawable(colorSet);

                    Objects.requireNonNull(onItemClickListener);
                    item.findViewById(R.id.themeClicker).setOnClickListener(view -> {
                        onItemClickListener.onItemClick(colorSet);
                    });
                }
                colorSetView.setBackground(background);
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
