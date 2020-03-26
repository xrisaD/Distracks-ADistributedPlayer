import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Path;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils ;

public class MusicPlayer extends Application {
    public void start(Stage stage) {
        //Circle circ = new Circle(40, 40, 30);
        //Group root = new Group(circ);
        // scene = new Scene(root, 400, 300);


        //play(new MusicFile(songs.get(1) , songData));
        //stage.setTitle("My JavaFX Application");
       // stage.setScene(scene);
        stage.show();


        System.out.println("Number of active threads from the given thread: " + Thread.activeCount());

        System.out.println("Shiet\n");
        System.out.println("Getting song metadata");
        List<MusicFileMetaData> songs = MP3Cutter.getSongsMetaData("a" , "z");
        MusicFile mf = readFully(songs.get(10));
        //Play two songs at the same time
        List<MusicFile> chunks = breakMusicFile(mf , 1024*512);
        System.out.println("chunks : " + chunks.size());
        playSequentially(chunks , 0);
        //for(int i = 0 ; i < 1 ; i++){
        //    play(chunks.get(i) , "tmp/chunk" + i + ".mp3" , () ->System.out.println("Done"));
        //}
    }

    /**
     * THIS IS THE HARDEST PIECE OF CODE I HAVE EVER WRITTEN
     */
    public void playSequentially(List<MusicFile> musicFiles , int index){
        if(musicFiles.isEmpty()) return;
        play(musicFiles.get(0) , "tmp/chunk" + index +".mp3" , new Runnable(){
            int i = 0;
            public void run() {
                //Rmoving the song that just played
                musicFiles.remove(0);
                System.out.println("Moving to the next lil chunk");
                playSequentially(musicFiles, index+1);
                i++;
            }
        });

    }
    public static List<MusicFile> breakMusicFile(MusicFile musicFile, int chunkSize){
        byte[] largeFile = musicFile.getMusicFileExtract();
        ArrayList<byte[]> extracts = new ArrayList<>();
        int offset = 0;
        while(offset < largeFile.length){

            byte[] chunk = new byte[chunkSize];
            //If this is the last chunk and the bytes left are less than the chunk's size
            if(largeFile.length - offset < chunkSize){
                chunk = new byte[largeFile.length - offset];
            }
            System.out.println(chunk.length);
            for(int i = 0 ; i < chunk.length ; i++){
                chunk[i] = largeFile[offset];
                offset++;
            }
            extracts.add(chunk);
            if(offset >= largeFile.length){
                break;
            }
        }
        ArrayList<MusicFile> mfs = new ArrayList<>();
        for(byte[] b : extracts){
            mfs.add(new MusicFile(musicFile.getMetaData() , b));
        }
        return mfs;
    }
    public static MusicFile readFully(MusicFileMetaData md){
        byte[] res = null;
        try (FileInputStream fos = new FileInputStream(md.getPath())) {
            res = IOUtils.toByteArray(fos);
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
        return new MusicFile(md,res);
    }
    public void play(MusicFile file , String tempFileName, Runnable next) {
        System.out.println(file);
        createTempFile(tempFileName);

        musicFileToMp3(file , tempFileName);

        String bip = tempFileName;
        Media hit = new Media(new File(bip).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(hit);
        mediaPlayer.play();

    }
    public static void musicFileToMp3(MusicFile file, String filename) {
        try (FileOutputStream fos = new FileOutputStream(filename , false)) {
            fos.write(file.getMusicFileExtract());
        }
        catch(Exception e){
            System.err.println(e.getMessage());
        }
    }
    public static void createTempFile(String fileName){
        new File("tmp").mkdir();
        File tmpFile = new File(fileName);

        try{
            tmpFile.delete();
            tmpFile.createNewFile();
            //tmpFile.deleteOnExit();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}