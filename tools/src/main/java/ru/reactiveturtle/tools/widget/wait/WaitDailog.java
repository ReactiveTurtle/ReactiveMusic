package ru.reactiveturtle.tools.widget.wait;

import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import ru.reactiveturtle.tools.R;

public class WaitDailog extends DialogFragment {
    private TextView mTitle;
    private Builder mBuilder;

    private WaitDailog(@NonNull Builder builder) {
        mBuilder = builder;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() == null) return;
        Window window = getDialog().getWindow();
        if (window == null) return;
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = getResources().getDisplayMetrics().widthPixels -
                getResources().getDimensionPixelSize(R.dimen.middle);
        window.setAttributes(params);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        View view = inflater.inflate(R.layout.wait_dialog_fragment, container, false);
        view.setBackground(mBuilder.background);

        mTitle = view.findViewById(R.id.waitMessage);
        mTitle.setTextColor(mBuilder.textColor);
        view.setMinimumWidth(getResources().getDisplayMetrics().widthPixels -
                getResources().getDimensionPixelSize(R.dimen.big) * 2);

        ProgressBar progressBar = view.findViewById(R.id.waitProgressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(
                mBuilder.progressColor, Color.BLACK));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
    }

    public void showText(String text) {
        if (mTitle != null) {
            mTitle.setText(text);
        }
    }

    public static class Builder implements IWaitDialog {
        private Drawable background;

        @Override
        public Builder setBackground(@Nullable Drawable drawable) {
            background = drawable;
            return this;
        }

        private int textColor = 0;

        @Override
        public Builder setTextColor(@ColorInt int color) {
            textColor = color;
            return this;
        }

        private int progressColor = 0;

        @Override
        public Builder setProgressColor(@ColorInt int color) {
            progressColor = color;
            return this;
        }

        public WaitDailog build() {
            return new WaitDailog(this);
        }
    }
}
