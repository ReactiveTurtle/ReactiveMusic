package ru.reactiveturtle.tools.text;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import ru.reactiveturtle.tools.R;

public class TextDialog extends BottomSheetDialogFragment {
    private AppCompatEditText editText;
    private OnClickListener onClickListener;

    private Builder mBuilder;

    TextDialog(@NonNull Builder builder) {
        mBuilder = builder;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        if (getDialog() != null
                && getDialog().getWindow() != null
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getDialog().getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        View view = inflater.inflate(R.layout.name_dialog_fragment, container, false);
        view.setBackground(mBuilder.background);
        TextView title = view.findViewById(R.id.nameTitle);
        title.setText(mBuilder.title);
        title.setTextColor(mBuilder.titleColor);

        editText = view.findViewById(R.id.name_edit_text);
        editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, mBuilder.textSize);
        editText.setTextColor(mBuilder.textColor);
        editText.setBackground(mBuilder.editTextBackground);
        editText.setEnabled(true);
        editText.setLines(mBuilder.lineCount);
        editText.setClickable(true);
        editText.setFocusable(true);

        Button positiveButton = view.findViewById(R.id.rightButton);
        positiveButton.setOnClickListener(v -> {
            if (onClickListener != null && editText.getText().toString().length() > 0) {
                InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                onClickListener.onPositiveButtonClicked(editText.getText().toString());
            } else {
                Toast.makeText(getContext(), "Введите название плейлиста", Toast.LENGTH_SHORT).show();
            }
        });
        Button negativeButton = view.findViewById(R.id.leftButton);
        negativeButton.setOnClickListener(v -> {
            if (onClickListener != null) {
                InputMethodManager imm = (InputMethodManager) getActivity().getApplicationContext()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                onClickListener.onNegativeButtonClicked();
                dismiss();
            }
        });

        editText.setText(mBuilder.text);
        editText.setHint(mBuilder.hint);
        editText.setHintTextColor(mBuilder.hintColor);
        editText.setInputType(mBuilder.inputType);
        if (mBuilder.isTextSelected) {
            editText.selectAll();
        }
        editText.requestFocus();
        if (getActivity() != null) {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) getActivity().getApplicationContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }

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
        public void onPositiveButtonClicked(String result) {

        }

        public void onNegativeButtonClicked() {

        }
    }

    public static class Builder implements ITextDialog {
        private Drawable background;

        @Override
        public TextDialog.Builder setBackground(Drawable drawable) {
            background = drawable;
            return this;
        }

        private Drawable editTextBackground;

        @Override
        public TextDialog.Builder setEditTextBackground(Drawable drawable) {
            editTextBackground = drawable;
            return this;
        }

        private int hintColor;

        @Override
        public Builder setHintColor(@ColorInt int color) {
            hintColor = color;
            return this;
        }

        private int textColor;

        @Override
        public Builder setTextColor(@ColorInt int color) {
            textColor = color;
            return this;
        }

        private int lineCount = 1;

        @Override
        public Builder setLineCount(int lines) {
            lineCount = Math.max(lines, 0);
            return this;
        }

        private String title = "";

        @Override
        public TextDialog.Builder setTitle(String text) {
            title = text;
            return this;
        }

        private int titleColor;

        @Override
        public Builder setTitleColor(int color) {
            titleColor = color;
            return this;
        }

        private String text = "";

        @Override
        public TextDialog.Builder setText(String text) {
            this.text = text;
            return this;
        }

        private int textSize = 14;

        @Override
        public Builder setTextSize(int sp) {
            this.textSize = sp;
            return this;
        }

        private boolean isTextSelected = false;

        @Override
        public Builder setTextSelected(boolean isSelected) {
            isTextSelected = isSelected;
            return this;
        }

        private int inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_VARIATION_NORMAL;

        @Override
        public TextDialog.Builder setInputType(int inputType) {
            this.inputType = inputType;
            return this;
        }

        private String positiveText = "";

        @Override
        public TextDialog.Builder setPositiveText(String text) {
            positiveText = text;
            return this;
        }

        private int positiveTextColor = Color.WHITE;

        @Override
        public Builder setPositiveTextColor(int color) {
            positiveTextColor = color;
            return this;
        }

        private Drawable positiveDrawable;

        @Override
        public Builder setPositiveBackground(Drawable drawable) {
            positiveDrawable = drawable;
            return this;
        }

        private String negativeText = "";

        @Override
        public TextDialog.Builder setNegativeText(String text) {
            negativeText = text;
            return this;
        }

        private int negativeTextColor = Color.WHITE;

        @Override
        public Builder setNegativeTextColor(int color) {
            negativeTextColor = color;
            return this;
        }

        private Drawable negativeDrawable;

        @Override
        public Builder setNegativeBackground(Drawable drawable) {
            negativeDrawable = drawable;
            return this;
        }

        private String hint = "";

        @Override
        public TextDialog.Builder setHint(String text) {
            hint = text;
            return this;
        }

        public TextDialog build() {
            return new TextDialog(this);
        }
    }
}
