import java.io.*;
import java.util.*;
import java.nio.file.*;
class MP3Cutter{
    public static void splitFile(File f) throws IOException {
        int partCounter = 1;//I like to name parts from 001, 002, 003, ...
                            //you can change it to 0 if you want 000, 001, ...

        int sizeOfFiles = 1024 * 1024;// 1MB
        byte[] buffer = new byte[sizeOfFiles];

        String fileName = f.getName();

        //try-with-resources to ensure closing stream
        try (FileInputStream fis = new FileInputStream(f);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int bytesAmount = 0;
            while ((bytesAmount = bis.read(buffer)) > 0) {
                //write each chunk of data into separate file with different number in name
                String filePartName = String.format("%s.%03d", fileName, partCounter++);
                File newFile = new File(f.getParent(), filePartName);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, bytesAmount);
                }
            }
        }
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
	
	public static void main(String[] args) throws IOException {
        splitFile(new File("C:\\Users\\Jero\\Desktop\\Kesha - TiK ToK.mp3"));
        Path currentRelativePath = Paths.get("");
        mergeFiles(currentRelativePath.toAbsolutePath().toString()+"\\Kesha - TiK ToK.mp3.001",currentRelativePath.toAbsolutePath().toString()+"\\KappaKeepo.mp3");
    }
    
}