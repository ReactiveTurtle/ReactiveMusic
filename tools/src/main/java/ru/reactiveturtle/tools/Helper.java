package ru.reactiveturtle.tools;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;

import java.util.List;

public class Helper {
    public static int getThemeColor(Context context, int attribute) {
        TypedValue typedValue = new TypedValue();

        TypedArray a = context.obtainStyledAttributes(typedValue.data, new int[]{attribute});
        int color = a.getColor(0, 0);

        a.recycle();
        return color;
    }

    public static float unitToPixels(int dp, int dimenType, Resources resources) {
        return TypedValue.applyDimension(dimenType, dp, resources.getDisplayMetrics());
    }
}
