package ru.reactiveturtle.reactivemusic.toolkit;

import android.os.Build;

import java.util.Locale;

public class UnsupportedApiException extends RuntimeException {
    public UnsupportedApiException(int requiredMinApi) {
        super(String.format(
                Locale.ENGLISH,
                "This method used in api lower than %d. Device Api version is %d",
                requiredMinApi,
                Build.VERSION.SDK_INT));

    }
}
