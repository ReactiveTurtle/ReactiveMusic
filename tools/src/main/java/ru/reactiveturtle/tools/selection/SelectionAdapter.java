package ru.reactiveturtle.tools.selection;

import android.annotation.SuppressLint;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ru.reactiveturtle.tools.R;

public class SelectionAdapter extends RecyclerView.Adapter<SelectionAdapter.ViewSelectionHolder> {
    private ArrayList<Spannable> viewNames = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    @NonNull
    @Override
    public ViewSelectionHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewSelectionHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.selection_adapter_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewSelectionHolder holder, int i) {
        holder.textView.setText(viewNames.get(i));
        System.out.println(Integer.toHexString(holder.textView.getTextColors().getDefaultColor()));
    }

    @Override
    public int getItemCount() {
        return viewNames.size();
    }

    public void setItems(List<Spannable> viewName) {
        viewNames.addAll(viewName);
        notifyDataSetChanged();
    }

    public class ViewSelectionHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        ViewSelectionHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.selection_item_text_view);
            View clicker = itemView.findViewById(R.id.selection_item_clicker);

            clicker.setOnClickListener(view -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(getLayoutPosition(), viewNames.get(getLayoutPosition()));
                }
            });
        }
    }

    void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position, CharSequence result);
    }
}
