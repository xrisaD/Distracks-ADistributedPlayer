import java.io.Serializable;

public class MusicFileMetaData implements Serializable {
    private String trackName;
    private String artistName;
    private String albumInfo;
    private String genre;

    //constructor
    public MusicFileMetaData(String trackName,String artistName,String albumInfo,String genre){
        this.trackName=trackName;
        this.artistName=artistName;
        this.albumInfo=albumInfo;
        this.genre=genre;
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
}
