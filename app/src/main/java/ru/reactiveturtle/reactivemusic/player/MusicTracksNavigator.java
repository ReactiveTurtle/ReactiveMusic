package ru.reactiveturtle.reactivemusic.player;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.util.List;

import ru.reactiveturtle.reactivemusic.player.repository.PlayerRepository;
import ru.reactiveturtle.reactivemusic.toolkit.ReactiveList;

public class MusicTracksNavigator {
    private PlayerRepository playerRepository;
    private Context context;
    private AllMusicDataManager allMusicDataManager;

    public MusicTracksNavigator(PlayerRepository repository,
                                Context context,
                                AllMusicDataManager allMusicDataManager) {
        this.playerRepository = repository;
        this.context = context;
        this.allMusicDataManager = allMusicDataManager;
    }

    public String getNextTrack() {
        if (playerRepository.getPlayerModel().isPlayRandomTrack()) {
            return getRandomTrackPath();
        } else {
            return getNextTrackOnList();
        }
    }

    public String getPreviousTrack() {
        if (playerRepository.getPlayerModel().isPlayRandomTrack()) {
            return getRandomTrackPath();
        } else {
            return getPreviousTrackOnList();
        }
    }

    private String getRandomTrackPath() {
        List<String> paths = getCurrrentPlaylist();
        int newPosition = (int) (Math.random() * (paths.size() - 1));
        return paths.get(newPosition);
    }

    public String getPreviousTrackOnList() {
        ReactiveList<String> playlist = getCurrrentPlaylist();
        if (playlist.size() > 0) {
            int currentMusicTrack = -1;
            for (int i = 0; i < playlist.size(); i++) {
                if (playlist.get(i).equals(playerRepository.getPlayerModel().getCurrentMusicPath())) {
                    currentMusicTrack = i;
                    break;
                }
            }
            int newTrackIndex = currentMusicTrack - 1 < 0 ? playlist.size() - 1 : currentMusicTrack - 1;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                DocumentFile file;
                while (!(file = DocumentFile.fromSingleUri(context, Uri.parse(playlist.get(newTrackIndex)))).exists()) {
                    newTrackIndex = newTrackIndex - 1 < 0 ? playlist.size() - 1 : newTrackIndex - 1;
                }
                return file.getUri().toString();
            } else {
                File file;
                while (!(file = new File(playlist.get(newTrackIndex))).exists()) {
                    newTrackIndex = newTrackIndex - 1 < 0 ? playlist.size() - 1 : newTrackIndex - 1;
                }
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public String getNextTrackOnList() {
        ReactiveList<String> allTracks = getCurrrentPlaylist();
        if (allTracks.size() > 0) {
            String currentTrackPath = playerRepository.getPlayerModel().getCurrentMusicPath();
            int currentMusicTrackIndex = allTracks.indexOf(x -> x.equals(currentTrackPath));
            int newTrackIndex = currentMusicTrackIndex + 1 == allTracks.size() ? 0 : currentMusicTrackIndex + 1;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                DocumentFile file;
                while (!(file = DocumentFile.fromSingleUri(context, Uri.parse(allTracks.get(newTrackIndex)))).exists()) {
                    newTrackIndex = newTrackIndex + 1 == allTracks.size() ? 0 : newTrackIndex + 1;
                }
                return file.getUri().toString();
            } else {
                File file;
                while (!(file = new File(allTracks.get(newTrackIndex))).exists()) {
                    newTrackIndex = newTrackIndex + 1 == allTracks.size() ? 0 : newTrackIndex + 1;
                }
                return file.getAbsolutePath();
            }
        }
        return null;
    }

    public String getLastTrackPath() {
        String path = playerRepository.getCurrentTrackPath();
        if (path == null) {
            List<String> allTracks = playerRepository.getPlaylist(playerRepository.getCurrentPlaylistName());
            if (allTracks.size() > 0) {
                path = allTracks.get(0);
            }
        }
        return path;
    }

    public void rememberPath(String path) {
        playerRepository.setCurrentTrackPath(path);
    }

    public void rememberPlayRandomTrack(boolean isPlayRandom) {
        playerRepository.setPlayRandomTrack(isPlayRandom);
    }

    public void rememberLooping(boolean isLooping) {
        playerRepository.setTrackLooped(isLooping);
    }

    private ReactiveList<String> getCurrrentPlaylist() {
        ReactiveList<String> paths = playerRepository.getPlaylist(playerRepository.getCurrentPlaylistName());
        if (paths == null) {
            paths = allMusicDataManager.getPaths();
        }
        return paths;
    }
}
