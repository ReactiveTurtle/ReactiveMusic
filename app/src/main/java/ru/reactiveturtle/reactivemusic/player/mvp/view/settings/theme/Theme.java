package ru.reactiveturtle.reactivemusic.player.mvp.view.settings.theme;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import ru.reactiveturtle.reactivemusic.Helper;
import ru.reactiveturtle.reactivemusic.R;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerModel;
import ru.reactiveturtle.reactivemusic.player.service.MusicModel;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;
import ru.reactiveturtle.tools.reactiveuvm.StateKeeper;
import ru.reactiveturtle.tools.widget.wait.WaitDailog;
import ru.reactiveturtle.tools.widget.text.TextDialog;
import ru.reactiveturtle.tools.widget.warning.MessageDialog;

public class Theme {
    static Resources RESOURCES;

    public static int CONTEXT_PRIMARY;
    public static int CONTEXT_PRIMARY_LIGHT;
    public static int CONTEXT_LIGHT;
    public static int CONTEXT_NEGATIVE_PRIMARY;
    public static int CONTEXT_NEGATIVE_SECONDARY;
    public static int CONTEXT_SECONDARY_TEXT;
    public static boolean IS_NEON_ENABLED = false;

    public static final String COLOR_SET = "COLOR_SET";
    public static final String IS_DARK = "IS_DARK";

    public static void init(@NonNull Resources resources, @NonNull ColorSet colorSet, boolean isDark) {
        RESOURCES = resources;
        DEFAULT_ALBUM_COVER = Helper.getDefaultAlbumCover(resources);

        int width = resources.getDisplayMetrics().widthPixels;
        Bitmap bitmap = Bitmap.createBitmap(width, 1, Bitmap.Config.ARGB_8888);
        mProgressDrawable = new BitmapDrawable(resources, bitmap);

        ReactiveArchitect.createState(COLOR_SET, colorSet).subscribe((view, value) -> {
            update((ColorSet) value, 0);
        }).call();
        ReactiveArchitect.createState(IS_DARK, isDark).subscribe((view, value) -> updateContext((Boolean) value)).call();

    }

    public static void switchThemeContext() {
        ReactiveArchitect.getStateKeeper(IS_DARK).changeState(!(Boolean) ReactiveArchitect.getStateKeeper(IS_DARK).getState());
    }

    public static boolean isDark() {
        return (boolean) ReactiveArchitect.getStateKeeper(IS_DARK).getState();
    }

    public static void updateContext(boolean isDark) {
        if (isDark) {
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
        CONTEXT_SECONDARY_TEXT = ThemeHelper.setAlpha("8a", Theme.CONTEXT_NEGATIVE_PRIMARY);
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
        int backColor = ThemeHelper.setAlpha("60", getColorSet().getPrimaryLight());
        int progressColor = ThemeHelper.setAlpha("28", getColorSet().getPrimaryLight());
        int width = RESOURCES.getDisplayMetrics().widthPixels;
        Canvas canvas = new Canvas(mProgressDrawable.getBitmap());
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(backColor);
        canvas.drawRect(0, 0, width, 1, paint);
        paint.setColor(progressColor);
        canvas.drawRect(0, 0, width * progressPercent, 1, paint);
    }

    public static void setColorSet(ColorSet colorSet) {
        ReactiveArchitect.getStateKeeper(COLOR_SET).changeState(colorSet);
    }

    public static ColorSet getColorSet() {
        return (ColorSet) ReactiveArchitect.getStateKeeper(COLOR_SET).getState();
    }

    @NonNull
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
            default:
                palette = ColorPalette.M_A700;
                break;
        }
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

    public static boolean isGrey(int color) {
        return Color.red(color) == Color.green(color) && Color.red(color) == Color.blue(color);
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
        if (isDark()) {
            cursorColor = Theme.getColorSet().getPrimaryLight();
            seekBarColor = Theme.getColorSet().getPrimary();
        }
        if (isGrey(cursorColor)) {
            cursorColor = Theme.CONTEXT_NEGATIVE_PRIMARY;
            seekBarColor = Theme.CONTEXT_NEGATIVE_SECONDARY;
        } else if (!isDark() && isVeryBright(cursorColor)) {
            cursorColor = Theme.CONTEXT_NEGATIVE_PRIMARY;
            seekBarColor = Theme.CONTEXT_NEGATIVE_SECONDARY;
        }
        ThemeHelper.changeColor(seekBar.getThumb(),
                cursorColor);
        ThemeHelper.changeColor(seekBar.getProgressDrawable(),
                seekBarColor);
    }

    @NonNull
    public static TextDialog.Builder getNameDialogBuilder() {
        return new TextDialog.Builder()
                .setBackground(new ColorDrawable(Theme.isDark() ? Theme.CONTEXT_PRIMARY_LIGHT : Theme.CONTEXT_PRIMARY))
                .setEditTextBackground(Helper.getRoundDrawable(Theme.CONTEXT_LIGHT,
                        RESOURCES.getDimensionPixelSize(R.dimen.big)))
                .setHintColor(ThemeHelper.setAlpha("8a", Theme.CONTEXT_NEGATIVE_SECONDARY))
                .setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY)
                .setTitleColor(Theme.CONTEXT_NEGATIVE_PRIMARY)
                .setNegativeTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY)
                .setNegativeBackground(ThemeHelper.getButtonDrawable(Theme.CONTEXT_LIGHT))
                .setPositiveTextColor(Theme.isVeryBright(Theme.getColorSet().getPrimary()) ?
                        Color.BLACK : Color.WHITE)
                .setPositiveBackground(ThemeHelper.getButtonDrawable(Theme.getColorSet().getPrimary()));
    }

    @NonNull
    public static MessageDialog.Builder getMessageDialogBuilder() {
        return new MessageDialog.Builder()
                .setBackground(new ColorDrawable(Theme.isDark() ? Theme.CONTEXT_PRIMARY_LIGHT : Theme.CONTEXT_PRIMARY))
                .setTitleColor(Theme.CONTEXT_NEGATIVE_PRIMARY)
                .setNegativeTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY)
                .setNegativeBackground(ThemeHelper.getButtonDrawable(Theme.CONTEXT_LIGHT))
                .setPositiveTextColor(Theme.isVeryBright(Theme.getColorSet().getPrimary()) ?
                        Color.BLACK : Color.WHITE)
                .setPositiveBackground(ThemeHelper.getButtonDrawable(Theme.getColorSet().getPrimary()));
    }

    public static WaitDailog.Builder getWaitDialogBuilder() {
        return new WaitDailog.Builder()
                .setBackground(new ColorDrawable(Theme.isDark() ? Theme.CONTEXT_LIGHT : Theme.CONTEXT_PRIMARY))
                .setTextColor(Theme.CONTEXT_NEGATIVE_PRIMARY)
                .setProgressColor(!isDark() && Theme.isVeryBright(Theme.getColorSet().getPrimary()) ?
                        Color.BLACK : Theme.getColorSet().getPrimary());
    }
}
