package ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.widget.SeekBar;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.tools.WaitDailog;
import ru.reactiveturtle.tools.text.TextDialog;

public class Theme {
    private static Resources RESOURCES;

    public static int CONTEXT_PRIMARY;
    public static int CONTEXT_PRIMARY_LIGHT;
    public static int CONTEXT_LIGHT;
    public static int CONTEXT_NEGATIVE_PRIMARY;
    public static int CONTEXT_NEGATIVE_SECONDARY;
    public static int CONTEXT_SECONDARY_TEXT;
    public static boolean IS_DARK = false;
    public static boolean IS_NEON_ENABLED = false;

    public static void init(@NonNull Resources resources, @NonNull ColorSet colorSet, boolean isDark) {
        RESOURCES = resources;
        DEFAULT_ALBUM_COVER = Helper.getDefaultAlbumCover(resources);
        Theme.colorSet = colorSet;

        int width = resources.getDisplayMetrics().widthPixels;
        Bitmap bitmap = Bitmap.createBitmap(width, 1, Bitmap.Config.ARGB_8888);
        mProgressDrawable = new BitmapDrawable(resources, bitmap);

        updateContext(isDark);
        update(colorSet, 0);
    }

    public static void updateContext(boolean isDark) {
        IS_DARK = isDark;
        if (IS_DARK) {
            CONTEXT_PRIMARY = Color.parseColor("#000000");
            CONTEXT_PRIMARY_LIGHT = Color.parseColor("#121212");
            CONTEXT_LIGHT = Color.parseColor("#323232");
            CONTEXT_NEGATIVE_PRIMARY = Color.parseColor("#FFFFFF");
            CONTEXT_NEGATIVE_SECONDARY = Color.parseColor("#BEBEBE");
        } else {
            CONTEXT_PRIMARY = Color.parseColor("#FFFFFF");
            CONTEXT_PRIMARY_LIGHT = Color.parseColor("#F5F5F5");
            CONTEXT_LIGHT = Color.parseColor("#DDDDDD");
            CONTEXT_NEGATIVE_PRIMARY = Color.parseColor("#000000");
            CONTEXT_NEGATIVE_SECONDARY = Color.parseColor("#323232");
        }
        CONTEXT_SECONDARY_TEXT = Theme.setAlpha("8a", Theme.CONTEXT_NEGATIVE_PRIMARY);
        updateDefaultAlbumCover();
    }

    private static BitmapDrawable DEFAULT_ALBUM_COVER = new BitmapDrawable();

    @NonNull
    public static BitmapDrawable getDefaultAlbumCover() {
        return DEFAULT_ALBUM_COVER;
    }

    @NonNull
    public static BitmapDrawable getDefaultAlbumCoverCopy() {
        return new BitmapDrawable(RESOURCES, DEFAULT_ALBUM_COVER.getBitmap());
    }

    private static ColorSet colorSet = new ColorSet(Color.parseColor("#0039cb")
            + "|" + Color.parseColor("#2962ff")
            + "|" + Color.parseColor("#768fff"));

    public static void update(@NonNull ColorSet colorSet,
                              @FloatRange(from = 0, to = 1) float trackProgress) {
        Theme.colorSet = colorSet;
        updateDefaultAlbumCover();
        updateProgressDrawable(trackProgress);
    }

