import java.io.*;
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

	//TODO:
	//1h: split pou tha dexete musicFile os orisma kai tha to spaei se polla musicFiles(dhladh se chunks) kai tha epistrefei ena list me afta ta musicFiles
	//2h: tha pernei os orisma to proto gramma kai to teleftaio gramma ta opoia tha eksipiretei o ekastote Publisher tha diavazei
	// ola ta tragoudia apo to dataset kai tha eksagei g kathe song ta metadata. tha ftiaxnei ena pinaka me metaData olwn twn tragoudiwn k tha ta epistrefei

	public static List<MusicFile> splitFile(MusicFile mf){
		return null;
	}

	public static ArrayList<MusicFileMetaData> getSongsMetaData(String a, String z){//A first letter, Z last letter, closed set
		ArrayList <MusicFileMetaData> AllMetadata = new ArrayList<MusicFileMetaData>();
		try (Stream<Path> walk = Files.walk(Paths.get("C:\\Users\\Jero\\Desktop\\dataset"))) {
			List<String> temp = walk.map(x -> x.toString()).filter(f -> (f.endsWith(".mp3"))).collect(Collectors.toList());
			for(String s: temp){
				try{
					MusicFileMetaData MFD = ID3(new File(s));
					AllMetadata.add(MFD);
				} catch (Exception e) {
    				e.printStackTrace();
				}
				if(!s.contains("._")){
					if((s.toLowerCase().compareTo(a)>=0)&&(s.toLowerCase().compareTo(z)<=0)){
						System.out.printf("Getting songs starting from %s to %s",a,z);
						int start=s.lastIndexOf("\\");
						int last=s.lastIndexOf(".");
						s=s.substring(start+1,last);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return AllMetadata;
	}





	public static int splitFile(File f) throws IOException {
		int partCounter = 1;//I like to name parts from 001, 002, 003, ...
		//you can change it to 0 if you want 000, 001, ...

		int sizeOfFiles = 1024 * 512;// 1MB
		byte[] buffer = new byte[sizeOfFiles];

		String fileName = f.getName();

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
			return partCounter;
		}
	}
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
		File[] files = oneOfFiles.getParentFile().listFiles(
				(File dir, String name) -> name.matches(destFileName + "[.]\\d+"));
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
	
	public static MusicFileMetaData ID3(File f) throws InvalidDataException, IOException, UnsupportedTagException {
		Mp3File mp3file = new Mp3File(f);
		MusicFileMetaData MFD= new MusicFileMetaData();
		String fileN= f.getName();
		fileN=fileN.substring(fileN.lastIndexOf("\\"),fileN.lastIndexOf("."));
		
		System.out.println("Length of this mp3 is: " + mp3file + " seconds");
		System.out.println("Bitrate: " + mp3file.getBitrate() + " kbps " + (mp3file.isVbr() ? "(VBR)" : "(CBR)"));
		System.out.println("Sample rate: " + mp3file.getSampleRate() + " Hz");
		System.out.println("Has ID3v1 tag?: " + (mp3file.hasId3v1Tag() ? "YES" : "NO"));
		System.out.println("Has ID3v2 tag?: " + (mp3file.hasId3v2Tag() ? "YES" : "NO"));
		System.out.println("Has custom tag?: " + (mp3file.hasCustomTag() ? "YES" : "NO"));
		//if hasId3v1Tag it means that it have metadata
		if (mp3file.hasId3v1Tag()) {
			ID3v1 id3v1Tag = mp3file.getId3v1Tag();
			//TITLE
			if(id3v1Tag.getTitle()!=null){
				MFD.setTrackName(id3v1Tag.getTitle());
			}else{
				MFD.setTrackName(fileN);//an einai null pare to onoma tou arxeiou
			}

			//ARTIST
			if(id3v1Tag.getArtist()!=null){
				MFD.setTrackName(id3v1Tag.getArtist());
			}else{
				MFD.setTrackName("Unknown Artist");//an einai null vale ton unknown
			}

			//ALBUM
			if(id3v1Tag.getAlbum()!=null){
				MFD.setAlbumInfo(id3v1Tag.getAlbum());
			}else{
				MFD.setAlbumInfo(MFD.getTrackName());//an einai null vale to onoma tou kommatiou san album
			}

			//GENRE
			if(id3v1Tag.getGenreDescription()!=null){
				MFD.setGenre(id3v1Tag.getGenreDescription());
			}else{
				MFD.setGenre("Unknown Genre");//an einai null vale to unknown
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

		ID3v1 id3v1Tag;
		if (mp3file.hasId3v1Tag()) {
			id3v1Tag =  mp3file.getId3v1Tag();
		} else {
			id3v1Tag = new ID3v1Tag();
			mp3file.setId3v1Tag(id3v1Tag);
		}

		if (mp3file.hasId3v2Tag()) {
			ID3v2 id3v2Tag = mp3file.getId3v2Tag();

			if(id3v2Tag.getTitle()!=null){
				MFD.setTrackName(id3v2Tag.getTitle());
			}else{
				MFD.setTrackName(fileN);//an einai null pare to onoma tou arxeiou
			}

			//ARTIST
			if(id3v2Tag.getArtist()!=null){
				MFD.setTrackName(id3v2Tag.getArtist());
			}else{
				MFD.setTrackName("Unknown Artist");//an einai null vale ton unknown
			}

			//ALBUM
			if(id3v2Tag.getAlbum()!=null){
				MFD.setAlbumInfo(id3v2Tag.getAlbum());
			}else{
				MFD.setAlbumInfo(MFD.getTrackName());//an einai null vale to onoma tou kommatiou san album
			}

			//GENRE
			if(id3v2Tag.getGenreDescription()!=null){
				MFD.setGenre(id3v2Tag.getGenreDescription());
			}else{
				MFD.setGenre("Unknown Genre");//an einai null vale to unknown
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
		return MFD;
	}
	

	public static void main(String[] args) throws IOException {
		//Path current = Paths.get("documents.txt");
		//String file = current.toAbsolutePath().toString();
		getSongsMetaData("b","j");
		//walk("C:\\Users\\Jero\\Desktop\\DistributedSystemsAssignment\\songs\\Horror","Horroriffic.mp3");
		//splitFile(new File("C:\\Users\\tinoa\\Desktop\\testmp3\\test.mp3"));
		//Path currentRelativePath = Paths.get("");
		//mergeFiles(currentRelativePath.toAbsolutePath().toString()+"\\Kesha - TiK ToK.mp3.001",currentRelativePath.toAbsolutePath().toString()+"\\KappaKeepo.mp3");
	}
	
}