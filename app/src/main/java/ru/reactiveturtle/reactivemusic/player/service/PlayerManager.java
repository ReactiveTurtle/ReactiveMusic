package ru.reactiveturtle.reactivemusic.player.service;

import java.io.File;
import java.util.List;

import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerModel;
import ru.reactiveturtle.reactivemusic.player.mvp.model.PlayerRepository;

public class PlayerManager {
    private PlayerRepository mRepository;

    public PlayerManager(PlayerRepository repository) {
        mRepository = repository;
    }

    public String getNextTrack() {
        if (PlayerModel.isPlayRandomTrack()) {
            return getRandomTrackPath();
        } else {
            return getNextTrackOnList();
        }
    }

    public String getPreviousTrack() {
        if (PlayerModel.isPlayRandomTrack()) {
            return getRandomTrackPath();
        } else {
            return getPreviousTrackOnList();
        }
    }

    private String getRandomTrackPath() {
        List<String> paths = mRepository.getPlaylist(mRepository.getCurrentPlaylist());
        int newPosition = (int) (Math.random() * (paths.size() - 1));
        String path = paths.get(newPosition);
        return path;
    }

    public String getPreviousTrackOnList() {
        MusicModel.setTrackPlay(true);
        List<String> allTracks =
                mRepository.getPlaylist(mRepository.getCurrentPlaylist());
        if (allTracks.size() > 0) {
            int currentMusicTrack = -1;
            for (int i = 0; i < allTracks.size(); i++) {
                if (allTracks.get(i).equals(MusicModel.getCurrentTrackPath())) {
                    currentMusicTrack = i;
                    break;
                }
            }
            int newTrackIndex = currentMusicTrack - 1 < 0 ? allTracks.size() - 1 : currentMusicTrack - 1;
            File file;
            while (!(file = new File(allTracks.get(newTrackIndex))).exists()) {
                newTrackIndex = newTrackIndex - 1 < 0 ? allTracks.size() - 1 : newTrackIndex - 1;
            }
            return file.getAbsolutePath();
        }
        return null;
    }

    public String getNextTrackOnList() {
        List<String> allTracks = mRepository.getPlaylist(mRepository.getCurrentPlaylist());
        if (allTracks.size() > 0) {
            int currentMusicTrack = -1;
            for (int i = 0; i < allTracks.size(); i++) {
                if (allTracks.get(i).equals(MusicModel.getCurrentTrackPath())) {
                    currentMusicTrack = i;
                    break;
                }
            }
            int newTrackIndex = currentMusicTrack + 1 >= allTracks.size() ? 0 : currentMusicTrack + 1;
            File file;
            while (!(file = new File(allTracks.get(newTrackIndex))).exists()) {
                newTrackIndex = newTrackIndex + 1 >= allTracks.size() ? 0 : newTrackIndex + 1;
            }
            return file.getAbsolutePath();
        }
        return null;
    }

    public String getLastTrackPath() {
        String path = mRepository.getCurrentTrackPath();
        if (path == null) {
            List<String> allTracks = mRepository.getPlaylist(mRepository.getCurrentPlaylist());
            if (allTracks.size() > 0) {
                path = allTracks.get(0);
            }
        }
        return path;
    }

    public void rememberPath(String path) {
        mRepository.setCurrentTrackPath(path);
    }

    public void rememberPlayRandomTrack(boolean isPlayRandom) {
        mRepository.setPlayRandomTrack(isPlayRandom);
    }

    public void rememberLooping(boolean isLooping) {
        mRepository.setTrackLooping(isLooping);
    }
}
