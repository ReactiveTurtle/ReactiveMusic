package ru.reactiveturtle.reactivemusic.player;

import androidx.annotation.Nullable;

import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;

public class GlobalModel {
    public static void updateTrackText(@Nullable MusicInfo data) {
        if (data != null) {
            ReactiveArchitect.changeState("CURRENT_TRACK_NAME", data.getTitle());
            ReactiveArchitect.changeState("CURRENT_TRACK_ARTIST", data.getArtist());
            ReactiveArchitect.changeState("CURRENT_TRACK_ALBUM", data.getAlbum());
        }
    }
}
