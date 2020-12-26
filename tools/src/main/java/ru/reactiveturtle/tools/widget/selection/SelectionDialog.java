package ru.reactiveturtle.tools.widget.selection;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ru.reactiveturtle.tools.R;
import ru.reactiveturtle.tools.widget.BaseDialog;

public class SelectionDialog extends BaseDialog {
    private SelectionAdapter adapter;

    private Builder mBuilder;

    private SelectionDialog(Builder builder) {
        mBuilder = builder;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.selection_dialog_fragment, container, false);
        view.setBackground(mBuilder.background);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getDialog().getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        RecyclerView recyclerView = view.findViewById(R.id.selection_fragment_recycler_view);

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(), llm.getOrientation());
        dividerItemDecoration.setDrawable(new ColorDrawable(mBuilder.dividerColor));
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(adapter);
        if (mBuilder.title == null)
            view.findViewById(R.id.select_dialog_title).setVisibility(View.GONE);
        else {
            TextView textView = view.findViewById(R.id.select_dialog_title);
            textView.setText(mBuilder.title);
            textView.setTextColor(mBuilder.titleColor);
        }
        adapter.setItems(mBuilder.items);
        recyclerView.scrollToPosition(mBuilder.position);
        return view;
    }

    public void setOnItemClickListener(SelectionAdapter.OnItemClickListener onItemClickListener) {
        adapter = new SelectionAdapter();
        adapter.setOnItemClickListener(onItemClickListener);
    }

    public static class Builder implements ISelectionDialog {
        private Drawable background;

        @Override
        public Builder setBackground(Drawable drawable) {
            background = drawable;
            return this;
        }

        private String title = null;

        @Override
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        private int titleColor;
        @Override
        public Builder setTitleColor(int color) {
            titleColor = color;
            return this;
        }

        private List<Spannable> items = new ArrayList<>();

        @Override
        public SelectionDialog.Builder addItems(Spannable... items) {
            this.items.addAll(Arrays.asList(items));
            return this;
        }

        private int position = 0;

        @Override
        public SelectionDialog.Builder setPosition(int position) {
            this.position = position;
            return this;
        }

        private int dividerColor;
        @Override
        public Builder setDividerColor(@ColorInt int color) {
            dividerColor = color;
            return this;
        }

        public SelectionDialog build() {
            return new SelectionDialog(this);
        }
    }
}
