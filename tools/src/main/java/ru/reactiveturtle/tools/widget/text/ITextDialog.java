package ru.reactiveturtle.tools.widget.text;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

import ru.reactiveturtle.tools.widget.warning.IMessageDialog;

interface ITextDialog extends IMessageDialog {
    TextDialog.Builder setEditTextBackground(Drawable drawable);

    TextDialog.Builder setHintColor(@ColorInt int color);

    TextDialog.Builder setTextColor(@ColorInt int color);

    TextDialog.Builder setLineCount(int lines);

    TextDialog.Builder setText(String text);

    TextDialog.Builder setTextSize(int sp);

    TextDialog.Builder setTextSelected(boolean isSelected);

    TextDialog.Builder setInputType(int inputType);

    TextDialog.Builder setHint(String text);

}
