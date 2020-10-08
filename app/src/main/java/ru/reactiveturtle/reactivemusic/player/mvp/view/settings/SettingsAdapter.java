package ru.reactiveturtle.reactivemusic.player.mvp.view.settings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ru.reactiveturtle.reactivemusic.R;

public class SettingsAdapter extends RecyclerView.Adapter<SettingsAdapter.LineViewHolder> {
    private List<SettingsItem> items = new ArrayList<>();

    @NonNull
    @Override
    public LineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LineViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.settings_line_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull LineViewHolder holder, int position) {
        int index = position * 3;
        holder.showItem(holder.item1, index);
        holder.showItem(holder.item2, index + 1);
        holder.showItem(holder.item3, index + 2);
    }

    @Override
    public int getItemCount() {
        return (int) Math.ceil(items.size() / 3f);
    }

    public void addItem(@NonNull SettingsItem item) {
        items.add(item);
    }

    public void removeItem(int position) {
        items.remove(position);
    }

    protected class LineViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.settingsLineItem1)
        protected ConstraintLayout item1;
        @BindView(R.id.settingsLineItem2)
        protected ConstraintLayout item2;
        @BindView(R.id.settingsLineItem3)
        protected ConstraintLayout item3;

        public LineViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void showItem(ConstraintLayout itemRoot, int index) {
            if (index < items.size()) {
                SettingsItem item = items.get(index);
                View icon = itemRoot.findViewById(R.id.settingsItemIcon);
                TextView title = itemRoot.findViewById(R.id.settingsItemTitle);

                icon.setBackground(item.getIcon());
                title.setText(item.getTitle());

                View clicker = itemRoot.findViewById(R.id.settingsItemClicker);
                clicker.setOnClickListener(view -> {
                    Objects.requireNonNull(onItemClickListener);
                    onItemClickListener.onItemClick(index);
                });
            } else {
                itemRoot.setVisibility(View.GONE);
            }
        }
    }

    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int index);
    }
}
