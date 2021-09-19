package ru.reactiveturtle.reactivemusic.player;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import ru.reactiveturtle.reactivemusic.theme.ColorSet;
import ru.reactiveturtle.reactivemusic.theme.ColorType;
import ru.reactiveturtle.reactivemusic.theme.MaterialColorPalette;

public class BackgroundGradientDrawable extends GradientDrawable {
    public BackgroundGradientDrawable(ColorSet colorSet) {
        super(getRandomOrientation(), getGradientColors(colorSet));
    }

    private static Map<ColorType, Integer[][]> gradientColorVariantsMap = new HashMap<>();

    static {
        Map<ColorType, Integer[][]> gradientColorVariantsMap = new HashMap<>();
        gradientColorVariantsMap.put(ColorType.GREEN, new Integer[][]{
                new Integer[]{},
                new Integer[]{},
                new Integer[]{},
        });
        BackgroundGradientDrawable.gradientColorVariantsMap = Collections.unmodifiableMap(gradientColorVariantsMap);
    }

    private static int[] getGradientColors(ColorSet colorSet) {
        ColorType colorType = MaterialColorPalette.findColorType(colorSet);
        Integer[][] variants = gradientColorVariantsMap.get(colorType);
        if (colorType == null || variants == null || variants.length == 0) {
            return new int[]{Color.CYAN, Color.MAGENTA};
        }

        int randomVariantIndex = (int) Math.round(Math.random() * (variants.length - 1));
        Integer[] variant = variants[randomVariantIndex];
        if (variant.length == 0) {
            return new int[]{Color.CYAN, Color.MAGENTA};
        }

        int[] variantsAsPrimitiveArray = new int[variant.length];
        for (int i = 0; i < variant.length; i++) {
            variantsAsPrimitiveArray[i] = variant[i];
        }

        return variantsAsPrimitiveArray;
    }

    private static Orientation getRandomOrientation() {
        Random random = new Random();
        int orientation = Math.round(random.nextFloat() * 3f);
        switch (orientation) {
            case 0:
                return Orientation.TL_BR;
            case 1:
                return Orientation.TR_BL;
            case 2:
                return Orientation.BR_TL;
            case 3:
                return Orientation.BL_TR;
        }
        throw new RuntimeException("Unhandled value " + orientation);
    }
}
