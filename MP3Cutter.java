import java.io.*;
import java.util.*;
import java.nio.file.*;
import com.mpatric.mp3agic.*;
import  java.util.stream.*;
class MP3Cutter{
	private File mp3ToCut;
	public MP3Cutter(File f){
		this.mp3ToCut = f;
	}
	public static ArrayList<MusicFileMetaData> getSongsMetaData(String first, String last){
		//A first letter, Z last letter, closed set
		ArrayList <MusicFileMetaData> AllMetadata = new ArrayList<MusicFileMetaData>();
		Path path = Paths.get("dataset").toAbsolutePath();
		try (Stream<Path> walk = Files.walk(path)) {
			List<String> temp = walk.map(x -> x.toString()).filter(f -> (f.endsWith(".mp3"))).collect(Collectors.toList());
			for(String s: temp){
				if(!s.contains("._")){
					//create music file with meta data
					MusicFileMetaData MFD = ID3(new File(s));
					if((MFD.getArtistName().toLowerCase().compareTo(first.toLowerCase())>=0) && (MFD.getArtistName().toLowerCase().compareTo(last.toLowerCase())<=0)){
						//if artistName is in this range, then this Publisher is responsible for this artist
						AllMetadata.add(MFD);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return AllMetadata;
	}

	public ArrayList<byte[]> splitFile() throws IOException {
		int sizeOfFiles = 1024 * 512;
		ArrayList<byte[]> chunklist=new ArrayList<byte[]> ();
		String fileName = mp3ToCut.getName();//onoma tou arxeiou/mp3
		long length=mp3ToCut.length();//arithmos byte arxeiou
		long numOfChunks = length/sizeOfFiles+1;
		long left=length-sizeOfFiles*(numOfChunks-1);//
		byte[] buffer;
		try (FileInputStream fis = new FileInputStream(mp3ToCut);
			BufferedInputStream bis = new BufferedInputStream(fis)) {
			int bytesAmount = 0;
			for(int i=0;i<numOfChunks;i++){
				if(i==numOfChunks-1) {
					buffer = new byte[(int) left];
				}else{
					buffer= new byte[sizeOfFiles];
				}
				bytesAmount = bis.read(buffer);
				chunklist.add(buffer);
				/**
				 int partCounter=0;
				 int indexOfMp3 = fileName.indexOf(".mp3");
				 String newName = fileName.substring(0,indexOfMp3);
				 String filePartName = String.format("%s%03d.mp3", newName, partCounter++);
				 File newFile = new File(mp3ToCut.getParent(), filePartName);
				 try (FileOutputStream out = new FileOutputStream(newFile)) {
				 out.write(buffer, 0, bytesAmount);
				 }
				 **/
			}
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();//baos stream gia bytes
		for(int k=0;k<numOfChunks;k++){
			baos.write(chunklist.get(k));
		}
		byte[] concatenated_byte_array = baos.toByteArray();//metatrepei to stream se array
		Path path = Paths.get("").toAbsolutePath();
		try (FileOutputStream fos = new FileOutputStream(path+"splittested.mp3")) {
			fos.write(concatenated_byte_array);
		}
		return chunklist;//gyrnaei lista me ta chunks
	}

	//function that handles metadata
	public static MusicFileMetaData ID3(File f)  {
		MusicFileMetaData MFD = new MusicFileMetaData();
		String fileN = f.getName();
		fileN = fileN.substring(fileN.lastIndexOf("\\")+1, fileN.lastIndexOf("."));
		MFD.setPath(f.getAbsolutePath());
		//handle metadata
		try {
			Mp3File mp3file = new Mp3File(f);
			//if hasId3v1Tag it means that it have hasId3v1Tag metadata
			if (mp3file.hasId3v1Tag()) {
				//take metadata
				ID3v1 id3v1Tag = mp3file.getId3v1Tag();

				//TITLE
				if (id3v1Tag.getTitle() != null) {
					MFD.setTrackName(id3v1Tag.getTitle());
				} else {
					//if it title is null, set it with fileName
					MFD.setTrackName(fileN);
				}

				//ARTIST
				if (id3v1Tag.getArtist() != null) {
					MFD.setArtistName(id3v1Tag.getArtist());
				} else {
					//if it is null, set is as unknown
					MFD.setArtistName("Unknown Artist");
				}

				//ALBUM
				if (id3v1Tag.getAlbum() != null) {
					MFD.setAlbumInfo(id3v1Tag.getAlbum());
				} else {
					//if it is null, set is as unknown
					MFD.setAlbumInfo("Unknown Album");
				}

				//GENRE
				if (id3v1Tag.getGenreDescription() != null) {
					MFD.setGenre(id3v1Tag.getGenreDescription());
				} else {
					//if it is null, set is as unknown
					MFD.setGenre("Unknown Genre");
				}
				/**
				 System.out.println("Track: " + id3v1Tag.getTrack());
				 System.out.println("Artist: " + id3v1Tag.getArtist());
				 System.out.println("Title: " + id3v1Tag.getTitle());
				 System.out.println("Album: " + id3v1Tag.getAlbum());
				 System.out.println("Year: " + id3v1Tag.getYear());
				 System.out.println("Genre: " + id3v1Tag.getGenre() + " (" + id3v1Tag.getGenreDescription() + ")");
				 System.out.println("Comment: " + id3v1Tag.getComment());
				 **/
			}
			//if hasId3v2Tag it means that it have Id3v2Tag metadata
			else if (mp3file.hasId3v2Tag()) {
				ID3v2 id3v2Tag = mp3file.getId3v2Tag();

				if (id3v2Tag.getTitle() != null) {
					MFD.setTrackName(id3v2Tag.getTitle());
				} else {
					//if it title is null, set it with fileName
					MFD.setTrackName(fileN);
				}

				//ARTIST
				if (id3v2Tag.getArtist() != null) {
					MFD.setArtistName(id3v2Tag.getArtist());
				} else {
					//if it is null, set is as unknown
					MFD.setArtistName("Unknown Artist");
				}

				//ALBUM
				if (id3v2Tag.getAlbum() != null) {
					MFD.setAlbumInfo(id3v2Tag.getAlbum());
				} else {
					//if it is null, set is as unknown
					MFD.setAlbumInfo("Unknown Album");
				}

				//GENRE
				if (id3v2Tag.getGenreDescription() != null) {
					MFD.setGenre(id3v2Tag.getGenreDescription());
				} else {
					//if it is null, set is as unknown
					MFD.setGenre("Unknown Genre");
				}

				/**
				 System.out.println("Track: " + id3v2Tag.getTrack());
				 System.out.println("Artist: " + id3v2Tag.getArtist());
				 System.out.println("Title: " + id3v2Tag.getTitle());
				 System.out.println("Album: " + id3v2Tag.getAlbum());
				 System.out.println("Year: " + id3v2Tag.getYear());
				 System.out.println("Genre: " + id3v2Tag.getGenre() + " (" + id3v2Tag.getGenreDescription() + ")");
				 System.out.println("Comment: " + id3v2Tag.getComment());
				 System.out.println("Composer: " + id3v2Tag.getComposer());
				 System.out.println("Publisher: " + id3v2Tag.getPublisher());
				 System.out.println("Original artist: " + id3v2Tag.getOriginalArtist());
				 System.out.println("Album artist: " + id3v2Tag.getAlbumArtist());
				 System.out.println("Copyright: " + id3v2Tag.getCopyright());
				 System.out.println("URL: " + id3v2Tag.getUrl());
				 System.out.println("Encoder: " + id3v2Tag.getEncoder());
				 **/
			}
			else{
				//no metadata occasion
				MFD.setTrackName(fileN);
				MFD.setArtistName("Unknown Artist");
				MFD.setAlbumInfo("Unknown Album");
				MFD.setGenre("Unknown Genre");
			}
			return MFD;
		} catch (UnsupportedTagException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidDataException e) {
			e.printStackTrace();
		}
		return null;
	}
}