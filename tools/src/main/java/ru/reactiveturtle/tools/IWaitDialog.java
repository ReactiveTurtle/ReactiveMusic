package ru.reactiveturtle.tools;

import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

interface IWaitDialog {
    WaitDailog.Builder setBackground(@Nullable Drawable drawable);

    WaitDailog.Builder setTextColor(@ColorInt int color);

    WaitDailog.Builder setProgressColor(@ColorInt int color);
}
