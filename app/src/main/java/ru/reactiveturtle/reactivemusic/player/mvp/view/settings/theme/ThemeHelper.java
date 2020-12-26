package ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.Shape;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.R;

import static ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme.CONTEXT_NEGATIVE_PRIMARY;
import static ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme.RESOURCES;
import static ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme.getColorSet;
import static ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme.Theme.isVeryBright;

public class ThemeHelper {
    public static int setAlpha(String hexAlpha, int color) {
        return Color.parseColor("#" + hexAlpha
                + Integer.toHexString(color).substring(2));
    }

    @NonNull
    public static Drawable changeColor(@NonNull Drawable drawable, int newColor) {
        drawable.setColorFilter(new LightingColorFilter(Color.BLACK, newColor));
        return drawable;
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

        paint.setColor(Theme.CONTEXT_NEGATIVE_SECONDARY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        canvas.drawCircle(256, 256, 252, paint);

        return new BitmapDrawable(RESOURCES, bitmap);
    }

    @NonNull
    public static Drawable getDefaultButtonDrawable(@DrawableRes int drawableId) {
        ShapeDrawable back = new ShapeDrawable();
        back.setShape(new Shape() {
            @Override
            public void draw(Canvas canvas, Paint paint) {
                paint.setColor(Color.GRAY);
                canvas.drawCircle(canvas.getWidth() / 2f, canvas.getHeight() / 2f,
                        canvas.getWidth() / 2f, paint);
            }
        });
        BitmapDrawable front = toBitmapDrawable(drawableId, 0.75f, CONTEXT_NEGATIVE_PRIMARY);
        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{back, front});
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, layerDrawable);
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, front);
        return stateListDrawable;
    }

    public static Drawable getDefaultRoundButtonDrawable(int drawableId) {
        ShapeDrawable shapeDrawableDefault = new ShapeDrawable();
        shapeDrawableDefault.setShape(new Shape() {
            @Override
            public void draw(Canvas canvas, Paint paint) {
                paint.setColor(getColorSet().getPrimary());
                canvas.drawCircle(canvas.getWidth() / 2f, canvas.getHeight() / 2f,
                        canvas.getWidth() / 2f, paint);
            }
        });
        ShapeDrawable shapeDrawablePressed = new ShapeDrawable();
        shapeDrawablePressed.setShape(new Shape() {
            @Override
            public void draw(Canvas canvas, Paint paint) {
                paint.setColor(getColorSet().getPrimaryDark());
                canvas.drawCircle(canvas.getWidth() / 2f, canvas.getHeight() / 2f,
                        canvas.getWidth() / 2f, paint);
            }
        });
        BitmapDrawable front = toBitmapDrawable(drawableId, 0.6f,
                isVeryBright(getColorSet().getPrimary()) ? Color.BLACK : Color.WHITE);
        LayerDrawable layerDrawableDefault = new LayerDrawable(new Drawable[]{shapeDrawableDefault, front});
        LayerDrawable layerDrawablePressed = new LayerDrawable(new Drawable[]{shapeDrawablePressed, front});
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, layerDrawablePressed);
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, layerDrawableDefault);
        return stateListDrawable;
    }

    private static BitmapDrawable toBitmapDrawable(int vectorDrawableId, float scale, int color) {
        BitmapDrawable front = null;
        VectorDrawableCompat drawable = VectorDrawableCompat.create(RESOURCES, vectorDrawableId, null);
        if (RESOURCES != null && drawable != null) {
            Bitmap bitmap = Helper.drawableToBitmap(drawable);
            Bitmap result = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            bitmap = Bitmap.createScaledBitmap(bitmap,
                    (int) (bitmap.getWidth() * scale), (int) (bitmap.getHeight() * scale), false);
            float leftPadding = Math.round((canvas.getWidth() - bitmap.getWidth()) / 2f);
            float topPadding = Math.round((canvas.getHeight() - bitmap.getHeight()) / 2f);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.WHITE);
            canvas.drawRect(leftPadding,
                    topPadding, result.getWidth() - leftPadding,
                    result.getHeight() - topPadding, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            canvas.drawBitmap(bitmap, leftPadding, topPadding, paint);
            front = new BitmapDrawable(RESOURCES, result);
            front.setColorFilter(new LightingColorFilter(Color.BLACK,
                    color));
        }
        return front;
    }

    @Nullable
    public static Drawable getCheckDrawable(@DrawableRes int drawableIdFalse, @DrawableRes int drawableIdTrue,
                                            Class<? extends Drawable> c) {
        if (RESOURCES == null) {
            return null;
        }
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

    @Nullable
    public static Drawable getButtonDrawable(@ColorInt int backColor) {
        if (RESOURCES == null) {
            return null;
        }
        GradientDrawable back = new GradientDrawable();
        back.setCornerRadius(RESOURCES.getDimensionPixelSize(R.dimen.big));
        back.setColor(backColor);

        GradientDrawable front = new GradientDrawable();
        front.setCornerRadius(RESOURCES.getDimensionPixelSize(R.dimen.big));
        front.setColor(Color.parseColor("#42000000"));

        LayerDrawable layerDrawable = new LayerDrawable(new Drawable[]{back, front});
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, layerDrawable);
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, back);
        return stateListDrawable;
    }
}