    public static void updateDefaultAlbumCover() {
        Bitmap note = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(RESOURCES, R.drawable.ic_note),
                256, 256, true);
        Canvas canvas = new Canvas(note);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Theme.CONTEXT_PRIMARY);
        int backColor = colorSet.getPrimary();
        if (isVeryBright(backColor)) {
            paint.setColor(Color.BLACK);
        } else {
            paint.setColor(Color.WHITE);
        }
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawRect(0, 0, 256, 256, paint);


        paint.setXfermode(null);
        Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(DEFAULT_ALBUM_COVER.getBitmap());

        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        paint.setColor(backColor);
        canvas.drawRoundRect(new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight()),
                0, 0, paint);
        canvas.drawBitmap(note, (bitmap.getWidth() - note.getWidth()) / 2f,
                (bitmap.getHeight() - note.getHeight()) / 2f, paint);
    }

    private static BitmapDrawable mProgressDrawable;

    public static BitmapDrawable getProgressDrawable() {
        return mProgressDrawable;
    }

    public static void updateProgressDrawable(@FloatRange(from = 0, to = 1) float progressPercent) {
        int backColor = Theme.setAlpha("60", getColorSet().getPrimaryLight());
        int progressColor = Theme.setAlpha("28", getColorSet().getPrimaryLight());
        int width = RESOURCES.getDisplayMetrics().widthPixels;
        Canvas canvas = new Canvas(mProgressDrawable.getBitmap());
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(backColor);
        canvas.drawRect(0, 0, width, 1, paint);
        paint.setColor(progressColor);
        canvas.drawRect(0, 0, width * progressPercent, 1, paint);
    }

    public static ColorSet getColorSet() {
        return colorSet;
    }

    @Nullable
    public static List<ColorSet> getColors(@IntRange(from = 0, to = 13) int brightness) {
        String palette = null;
        switch (brightness) {
            case 0:
                palette = ColorPalette.M_50;
                break;
            case 1:
                palette = ColorPalette.M_100;
                break;
            case 2:
                palette = ColorPalette.M_200;
                break;
            case 3:
                palette = ColorPalette.M_300;
                break;
            case 4:
                palette = ColorPalette.M_400;
                break;
            case 5:
                palette = ColorPalette.M_500;
                break;
            case 6:
                palette = ColorPalette.M_600;
                break;
            case 7:
                palette = ColorPalette.M_700;
                break;
            case 8:
                palette = ColorPalette.M_800;
                break;
            case 9:
                palette = ColorPalette.M_900;
                break;
            case 10:
                palette = ColorPalette.M_A100;
                break;
            case 11:
                palette = ColorPalette.M_A200;
                break;
            case 12:
                palette = ColorPalette.M_A400;
                break;
            case 13:
                palette = ColorPalette.M_A700;
                break;
        }
        if (palette != null) {
            List<ColorSet> colors = new ArrayList<>();
            String[] paletteColors = palette.split("\\|");
            for (int i = 0; i < paletteColors.length; i += 3) {
                colors.add(new ColorSet(
                        Color.parseColor("#" + paletteColors[i]),
                        Color.parseColor("#" + paletteColors[i + 1]),
                        Color.parseColor("#" + paletteColors[i + 2])));
            }
            return colors;
        }
        return null;
    }

    @NonNull
    public static Drawable changeColor(@NonNull Drawable drawable, int newColor) {
        drawable.setColorFilter(new LightingColorFilter(Color.BLACK, newColor));
        return drawable;
    }

    public static boolean isGrey(int color) {
        return Color.red(color) == Color.green(color) && Color.red(color) == Color.blue(color);
    }

    public static int setAlpha(String hexAlpha, int color) {
        return Color.parseColor("#" + hexAlpha
                + Integer.toHexString(color).substring(2));
    }

    public static boolean isVeryBright(int color) {
        return Color.green(color) >= 232;
    }

    public static void updateFab(FloatingActionButton fab) {
        fab.setBackgroundTintList(ColorStateList.valueOf(
                Theme.getColorSet().getPrimaryLight()));
        if (Theme.isVeryBright(Theme.getColorSet().getPrimaryLight())) {
            fab.setSupportImageTintList(ColorStateList.valueOf(Color.BLACK));
        } else {
            fab.setSupportImageTintList(ColorStateList.valueOf(Color.WHITE));
        }
    }

    public static void updateSeekBar(SeekBar seekBar) {
        int cursorColor = Theme.getColorSet().getPrimary();
        int seekBarColor = Theme.getColorSet().getPrimaryDark();
        if (IS_DARK) {
            cursorColor = Theme.getColorSet().getPrimaryLight();
            seekBarColor = Theme.getColorSet().getPrimary();
        }
        if (isGrey(cursorColor)) {
            cursorColor = Theme.CONTEXT_NEGATIVE_PRIMARY;
            seekBarColor = Theme.CONTEXT_NEGATIVE_SECONDARY;
        } else if (!IS_DARK && isVeryBright(cursorColor)) {
            cursorColor = Theme.CONTEXT_NEGATIVE_PRIMARY;
            seekBarColor = Theme.CONTEXT_NEGATIVE_SECONDARY;
        }
        Theme.changeColor(seekBar.getThumb(),
                cursorColor);
        Theme.changeColor(seekBar.getProgressDrawable(),
                seekBarColor);
    }

    @NonNull
    public static Drawable getColorSetDrawable(@NonNull ColorSet colorSet) {
        Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        Bitmap colorSetBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        Canvas canvas = new Canvas(colorSetBitmap);

        float y = 128;
        Path path = new Path();
        path.moveTo(256, 256);
        path.lineTo(0, y);
        path.lineTo(0, 0);
        path.lineTo(512, 0);
        path.lineTo(512, y);
        path.lineTo(256, 256);
        paint.setColor(colorSet.getPrimaryDark());
        canvas.drawPath(path, paint);

        path = new Path();
        path.moveTo(256, 256);
        path.lineTo(256, 512);
        path.lineTo(0, 512);
        path.lineTo(0, y);
        path.lineTo(256, 256);
        paint.setColor(colorSet.getPrimary());
        canvas.drawPath(path, paint);

        path = new Path();
        path.moveTo(256, 256);
        path.lineTo(512, y);
        path.lineTo(512, 512);
        path.lineTo(256, 512);
        path.lineTo(256, 256);
        paint.setColor(colorSet.getPrimaryLight());
        canvas.drawPath(path, paint);

        canvas = new Canvas(bitmap);
        paint.setColor(Color.BLACK);
        canvas.drawCircle(256, 256, 256, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(colorSetBitmap, 0, 0, paint);
        paint.setXfermode(null);

        paint.setColor(CONTEXT_NEGATIVE_SECONDARY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        canvas.drawCircle(256, 256, 252, paint);

        return new BitmapDrawable(RESOURCES, bitmap);
    }

    @NonNull
    public static Drawable getDefaultButtonDrawable(@DrawableRes int drawableId) {
        Drawable back = ResourcesCompat.getDrawable(
                RESOURCES, R.drawable.player_default_button, null);
        VectorDrawableCompat front = VectorDrawableCompat.create(RESOURCES, drawableId, null);
        if (front != null) {
            front.setTint(CONTEXT_NEGATIVE_PRIMARY);
        }
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{back, front});
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, layerDrawable);
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, front);
        return stateListDrawable;
    }

    @NonNull
    public static Drawable getCheckDrawable(@DrawableRes int drawableIdFalse, @DrawableRes int drawableIdTrue,
                                            Class<? extends Drawable> c) {
        Drawable back = ResourcesCompat.getDrawable(
                RESOURCES, R.drawable.player_default_button, null);
        Drawable frontFalse = null;
        Drawable frontTrue = null;
        if (c.equals(BitmapDrawable.class)) {
            frontFalse = ResourcesCompat.getDrawable(RESOURCES, drawableIdFalse, null);
            frontTrue = ResourcesCompat.getDrawable(RESOURCES, drawableIdTrue, null);
            if (frontFalse != null) {
                frontFalse.mutate();
                changeColor(frontFalse,
                        setAlpha("8A", CONTEXT_NEGATIVE_PRIMARY));
            }
            if (frontTrue != null) {
                frontTrue.mutate();
                changeColor(frontTrue, getColorSet().getPrimary());
            }
        } else if (c.equals(VectorDrawableCompat.class)) {
            frontFalse = VectorDrawableCompat.create(RESOURCES, drawableIdFalse, null);
            frontTrue = VectorDrawableCompat.create(RESOURCES, drawableIdTrue, null);
            if (frontFalse != null) {
                frontFalse.mutate();
                ((VectorDrawableCompat) frontFalse).setTint(
                        setAlpha("8A", CONTEXT_NEGATIVE_PRIMARY));
            }
            if (frontTrue != null) {
                frontTrue.mutate();
                ((VectorDrawableCompat) frontTrue).setTint(getColorSet().getPrimary());
            }
        }

        LayerDrawable layerDrawableFalse = new LayerDrawable(new Drawable[]{back, frontFalse});
        LayerDrawable layerDrawableTrue = new LayerDrawable(new Drawable[]{back, frontTrue});
        StateListDrawable stateListDrawable = new StateListDrawable();

        stateListDrawable.addState(new int[]{android.R.attr.state_checked}, layerDrawableTrue);
        stateListDrawable.addState(new int[]{-android.R.attr.state_checked}, layerDrawableFalse);
        return stateListDrawable;
    }

    @NonNull
    public static Drawable getButtonDrawable(@ColorInt int backColor) {
        GradientDrawable back = new GradientDrawable();
        back.setCornerRadius(RESOURCES.getDimensionPixelSize(R.dimen.big));
        back.setColor(backColor);

        GradientDrawable front = new GradientDrawable();
        front.setCornerRadius(RESOURCES.getDimensionPixelSize(R.dimen.big));
        front.setColor(Color.parseColor("#42424242"));

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{back, front});
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, layerDrawable);
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, back);
        return stateListDrawable;
    }

    @NonNull
    public static TextDialog.Builder getNameDialogBuilder() {
        return new TextDialog.Builder()
                .setBackground(new ColorDrawable(Theme.IS_DARK ? Theme.CONTEXT_PRIMARY_LIGHT : Theme.CONTEXT_PRIMARY))
                .setEditTextBackground(Helper.getRoundDrawable(Theme.CONTEXT_LIGHT,
                        RESOURCES.getDimensionPixelSize(R.dimen.big)))
                .setHintColor(setAlpha("8a", Theme.CONTEXT_NEGATIVE_SECONDARY))
                .setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY)
                .setTitleColor(Theme.CONTEXT_NEGATIVE_PRIMARY)
                .setNegativeTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY)
                .setNegativeBackground(Theme.getButtonDrawable(Theme.CONTEXT_LIGHT))
                .setPositiveTextColor(Theme.isVeryBright(Theme.getColorSet().getPrimary()) ?
                        Color.BLACK : Color.WHITE)
                .setPositiveBackground(Theme.getButtonDrawable(Theme.getColorSet().getPrimary()));
    }

    public static WaitDailog.Builder getWaitDialogBuilder() {
        return new WaitDailog.Builder()
                .setBackground(new ColorDrawable(Theme.IS_DARK ? Theme.CONTEXT_LIGHT : Theme.CONTEXT_PRIMARY))
                .setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY)
                .setProgressColor(!IS_DARK && Theme.isVeryBright(Theme.getColorSet().getPrimary()) ?
                        Color.BLACK : Theme.getColorSet().getPrimary());
    }
}
