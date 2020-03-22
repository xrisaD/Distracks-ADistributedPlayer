import java.io.Serializable;

public class MusicFile implements Serializable {
	private byte[] musicFileExtract;
	private MusicFileMetaData metaData;
	//constructor
	public MusicFile( MusicFileMetaData metaData ,byte[] musicFileExtract){
		this.metaData = metaData;
		this.musicFileExtract=musicFileExtract;
	}
	public MusicFile(){

	}
	//setters and getters
	public byte[] getMusicFileExtract() {
		return musicFileExtract;
	}
	public void setMusicFileExtract(byte[] musicFileExtract) {
		this.musicFileExtract = musicFileExtract;
	}
	public void setMetaData(MusicFileMetaData metaData){
		this.metaData = metaData;
	}
	public MusicFileMetaData getMetaData(){
		return metaData;
	}
	
}
