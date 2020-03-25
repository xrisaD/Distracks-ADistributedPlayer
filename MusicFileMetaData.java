import java.io.Serializable;

public class MusicFileMetaData implements Serializable {
    private String trackName;
    private String artistName;
    private String albumInfo;
    private String genre;
    private String path;
    //constructor
    public MusicFileMetaData(String trackName,String artistName,String albumInfo,String genre,String path){
        this.trackName=trackName;
        this.artistName=artistName;
        this.albumInfo=albumInfo;
        this.genre=genre;
        this.path=path;
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

    @Override
    public String toString() {
        return "MusicFileMetaData{" +
                "trackName='" + trackName + '\'' +
                ", artistName='" + artistName + '\'' +
                ", albumInfo='" + albumInfo + '\'' +
                ", genre='" + genre + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
