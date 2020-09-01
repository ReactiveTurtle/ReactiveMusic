package ru.reactiveturtle.tools;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class WaitDailog extends DialogFragment {
    private TextView mTitle;

    @Override
    public void onResume() {
        super.onResume();
        Window window = getDialog().getWindow();
        if(window == null) return;
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
        mTitle = view.findViewById(R.id.waitMessage);
        view.setMinimumWidth(getResources().getDisplayMetrics().widthPixels -
                getResources().getDimensionPixelSize(R.dimen.big) * 2);

        ProgressBar progressBar = view.findViewById(R.id.waitProgressBar);
        progressBar.getIndeterminateDrawable().setColorFilter(new LightingColorFilter(
                Helper.getThemeColor(getContext(), R.attr.colorAccent), Color.BLACK));
        return view;
    }

    public void showText(String text) {
        if (mTitle != null) {
            mTitle.setText(text);
        }
    }
}
