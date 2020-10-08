package ru.reactiveturtle.tools.widget.warning;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;

public interface IMessageDialog {
    MessageDialog.Builder setBackground(Drawable drawable);

    MessageDialog.Builder setTitle(String text);

    MessageDialog.Builder setTitleColor(@ColorInt int color);

    MessageDialog.Builder setPositiveText(String text);

    MessageDialog.Builder setPositiveTextColor(@ColorInt int color);

    MessageDialog.Builder setPositiveBackground(Drawable drawable);

    MessageDialog.Builder setNegativeText(String text);

    MessageDialog.Builder setNegativeTextColor(@ColorInt int color);

    MessageDialog.Builder setNegativeBackground(Drawable drawable);
}
