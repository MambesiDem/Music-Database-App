package com.example.task2;

public class Song {
    private int songId;
    private int albumId;
    private int songNo;
    private String Name;
    private String artist;
    private float length;

    public Song(int songId, int albumId, int songNo, String name, String artist, float length) {
        this.songId = songId;
        this.albumId = albumId;
        this.songNo = songNo;
        Name = name;
        this.artist = artist;
        this.length = length;
    }

    public int getSongId() {
        return songId;
    }

    public int getAlbumId() {
        return albumId;
    }

    public int getSongNo() {
        return songNo;
    }

    public String getName() {
        return Name;
    }

    public String getArtist() {
        return artist;
    }

    public float getLength() {
        return length;
    }
}
