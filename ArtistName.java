import java.util.Objects;

public class ArtistName {

	private String artistName;
	
	//constructor
	public ArtistName(String artistName){
		this.artistName=artistName;
	}
	//setters and getters
	public String getArtistName() {
		return artistName;
	}
	public void setArtistName(String artistName) {
		this.artistName = artistName;
	}

	@Override
	public int hashCode(){
		return artistName.hashCode();
	}
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ArtistName that = (ArtistName) o;
		return this.artistName.equals( that.artistName);
	}
}