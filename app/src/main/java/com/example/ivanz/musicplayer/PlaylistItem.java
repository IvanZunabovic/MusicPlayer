package com.example.ivanz.musicplayer;

public class PlaylistItem {
    private String songName;
    private String path;
    private boolean favorite;

    public PlaylistItem(String songName, String path) {
        this.songName = songName;
        this.path = path;
        this.favorite = false;
    }

    public String getSongName() {
        return songName;
    }

    public String getPath() {
        return path;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean isFavorite) {
        this.favorite = isFavorite;
    }
}
