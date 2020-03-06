public class MusicFile {

	public String trackName;
	public String artistName;
	public String albumInfo;
	public String genre;
	public byte[] musicFileExtract;
	public MusicFile(String trackName,String artistName,String albumInfo,String genre,byte[] musicFileExtract){
		this.trackName=trackName;
		this.artistName=artistName;
		this.albumInfo=albumInfo;
		this.genre=genre;
	}
}