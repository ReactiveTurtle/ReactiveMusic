package ru.reactiveturtle.tools.text;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

interface ITextDialog {
    TextDialog.Builder setBackground(Drawable drawable);

    TextDialog.Builder setEditTextBackground(Drawable drawable);

    TextDialog.Builder setHintColor(@ColorInt int color);

    TextDialog.Builder setTextColor(@ColorInt int color);

    TextDialog.Builder setLineCount(int lines);

    TextDialog.Builder setTitle(String text);

    TextDialog.Builder setTitleColor(@ColorInt int color);

    TextDialog.Builder setText(String text);

    TextDialog.Builder setTextSize(int sp);

    TextDialog.Builder setTextSelected(boolean isSelected);

    TextDialog.Builder setInputType(int inputType);

    TextDialog.Builder setPositiveText(String text);

    TextDialog.Builder setPositiveTextColor(@ColorInt int color);

    TextDialog.Builder setPositiveBackground(Drawable drawable);

    TextDialog.Builder setNegativeText(String text);

    TextDialog.Builder setNegativeTextColor(@ColorInt int color);

    TextDialog.Builder setNegativeBackground(Drawable drawable);

    TextDialog.Builder setHint(String text);

}
