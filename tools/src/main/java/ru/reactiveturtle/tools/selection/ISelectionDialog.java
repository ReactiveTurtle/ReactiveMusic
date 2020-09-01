package ru.reactiveturtle.tools.selection;

import android.graphics.drawable.Drawable;
import android.text.Spannable;

import androidx.annotation.ColorInt;

interface ISelectionDialog {
    SelectionDialog.Builder setBackground(Drawable drawable);

    SelectionDialog.Builder setTitle(String title);

    SelectionDialog.Builder setTitleColor(int color);

    SelectionDialog.Builder addItems(Spannable... items);

    SelectionDialog.Builder setPosition(int position);

    SelectionDialog.Builder setDividerColor(@ColorInt int color);
}
