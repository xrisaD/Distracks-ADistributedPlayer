package com.world.myapplication;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Consumer extends Application {
    private ArrayList<Component> knownBrokers = new ArrayList<>();
    private Map<ArtistName, Component> artistToBroker = new HashMap<ArtistName, Component>();
    @Override
    public void onCreate() {
        super.onCreate();

        knownBrokers.add(new Component("192.168.1.8", 5000));
        //this.readBroker(getFilesDir().getAbsolutePath()+"brokers.txt");
    }
    // Send a search request to the broker at the end of the outputstream
    public void requestSearchToBroker(ArtistName artist, ObjectOutputStream out) throws IOException {
        Request.RequestToBroker request = new Request.RequestToBroker();
        request.method = Request.Methods.SEARCH;
        request.pullArtistName = artist.getArtistName();
        out.writeObject(request);
    }
    //Send a pull request to the broker at the end of the stream
    public void requestPullToBroker(ArtistName artist, String songName, ObjectOutputStream out) throws IOException {
        Request.RequestToBroker request = new Request.RequestToBroker();
        request.method = Request.Methods.PULL;
        request.pullArtistName = artist.getArtistName();
        request.songName = songName;
        out.writeObject(request);
    }
    //TODO:read Brokers file from resraw
    // isos xrhsimos o kato kodikas h proth grammh, isws diavasma me input stream
    public void readBroker(String fileName) {

        InputStream ins = getResources().openRawResource(
                getResources().getIdentifier("FILENAME_WITHOUT_EXTENSION",
                        "raw", getPackageName()));

        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            if (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] arrOfStr = data.split("\\s");
                String ip = arrOfStr[0];
                int port = Integer.parseInt(arrOfStr[1]);
                //add the first broker
                knownBrokers.add(new Component(ip,port));
            }
            //close reader
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    //Find the reposible broker for this artist , or, If he is not yet known return a random known Broker
    public Component getBroker(ArtistName artist){
        String ip = null;
        int port = 0;
        //try to find the responsible broker
        Component c = artistToBroker.get(artist);
        if(c!=null){
            //this consumer have done this search before
            ip = c.getIp();
            port = c.getPort();
        }//take a random broker
        else{
            System.out.println("known brokers" + knownBrokers);
            int index = new Random().nextInt(knownBrokers.size());
            ip = knownBrokers.get(index).getIp();
            port = knownBrokers.get(index).getPort();
        }
        return new Component(ip, port);
    }
    // Register the broker with ip c.getIp , port c.getPort as responsible for thie artistname
    public void register(Component c, ArtistName artist) {
        artistToBroker.put(artist,c);
        this.knownBrokers.add(c);
    }
    //Download song and save to filename
    public void download(int numChunks, ObjectInputStream in, String filename,File path) throws IOException, ClassNotFoundException {
        ArrayList<MusicFile> chunks = new ArrayList<>();
        Log.e("E","STARTED DOWNLOAD METHOD");
        int size = 0;
        //Start reading chunks
        for (int i = 0; i < numChunks; i++) {
            //HandleCHunks
            Object input = in.readObject();

            System.out.println(input.getClass().getCanonicalName());
            System.out.println(MusicFile.class.getCanonicalName());
            MusicFile chunk = (MusicFile) input;
            //System.out.println(chunk.getMetaData());
            //System.out.println(Arrays.hashCode(chunk.getMusicFileExtract()));
            System.out.println("[CONSUMER] got chunk Number " + i);
            System.out.println();
            size += chunk.getMusicFileExtract().length;
            //Add chunk to the icomplete list
            chunks.add(chunk);
        }
        Log.e("WRA NA SOSOUME TRAGOUDI",filename);
        save(chunks, filename + ".mp3", path);
    }

    // Save a list of music files as entire mp3 with the given filename
    public void save(ArrayList<MusicFile> chunks , String filename, File path) throws IOException {
        Log.e("pathaki", path + filename);
        Log.e("aloha",filename);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//baos stream gia bytes
        for(int k = 0 ; k < chunks.size() ; k++){
            baos.write(chunks.get(k).getMusicFileExtract());
        }
        byte[] concatenated_byte_array = baos.toByteArray();//metatrepei to stream se array
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            fos.write(concatenated_byte_array);
        }
    }
}
