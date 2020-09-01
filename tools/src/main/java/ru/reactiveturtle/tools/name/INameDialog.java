package ru.reactiveturtle.tools.name;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

interface INameDialog {
    NameDialog.Builder setBackground(Drawable drawable);

    NameDialog.Builder setEditTextBackground(Drawable drawable);

    NameDialog.Builder setHintColor(@ColorInt int color);

    NameDialog.Builder setTextColor(@ColorInt int color);

    NameDialog.Builder setTitle(String text);

    NameDialog.Builder setTitleColor(@ColorInt int color);

    NameDialog.Builder setText(String text);

    NameDialog.Builder setTextSize(int sp);

    NameDialog.Builder setTextSelected(boolean isSelected);

    NameDialog.Builder setInputType(int inputType);

    NameDialog.Builder setPositiveText(String text);

    NameDialog.Builder setPositiveTextColor(@ColorInt int color);

    NameDialog.Builder setPositiveBackground(Drawable drawable);

    NameDialog.Builder setNegativeText(String text);

    NameDialog.Builder setNegativeTextColor(@ColorInt int color);

    NameDialog.Builder setNegativeBackground(Drawable drawable);

    NameDialog.Builder setHint(String text);

}
