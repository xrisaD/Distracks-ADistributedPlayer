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
        //stage.show();



        System.out.println("Shiet\n");
        System.out.println("Getting song metadata");
        List<MusicFileMetaData> songs = MP3Cutter.getSongsMetaData("a" , "z");
        System.out.println(songs.get(10));

        ArrayList<MusicFile> musicFiles = new ArrayList<>();
        //Play two songs at the same time
        int n = 4;
        for(int i = 0 ; i < n ; i++){
            MusicFile mf = readFully(songs.get(i));
            musicFiles.add(mf);
            play(mf, "tmp/tmp" + i + ".mp3");
        }
    }
    public static List<MusicFile> breakMusicFile(MusicFile musicFile, int chunkSize){
        byte[] largeFile = musicFile.getMusicFileExtract();
        ArrayList<byte[]> extracts = new ArrayList<>();

        int offset = 0;
        while(offset < largeFile.length){
            if(largeFile.length - offset > chunkSize){

            }
            byte[] chunk = new byte[chunkSize];
            for(int i = 0 ; i < chunkSize ; i++){
                chunk[i] = largeFile[offset];
                offset++;
                if(offset >= largeFile.length){
                    break;
                }
            }
            extracts.add(chunk);
            if(offset >= largeFile.length){
                break;
            }
        }
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
    public void play(MusicFile file , String tempFileName) {
        System.out.println(file);
        createTempFile(tempFileName);

        musicFileToMp3(file , tempFileName);

        String bip = tempFileName;
        Media hit = new Media(new File(bip).toURI().toString());
        System.out.println(new File(bip).toURI().toString());
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
            tmpFile.deleteOnExit();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}