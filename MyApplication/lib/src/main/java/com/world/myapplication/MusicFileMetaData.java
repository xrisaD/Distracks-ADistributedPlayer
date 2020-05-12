package com.world.myapplication;

import java.io.Serializable;
import java.util.Objects;

public class MusicFileMetaData implements Serializable {
    private static long serialVersionUID  = 6L;
    private String trackName;
    private String artistName;
    private String albumInfo;
    private String genre;
    private String path;
    private long duration;
    private byte[] image;

    //constructor
    public MusicFileMetaData(String trackName,String artistName,String albumInfo,String genre,String path,long duration, byte[] image){
        this.trackName = trackName;
        this.artistName = artistName;
        this.albumInfo = albumInfo;
        this.genre = genre;
        this.path = path;
        this.duration = duration;
        this.image = image;
    }
    public MusicFileMetaData(){
    }
    //setters and getters
    public String getTrackName() {
        return trackName;
    }
    public void setTrackName(String trackName) {
        this.trackName = trackName;
    }
    public String getArtistName() {
        return artistName;
    }
    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }
    public String getAlbumInfo() {
        return albumInfo;
    }
    public void setAlbumInfo(String albumInfo) {
        this.albumInfo = albumInfo;
    }
    public String getGenre() {
        return genre;
    }
    public void setGenre(String genre) {
        this.genre = genre;
    }
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public long getDuration() {
        return duration;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "MusicFileMetaData{" +
                "trackName='" + trackName + '\'' +
                ", artistName='" + artistName + '\'' +
                ", albumInfo='" + albumInfo + '\'' +
                ", genre='" + genre + '\'' +
                ", path='" + path + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }


    //For use in caches
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicFileMetaData that = (MusicFileMetaData) o;
        return Objects.equals(trackName, that.trackName) &&
                Objects.equals(artistName, that.artistName);

    }

    @Override
    public int hashCode() {
        return Objects.hash(trackName, artistName);
    }
}
