package ru.reactiveturtle.tools.widget.warning;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ru.reactiveturtle.tools.R;
import ru.reactiveturtle.tools.widget.BaseDialog;

public class MessageDialog extends BaseDialog {
    private Builder mBuilder;
    private OnClickListener onClickListener;

    private MessageDialog(Builder builder) {
        mBuilder = builder;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getDialog() != null
                && getDialog().getWindow() != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getDialog().getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        ConstraintLayout view = (ConstraintLayout) inflater.inflate(R.layout.name_dialog_fragment, container, false);
        view.setBackground(mBuilder.background);
        TextView title = view.findViewById(R.id.nameTitle);
        title.setText(mBuilder.title);
        title.setTypeface(title.getTypeface(), Typeface.NORMAL);
        title.setTextColor(mBuilder.titleColor);

        EditText editText = view.findViewById(R.id.name_edit_text);
        editText.setVisibility(View.GONE);
        editText.setEnabled(false);
        editText.setClickable(false);

        Button positiveButton = view.findViewById(R.id.rightButton);
        positiveButton.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onPositiveButtonClicked();
            }
        });
        Button negativeButton = view.findViewById(R.id.leftButton);
        negativeButton.setOnClickListener(v -> {
            if (onClickListener != null) {
                onClickListener.onNegativeButtonClicked();
                dismiss();
            }
        });

        positiveButton.setText(mBuilder.positiveText);
        positiveButton.setTextColor(mBuilder.positiveTextColor);
        positiveButton.setBackground(mBuilder.positiveDrawable);
        negativeButton.setText(mBuilder.negativeText);
        negativeButton.setTextColor(mBuilder.negativeTextColor);
        negativeButton.setBackground(mBuilder.negativeDrawable);
        return view;
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public abstract static class OnClickListener {
        public void onPositiveButtonClicked() {

        }

        public void onNegativeButtonClicked() {

        }
    }

    public static class Builder implements IMessageDialog {
        private Drawable background;

        @Override
        public MessageDialog.Builder setBackground(Drawable drawable) {
            background = drawable;
            return this;
        }

        private String title = "";

        @Override
        public MessageDialog.Builder setTitle(String text) {
            title = text;
            return this;
        }

        private int titleColor;

        @Override
        public MessageDialog.Builder setTitleColor(int color) {
            titleColor = color;
            return this;
        }

        private String positiveText = "";

        @Override
        public MessageDialog.Builder setPositiveText(String text) {
            positiveText = text;
            return this;
        }

        private int positiveTextColor = Color.WHITE;

        @Override
        public MessageDialog.Builder setPositiveTextColor(int color) {
            positiveTextColor = color;
            return this;
        }

        private Drawable positiveDrawable;

        @Override
        public MessageDialog.Builder setPositiveBackground(Drawable drawable) {
            positiveDrawable = drawable;
            return this;
        }

        private String negativeText = "";

        @Override
        public MessageDialog.Builder setNegativeText(String text) {
            negativeText = text;
            return this;
        }

        private int negativeTextColor = Color.WHITE;

        @Override
        public MessageDialog.Builder setNegativeTextColor(int color) {
            negativeTextColor = color;
            return this;
        }

        private Drawable negativeDrawable;

        @Override
        public MessageDialog.Builder setNegativeBackground(Drawable drawable) {
            negativeDrawable = drawable;
            return this;
        }

        public MessageDialog buildDialog() {
            return new MessageDialog(this);
        }
    }
}
