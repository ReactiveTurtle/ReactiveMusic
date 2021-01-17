package ru.reactiveturtle.reactivemusic.player;

import androidx.annotation.Nullable;

import ru.reactiveturtle.reactivemusic.player.service.MusicModel;
import ru.reactiveturtle.tools.reactiveuvm.ReactiveArchitect;

public class GlobalModel {
    public static void updateTrackText(@Nullable MusicInfo data) {
        if (data != null) {
            MusicModel.setCurrentTrackName(data.getTitle());
            MusicModel.setCurrentTrackArtist(data.getArtist());
            MusicModel.setCurrentTrackAlbum(data.getAlbum());
        }
    }
}
