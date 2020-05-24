package com.world.myapplication;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;


public class Distracks extends Application {
    private Consumer consumer;

    MediaPlayer onlinePlayer = new MediaPlayer();
    MediaPlayer offlinePlayer = new MediaPlayer();
    String currentDataSource;
    private  AsyncDownload runner;

    //last search, don't ask again broker for this data
    public String lastSearch = "";
    public ArrayList<MusicFileMetaData> lastSearchResult= null;

    //State
    public String playNowArtist;
    public String playNowSong;
    public byte[] imageBytesNow;
    public long duration;

    //Reference to the player fragment that is currently being used
    PlayerFragment player;
    boolean currentlyStreamingOnline  = true;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        consumer = new Consumer();
        consumer.addBroker(new Component("192.168.1.13", 5000));
    }

    // download song with this meta data
    public  void download(MusicFileMetaData artistAndSong){
        runner = new AsyncDownload();
        runner.execute(artistAndSong);
    }


    // stream offline song
    public void streamSongOffline(String path){
        currentlyStreamingOnline = false;
        try{
            resetEverything();
            offlinePlayer = new MediaPlayer();
            offlinePlayer.setDataSource(path);
            offlinePlayer.prepare();
            offlinePlayer.start();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
    // stream online song
    public void streamSongOnline(String artistName, String songName){
        resetEverything();
        currentlyStreamingOnline = true;
        MusicFileMetaData metaData= new MusicFileMetaData(songName , artistName , null , null , null,
                0, null);
        DistracksOnlineMediaDataSource mDataSource = new DistracksOnlineMediaDataSource(consumer , metaData);
        onlinePlayer = new MediaPlayer();
        onlinePlayer.setDataSource(mDataSource);
        try {
            onlinePlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        onlinePlayer.start();

    }

    //Async for download
    public class AsyncDownload extends AsyncTask<MusicFileMetaData, Integer, String> {
        private ArrayList<MusicFile> chunks = new ArrayList<>();
        int PROGRESS_MAX;
        int PROGRESS_CURRENT;
        int notificationId;
        NotificationManagerCompat notificationManager;
        NotificationCompat.Builder builder;
        MusicFileMetaData musicFileMetaData = null;
        boolean completed = false;

        @Override
        protected String doInBackground(MusicFileMetaData... artistAndSong) {

            musicFileMetaData = artistAndSong[0];
            //get data
            ArtistName artist = new ArtistName(musicFileMetaData.getArtistName());
            String songName = musicFileMetaData.getTrackName();

            Component b = consumer.getBroker(artist);
            String ip = b.getIp();
            int port = b.getPort();

            Socket s = null;
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            try {
                //While we find a broker who is not responsible for the artistname
                Request.ReplyFromBroker reply=null;
                int statusCode = Request.StatusCodes.NOT_RESPONSIBLE;
                while(statusCode == Request.StatusCodes.NOT_RESPONSIBLE){
                    s = new Socket(ip, port);
                    //Creating the request to Broker for this artist
                    out = new ObjectOutputStream(s.getOutputStream());
                    consumer.requestPullToBroker(artist, songName, out);
                    //Waiting for the reply
                    in = new ObjectInputStream(s.getInputStream());
                    reply = (Request.ReplyFromBroker) in.readObject();
                    System.out.printf("[CONSUMER] Got reply from Broker(%s,%d) : %s%n", ip, port, reply);
                    statusCode = reply.statusCode;
                    ip = reply.responsibleBrokerIp;
                    port = reply.responsibleBrokerPort;
                }
                if(statusCode == Request.StatusCodes.NOT_FOUND){
                    System.out.println("Song or Artist does not exist");
                    throw new Exception("Song or Artist does not exist");
                }
                //Song exists and the broker is responsible for the artist
                else if(statusCode == Request.StatusCodes.OK){
                    //Save the information that this broker is responsible for the requested artist
                    consumer.register(new Component(s.getInetAddress().getHostAddress(),s.getPort()) , artist);
                    //download mp3 to the device
                    download(reply.numChunks, in , out, songName);
                }
                //In this case the status code is MALFORMED_REQUEST
                else{
                    System.out.println("MALFORMED_REQUEST");
                    throw new Exception("MALFORMED_REQUEST");
                }
            }
            catch(ClassNotFoundException e){
                //Protocol Error (Unexpected Object Caught) its a protocol error
                System.out.printf("[CONSUMER] Unexpected object on playData %s " , e.getMessage());
                e.printStackTrace();
            }
            catch (Exception e){
                System.out.printf("[CONSUMER] Error on playData %s " , e.getMessage());
                e.printStackTrace();
            }
            finally {
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (s != null) s.close();
                }
                catch(Exception e){
                    System.out.printf("[CONSUMER] Error while closing socket on playData %s " , e.getMessage());
                    e.printStackTrace();
                }

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            notificationManager = NotificationManagerCompat.from(getApplicationContext());
            builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_audiotrack_light)
                    .setContentTitle("Download Song ")
                    .setContentText("Download in progress...")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            PROGRESS_MAX = 100;
            PROGRESS_CURRENT = 0;
            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            notificationId = 1;
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(1, builder.build());

        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            PROGRESS_CURRENT = 100;
            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            if(completed) {
                builder.setContentText("Download complete")
                        .setProgress(0, 0, false);
                notificationManager.notify(notificationId, builder.build());
            }else{
                builder.setContentText("Can't download")
                        .setProgress(0, 0, false);
                notificationManager.notify(notificationId, builder.build());
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(values[0]>0) {
                PROGRESS_CURRENT = PROGRESS_CURRENT + values[0];
                builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            }
            notificationManager.notify(notificationId, builder.build());
        }


        //Download song and save with given filename
        private void download(int numChunks, ObjectInputStream in, ObjectOutputStream out, String filename) throws IOException, ClassNotFoundException {
            chunks = new ArrayList<>(numChunks);
            int size = 0;
            //Start reading chunks

            Integer progressStep = new Integer(PROGRESS_MAX/(numChunks+1));
            for (int i = 0; i < numChunks; i++) {

                //HandleCHunks
                Object object = in.readObject();
                if(object instanceof MusicFile) {

                    MusicFile chunk = (MusicFile) object;

                    System.out.println("[CONSUMER] got chunk Number " + i);
                    System.out.println();

                    size += chunk.getMusicFileExtract().length;
                    //Add chunk to the icomplete list
                    chunks.add(chunk);

                    // update notification
                    publishProgress(progressStep);
                }
            }
            // send the_end so broker can close the socket
            Request.RequestToBroker requestToBroker= new Request.RequestToBroker();
            requestToBroker.method = Request.Methods.THE_END;
            out.writeObject(requestToBroker);
            out.flush();

            //if everything ok, we got all chunks
            if(chunks.size() == numChunks) {
                save(chunks, filename + ".mp3");
            }
        }

        // Save a list of music files as entire mp3 with the given filename
        private void save(ArrayList<MusicFile> chunks , String filename) throws IOException {
            System.out.println("Saving a song to " + getFilesDir().getAbsolutePath()+ "/" + filename);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();//baos stream gia bytes
            for(MusicFile chunk : chunks){
                baos.write(chunk.getMusicFileExtract());
            }
            byte[] concatenated_byte_array = baos.toByteArray();//metatrepei to stream se array
            try (FileOutputStream fos = new FileOutputStream(getFilesDir() + "/" + filename)) {
                fos.write(concatenated_byte_array);
            }
            completed = true;
        }
    }

    //Player's methods
    private void pauseOnlineStreaming(){
        onlinePlayer.pause();
    }
    private void resumeOnlineStreaming(){
        onlinePlayer.start();
    }
    private void pauseOfflineStreaming(){
        this.offlinePlayer.pause();
    }
    private void resumeOfflineStreaming(){
        offlinePlayer.start();
    }

    public void pause(){
        if(currentlyStreamingOnline){
            pauseOnlineStreaming();
        }
        else{
            pauseOfflineStreaming();
        }
    }
    public void resume(){
        if(currentlyStreamingOnline){
            resumeOnlineStreaming();
        }
        else {
            resumeOfflineStreaming();
        }
    }
    private void resetEverything(){
        try{
            System.out.println("RESETTING EVERYTHING");
            if(onlinePlayer != null) {
                onlinePlayer.reset();
                onlinePlayer.release();
                onlinePlayer = null;
            }
            if(offlinePlayer != null) {
                offlinePlayer.reset();
                offlinePlayer.release();
                offlinePlayer = null;
            }
            System.out.println("SUCCESSFULLY RESETED EVERYTHING");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    public void seekTo(int seconds){
        if(!currentlyStreamingOnline) {
            if(!offlinePlayer.isPlaying()){
                offlinePlayer.stop();
                try {
                    offlinePlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                offlinePlayer.seekTo(seconds* 1000);
                offlinePlayer.start();
            }
            else {
                offlinePlayer.seekTo(seconds * 1000);
            }
        }
        else{
            if(!onlinePlayer.isPlaying()){
                onlinePlayer.stop();
                try {
                    onlinePlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                onlinePlayer.seekTo(seconds* 1000);
                onlinePlayer.start();
            }
            else {
                onlinePlayer.seekTo(seconds * 1000);
            }
        }

    }
    public int getCurrentPositionInSeconds(){
        if(!currentlyStreamingOnline){
            if(offlinePlayer == null) return 0;
            return offlinePlayer.getCurrentPosition() / 1000 ;
        }
        else{
            if(onlinePlayer == null) return 0;
            return onlinePlayer.getCurrentPosition() / 1000;
        }
    }

    public Consumer getConsumer() {
        return consumer;
    }
    // set State
    public void setState(String playNowArtist, String playNowSong, byte[] imageBytesNow, long duration){
        this.playNowArtist = playNowArtist;
        this.playNowSong = playNowSong;
        this.imageBytesNow = imageBytesNow;
        this.duration = duration;
    }
    public boolean isStateNull(){
        if(playNowArtist == null && playNowSong == null && imageBytesNow == null){
            return true;
        }
        return false;
    }

    // notification channel: for download
    private static final String CHANNEL_ID = "BasicChannel";
    private void createNotificationChannel() {
        String channel_name = "Basic chanel";
        String channel_description = "Download";
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = channel_name;
            String description = channel_description;
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
