package ru.reactiveturtle.reactivemusic.player.service;

public class Bridges {
    public static final String PreviousTrackClick_To_PlayTrack = "(PreviousTrackClick)To(PlayTrack)";
    public static final String NextTrackClick_To_PlayTrack = "(NextTrackClick)To(PlayTrack)";

    public static final String PlaylistAddClick_To_ShowCreateNameDialog = "(PlaylistAddClick)To(ShowCreateNameDialog)";
    public static final String PlaylistOpen_To_ShowPlaylist = "(PlaylistOpen)To(ShowPlaylist)";
    public static final String PlaylistCreated_To_UpdateFragment = "(PlaylistCreated)To(UpdateFragment)";
    public static final String PlaylistFragment_To_ShowTrackActions = "(PlaylistActionsClick)To(ShowTrackActions)";
    public static final String PlaylistClick_To_FindTrack = "(PlaylistClick)To(FindTrack)";
    public static final String PlaylistClick_To_FindTrackInMainPlaylist = "(PlaylistClick)To(FindTrackInMainPlaylist)";
    public static final String FindTrack_To_PlaylistScrollToTrack = "(FindTrack)To(ScrollToTrack)";
    public static final String FindTrack_To_MusicListScrollToTrack = "(FindTrack)To(MusicListScrollToTrack)";
    public static final String RenameDialog_To_RenamePlaylistList = "(RenameDialog)To(RenamePlaylistList)";
    public static final String MusicService_To_Init = "(MusicService)To(Init)";
    public static final String MusicBroadcast_To_CloseService = "(MusicBroadcast)To(CloseService)";
    public static final String SettingsClick_To_UpdateToolbarArrow = "(SettingsClick)To(UpdateToolbarArrow)";
    public static final String SelectMusicListFragment_To_ViewLoaded = "(SelectMusicListFragment)To(ViewLoaded)";
    public static final String SelectMusicListFragment_To_AddTrack = "(SelectMusicListFragment)To(AddTrack)";
    public static final String SelectMusicListFragment_To_RemoveTrack = "(SelectMusicListFragment)To(RemoveTrack)";
    public static final String SelectMusicListFragment_To_AddTrackPlaylist = "(SelectMusicListFragment)To(AddTrackPlaylist)";
    public static final String SelectMusicListFragment_To_RemoveTrackPlaylist = "(SelectMusicListFragment)To(RemoveTrackPlaylist)";
}
