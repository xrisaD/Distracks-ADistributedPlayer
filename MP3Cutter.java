import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.nio.file.*;
import com.mpatric.mp3agic.*;
import  java.util.stream.*;
import java.util.regex.*;
class MP3Cutter{
	private File mp3ToCut;
	public MP3Cutter(){}
	public MP3Cutter(File f){
		this.mp3ToCut = f;
	}

	//TODO: split pou tha dexete musicFile os orisma kai tha to spaei se polla musicFiles(dhladh se chunks) kai tha epistrefei ena list me afta ta musicFiles

	public static List<MusicFile> splitFile(MusicFile mf){
		return null;
	}

	public static ArrayList<MusicFileMetaData> getSongsMetaData(String first, String last){
		//A first letter, Z last letter, closed set
		ArrayList <MusicFileMetaData> AllMetadata = new ArrayList<MusicFileMetaData>();
		Path currentRelativePath = Paths.get("");
		try (Stream<Path> walk = Files.walk(Paths.get(currentRelativePath.toAbsolutePath().toString()+"\\src\\dataset"))) {
			List<String> temp = walk.map(x -> x.toString()).filter(f -> (f.endsWith(".mp3"))).collect(Collectors.toList());
			for(String s: temp){
				if(!s.contains("._")){
					//create music file with meta data
					MusicFileMetaData MFD = ID3(new File(s));
					if((MFD.getArtistName().toLowerCase().compareTo(first)>=0) && (MFD.getArtistName().toLowerCase().compareTo(last)<=0)){
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
		System.out.println(numOfChunks);
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

		byte[] allByteArray = new byte[(int)length];
		ByteArrayOutputStream my_stream = new ByteArrayOutputStream();
		ByteBuffer buff = ByteBuffer.wrap(allByteArray);
		for(int k=0;k<numOfChunks;k++){
			System.out.println(chunklist.get(k).length);
			my_stream.write(chunklist.get(k));
		}
		byte[] concatenated_byte_array = my_stream.toByteArray();
		byte[] combined = buff.array();
		try (FileOutputStream fos = new FileOutputStream("C:\\Users\\Jero\\Desktop\\TESTING\\testing.mp3")) {

			fos.write(concatenated_byte_array);

			//fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
		}
		return chunklist;
	}


	/**
	 //AN THELAME NA APOTHIKEVOUME CHUNKS.MP3
	 //try-with-resources to ensure closing stream
	 try (FileInputStream fis = new FileInputStream(f);
	 BufferedInputStream bis = new BufferedInputStream(fis)) {

	 int bytesAmount = 0;
	 while ((bytesAmount = bis.read(buffer)) > 0) {
	 //write each chunk of data into separate file with different number in name
	 int indexOfMp3 = fileName.indexOf(".mp3");
	 String newName = fileName.substring(0,indexOfMp3);
	 String filePartName = String.format("%s%03d.mp3", newName, partCounter++);
	 File newFile = new File(f.getParent(), filePartName);
	 try (FileOutputStream out = new FileOutputStream(newFile)) {
	 out.write(buffer, 0, bytesAmount);
	 }
	 }
	 return chunklist;
	 **/
	public static String walk( String path,String song) {

		File root = new File( path );
		File[] list = root.listFiles();

		if (list==null) {return "Error";}

		for ( File f : list ) {
			if ( f.isDirectory() ) {
				walk( f.getAbsolutePath(),song);
				System.out.println( "Dir:" + f.getAbsoluteFile() );
			}else {
				if(f.getName().startsWith("._")) continue;
				if(!f.getName().endsWith(".mp3")) continue;
				if(f.getName().toLowerCase().equals(song.toLowerCase())){
					String fileName=f.getAbsolutePath();
					System.out.println( "File:" + f.getAbsoluteFile() );
					return fileName;
				}


			}
		}
		return "Error";
	}
	public static void mergeFiles(List<File> files, File into)
			throws IOException {
		try (FileOutputStream fos = new FileOutputStream(into);
			 BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {
			for (File f : files) {
				Files.copy(f.toPath(), mergingStream);
			}
		}
	}

	public static List<File> listOfFilesToMerge(File oneOfFiles) {
		String tmpName = oneOfFiles.getName();//{name}.{number}
		String destFileName = tmpName.substring(0, tmpName.lastIndexOf('.'));//remove .{number}
		String temp = tmpName.substring(0, tmpName.lastIndexOf('.')-2);
		File[] files = oneOfFiles.getParentFile().listFiles(
				(File dir, String name) -> name.matches(temp + "\\d+.mp3"));

		Arrays.sort(files);//ensuring order 001, 002, ..., 010, ...
		return Arrays.asList(files);
	}

	public static void mergeFiles(File oneOfFiles, File into)
			throws IOException {
		mergeFiles(listOfFilesToMerge(oneOfFiles), into);
	}

	public static List<File> listOfFilesToMerge(String oneOfFiles) {
		return listOfFilesToMerge(new File(oneOfFiles));
	}

	public static void mergeFiles(String oneOfFiles, String into) throws IOException{
		mergeFiles(new File(oneOfFiles), new File(into));
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

	public static void main(String[] args) throws IOException {
		//Path current = Paths.get("documents.txt");
		//String file = current.toAbsolutePath().toString();
		//getSongsMetaData("b","j");
		//walk("C:\\Users\\Jero\\Desktop\\DistributedSystemsAssignment\\songs\\Horror","Horroriffic.mp3");
		//Path currentRelativePath = Paths.get("");
		//splitFile(new File(currentRelativePath.toAbsolutePath().toString()+"\\dataset\\dataset1\\World\\Eye of Forgiveness.mp3"));
		//mergeFiles(currentRelativePath.toAbsolutePath().toString()+"\\dataset\\dataset1\\World\\Eye of Forgiveness001.mp3",currentRelativePath.toAbsolutePath().toString()+"\\Eye of Forgiveness.mp3");
	}

}