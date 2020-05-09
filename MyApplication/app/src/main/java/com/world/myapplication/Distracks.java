package com.world.myapplication;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentTransaction;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;


public class Distracks extends Application {
    private Consumer consumer;
    ArrayList<MediaPlayer> players;
    MediaPlayer streamPlayer = new MediaPlayer();
    String currentDataSource;

    @Override
    public void onCreate() {
        super.onCreate();
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


    private void saveChunk(MusicFile chunk , String filename){
        try (FileOutputStream fos = new FileOutputStream(filename)) {
            System.out.println("Saving a chunk to filename " + filename);
            fos.write(chunk.getMusicFileExtract());
        }
        catch (Exception e){
            e.printStackTrace();
        }
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

    //Async for download
    public Consumer getConsumer() {
        return consumer;
    }
}
