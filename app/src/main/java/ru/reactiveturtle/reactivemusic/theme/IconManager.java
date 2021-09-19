package ru.reactiveturtle.reactivemusic.theme;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.Shape;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import java.util.Objects;

import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.toolkit.BitmapExtensions;

import static ru.reactiveturtle.reactivemusic.theme.Theme.changeColor;
import static ru.reactiveturtle.reactivemusic.theme.Theme.isVeryBright;
import static ru.reactiveturtle.reactivemusic.theme.Theme.setAlpha;

public class IconManager {
    private Resources resources;
    private Resources.Theme theme;
    private ThemeContext themeContext;
    private ColorSet colorSet;

    public IconManager(@NonNull Resources resources,
                       @NonNull Resources.Theme theme,
                       @NonNull ThemeContext themeContext) {
        Objects.requireNonNull(resources);
        Objects.requireNonNull(theme);
        Objects.requireNonNull(themeContext);
        this.resources = resources;
        this.theme = theme;
        this.themeContext = themeContext;
    }

    public void setColorSet(@NonNull ColorSet colorSet) {
        Objects.requireNonNull(colorSet);
        this.colorSet = colorSet;
    }

    public Drawable getRoundPlayIcon() {
        return getDefaultRoundButtonDrawable(R.drawable.ic_play);
    }

    public Drawable getRoundPauseIcon() {
        return getDefaultRoundButtonDrawable(R.drawable.ic_pause);
    }

    public Drawable getPreviousIcon() {
        return getDefaultRoundButtonDrawable(R.drawable.ic_previous);
    }

    public Drawable getNextIcon() {
        return getDefaultRoundButtonDrawable(R.drawable.ic_next);
    }

    public Drawable getRepeatIcon() {
        return getCheckDrawable(R.drawable.ic_repeat_all, R.drawable.ic_repeat_one, VectorDrawableCompat.class);
    }

    public Drawable getRandomIcon() {
        return getCheckDrawable(R.drawable.ic_random, R.drawable.ic_random, VectorDrawableCompat.class);
    }

    private Drawable getDefaultRoundButtonDrawable(int drawableId) {
        ShapeDrawable shapeDrawableDefault = new ShapeDrawable();
        shapeDrawableDefault.setShape(new Shape() {
            @Override
            public void draw(Canvas canvas, Paint paint) {
                paint.setColor(setAlpha("8A", colorSet.getPrimary()));
                canvas.drawCircle(canvas.getWidth() / 2f, canvas.getHeight() / 2f,
                        canvas.getWidth() / 2f, paint);
            }
        });
        ShapeDrawable shapeDrawablePressed = new ShapeDrawable();
        shapeDrawablePressed.setShape(new Shape() {
            @Override
            public void draw(Canvas canvas, Paint paint) {
                paint.setColor(setAlpha("8A", colorSet.getPrimaryDark()));
                canvas.drawCircle(canvas.getWidth() / 2f, canvas.getHeight() / 2f,
                        canvas.getWidth() / 2f, paint);
            }
        });
        BitmapDrawable front = toBitmapDrawable(drawableId, 0.6f,
                isVeryBright(colorSet.getPrimary()) ? Color.BLACK : Color.WHITE);
        LayerDrawable layerDrawableDefault = new LayerDrawable(new Drawable[]{shapeDrawableDefault, front});
        LayerDrawable layerDrawablePressed = new LayerDrawable(new Drawable[]{shapeDrawablePressed, front});
        StateListDrawable stateListDrawable = new StateListDrawable();
        stateListDrawable.addState(new int[]{android.R.attr.state_pressed}, layerDrawablePressed);
        stateListDrawable.addState(new int[]{-android.R.attr.state_pressed}, layerDrawableDefault);
        return stateListDrawable;
    }

    private BitmapDrawable toBitmapDrawable(@DrawableRes int vectorDrawableId, float scale, int color) {
        BitmapDrawable front = null;
        VectorDrawableCompat drawable = VectorDrawableCompat.create(resources, vectorDrawableId, theme);
        if (drawable != null) {
            Bitmap bitmap = BitmapExtensions.drawableToBitmap(drawable);
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
            front = new BitmapDrawable(resources, result);
            front.setColorFilter(new LightingColorFilter(Color.BLACK, color));
        }
        return front;
    }

    @NonNull
    public Drawable getCheckDrawable(@DrawableRes int drawableIdFalse,
                                     @DrawableRes int drawableIdTrue,
                                     Class<? extends Drawable> c) {
        Drawable back = ResourcesCompat.getDrawable(
                resources, R.drawable.player_default_button, theme);
        Drawable frontFalse = null;
        Drawable frontTrue = null;
        if (c.equals(BitmapDrawable.class)) {
            frontFalse = ResourcesCompat.getDrawable(resources, drawableIdFalse, theme);
            frontTrue = ResourcesCompat.getDrawable(resources, drawableIdTrue, theme);
            if (frontFalse != null) {
                frontFalse.mutate();
                changeColor(frontFalse,
                        setAlpha("8A", themeContext.getNegativePrimary()));
            }
            if (frontTrue != null) {
                frontTrue.mutate();
                changeColor(frontTrue, colorSet.getPrimary());
            }
        } else if (c.equals(VectorDrawableCompat.class)) {
            frontFalse = VectorDrawableCompat.create(resources, drawableIdFalse, theme);
            frontTrue = VectorDrawableCompat.create(resources, drawableIdTrue, theme);
            if (frontFalse != null) {
                frontFalse.mutate();
                ((VectorDrawableCompat) frontFalse).setTintList(ColorStateList.valueOf(
                        setAlpha("8A", themeContext.getNegativePrimary())));
            }
            if (frontTrue != null) {
                frontTrue.mutate();
                Bitmap bitmap = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                canvas.drawColor(colorSet.getPrimary());
                ((VectorDrawableCompat) frontTrue).setTintList(ColorStateList.valueOf(colorSet.getPrimary()));
            }
        }

        LayerDrawable layerDrawableFalse = new LayerDrawable(new Drawable[]{back, frontFalse});
        LayerDrawable layerDrawableTrue = new LayerDrawable(new Drawable[]{back, frontTrue});
        StateListDrawable stateListDrawable = new StateListDrawable();

        stateListDrawable.addState(new int[]{android.R.attr.state_checked}, layerDrawableTrue);
        stateListDrawable.addState(new int[]{-android.R.attr.state_checked}, layerDrawableFalse);
        return stateListDrawable;
    }
}
