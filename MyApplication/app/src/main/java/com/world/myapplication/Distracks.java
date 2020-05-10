package com.world.myapplication;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

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
    ArrayList<MediaPlayer> players;
    MediaPlayer streamPlayer = new MediaPlayer();
    String currentDataSource;
    private  AsyncDownload runner;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        consumer = new Consumer();
        consumer.addBroker(new Component("192.168.1.13", 5000));

        consumer.setPath(getFilesDir());
        //this.readBroker(getFilesDir().getAbsolutePath()+"brokers.txt");
        streamPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        streamPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                System.out.println("Media player ended");

                try {
                    int prev = mp.getCurrentPosition();
                    streamPlayer.reset();
                    streamPlayer.setDataSource(currentDataSource);
                    streamPlayer.prepare();
                    streamPlayer.seekTo(prev);
                    System.out.println("Seeking to " + prev);
                    streamPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        streamPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mp, int what, int extra) {
                if(what == MediaPlayer.MEDIA_INFO_BUFFERING_END)
                System.out.println("bUFFERING ENd");
                return false;
            }
        });
    }


    //Starts playing a song immediateely
    public void streamSong(MusicFileMetaData metadata){
        if(streamPlayer != null && streamPlayer.isPlaying()){
            System.out.println("Stopping previous player");
//            streamPlayer.stop();
//            streamPlayer.release();
//            streamPlayer = new MediaPlayer();
            streamPlayer.reset();
        }
        Log.e("E", "STREAM SONG CALLED");
        StreamSong s = new StreamSong();
        s.execute(metadata);
        while(!consumer.hasReadFirstChunk()){
            System.out.println("First chunk not read yet waiting");
        }
        forceChangeMediaPlayer(consumer.getPath() + metadata.getTrackName());
    }
    private void forceChangeMediaPlayer(String newDataSource){
        System.out.println("force changed caled");
        try {
            streamPlayer.reset();
            streamPlayer.setDataSource(newDataSource);
            currentDataSource = newDataSource;
            streamPlayer.prepare();
            streamPlayer.start();
        } catch (IOException e) {
            forceChangeMediaPlayer(newDataSource);
        }
    }

    // Async for streaming
    private class StreamSong extends AsyncTask<MusicFileMetaData , Void , Void>{

        @Override
        protected Void doInBackground(MusicFileMetaData... musicFileMetaData) {
            String artistName = musicFileMetaData[0].getArtistName();
            String songName = musicFileMetaData[0].getTrackName();
            try {
                consumer.playData(new ArtistName(artistName) , songName , false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private void saveChunk(MusicFile chunk , String filename){
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            System.out.println("Saving a chunk to filename " + filename);
            fos.write(chunk.getMusicFileExtract());
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    // Async for streaming
    private class StreamDownloadedSong extends AsyncTask<MusicFileMetaData , Void , Void>{

        @Override
        protected Void doInBackground(MusicFileMetaData... musicFileMetaData) {
            String artistName = musicFileMetaData[0].getArtistName();
            String songName = musicFileMetaData[0].getTrackName();
            try {
                //Stream downloaded song

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
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
        boolean completed = false;
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
            //Initializing donwload incomplete list
            chunks = new ArrayList<>(numChunks);
            int size = 0;
            //Start reading chunks

            Integer progressStep = new Integer(PROGRESS_MAX/(numChunks+1));
            for (int i = 0; i < numChunks; i++) {
                //ask for the next chunk
                Request.RequestToBroker requestToBroker= new Request.RequestToBroker();
                requestToBroker.method = Request.Methods.NEXT_CHUNK;
                out.writeObject(requestToBroker);
                out.flush();

                //HandleCHunks
                Object object = in.readObject();
                if(object instanceof MusicFile) {

                    MusicFile chunk = (MusicFile) object;

                    System.out.println("[CONSUMER] got chunk Number " + i);
                    System.out.println();

                    size += chunk.getMusicFileExtract().length;
                    //Add chunk to the icomplete list
                    chunks.add(chunk);
                    publishProgress(progressStep);
                }

            }
            Request.RequestToBroker requestToBroker= new Request.RequestToBroker();
            requestToBroker.method = Request.Methods.THE_END;
            out.writeObject(requestToBroker);
            out.flush();

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

    public Consumer getConsumer() {
        return consumer;
    }

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

    public  void download(MusicFileMetaData artistAndSong){
        runner = new AsyncDownload();
        runner.execute(artistAndSong);
    }
}
