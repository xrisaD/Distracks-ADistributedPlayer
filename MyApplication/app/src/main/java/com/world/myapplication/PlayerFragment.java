package com.world.myapplication;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.media.MediaPlayer;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public class PlayerFragment extends Fragment {
    private View rootView;
    private MediaPlayer musicPlayer;
    private ImageButton playButton;
    private TextView titleText;
    private SeekBar seek;
    private Runnable runnable;
    private Handler handler;

    private String artist;
    private String song;

    private boolean flag=true;
    private int length;
    //TODO: na asxolithoume me to interface edw!
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.player_fragment, container, false);
        return rootView;
    }

    @Override
    public void onStart() {

        super.onStart();
        if(getArguments() != null){
            boolean offline = getArguments().getBoolean("offline");
            if (offline) {
                String path = getArguments().getString("path"); //get song's path
                artist= getArguments().getString("artist_name");
                song = getArguments().getString("song_name");
                Distracks distracks = (Distracks) getActivity().getApplication();
                distracks.streamSongOffline(path);

            } else {
                //online mode
                artist= getArguments().getString("artist_name");
                song = getArguments().getString("song_name");
                Distracks distracks = (Distracks) getActivity().getApplication();
                distracks.streamSongOnline(artist, song);
            }
        }

        playButton = (ImageButton) rootView.findViewById(R.id.start);
        playButton.setOnClickListener(new View.OnClickListener() {
            boolean paused = false;
            @Override
            public void onClick(View v) {
                if(!paused){
                    Distracks distracks = (Distracks) getActivity().getApplication();
                    playButton.setImageResource(R.drawable.play);
                    distracks.pause();
                    paused = true;
                }
                else{
                    Distracks distracks = (Distracks) getActivity().getApplication();
                    playButton.setImageResource(R.drawable.pause);
                    distracks.resume();
                    paused = false;
                }

            }
        });

        seek = (SeekBar)  rootView.findViewById(R.id.seekbar);
        seek.setEnabled(false);
        titleText = (TextView) rootView.findViewById(R.id.artistsong);
        titleText.setText(artist+ " - "+song);
        handler=new Handler();
        musicPlayer = MediaPlayer.create(getActivity(), R.raw.kk);

        musicPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                seek.setMax(musicPlayer.getDuration());
                musicPlayer.setLooping(false);
                musicPlayer.start();
                updateSeek();
            }
        });
        TestPos s = new TestPos();
        s.execute();
//        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                if(fromUser){
//                    musicPlayer.seekTo(progress);
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
        //PlayerFragment.AsyncPlaySong runner = new PlayerFragment.AsyncPlaySong();
        //runner.execute(settings);

//
//        Button pauseButton = (Button) findViewById(R.id.stop);
//
//        musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                Toast.makeText(MainActivity.this, "The Song is Over", Toast.LENGTH_SHORT).show();
//            }
//        });
//        pauseButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
        //setImageResource(R.drawable.start);
//                musicPlayer.pause();
//            }
//        });
    }




    //TODO: 4 anazhthse to tragoudi kai paikstro
    private class AsyncPlaySong extends AsyncTask<ArrayList<String>, String, String> {
        ProgressDialog progressDialog;


        @Override
        protected String doInBackground(ArrayList<String>... arrayLists) {

            return null;
        }

        @Override
        protected void onPreExecute() {





            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /*
                    length=musicPlayer.getCurrentPosition();
                    Log.e("yag", String.valueOf(length));
                    if(length==0){
                        musicPlayer.start();
                        playButton.setImageResource(R.drawable.pause);
                        flag=!flag;
                        updateSeek();
                        return;
                    }
                    if(flag){
                        musicPlayer.pause();
                        playButton.setImageResource(R.drawable.play);
                    }else{
                        playButton.setImageResource(R.drawable.pause);
                        musicPlayer.seekTo(length);
                        musicPlayer.start();

                    }
                    updateSeek();
                    flag=!flag;
                    */

                }
            });
//            musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    playButton.setImageResource(R.drawable.play);
//                    flag=!flag;
//                    Toast.makeText(getActivity(), "The Song is Over", Toast.LENGTH_SHORT).show();
//
//                    stopPlaying(playButton);
//
//                }
//            });
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }



    }
    private void stopPlaying(final ImageButton playButton) {
        if (musicPlayer != null) {
            musicPlayer.seekTo(0);
            musicPlayer.start();
            musicPlayer.pause();
        }
    }

    public void updateSeek(){
        seek.setProgress(musicPlayer.getCurrentPosition());
        if(musicPlayer.isPlaying()){
            runnable=new Runnable() {
                @Override
                public void run() {
                    updateSeek();
                }
            };
            handler.postDelayed(runnable,100);
        }
    }

    class TestPos extends AsyncTask<Void , Void , Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            Distracks e = (Distracks) getActivity().getApplication();
            while(true) {
                Log.e("wtf", "startinga " + e.getCurrentPositionInSeconds());

                try {
                    Thread.sleep(1300);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

        }
    }
}
