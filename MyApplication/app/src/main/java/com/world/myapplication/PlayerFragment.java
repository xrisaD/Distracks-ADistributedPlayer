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
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import java.io.IOException;

public class PlayerFragment extends Fragment {
    private View rootView;
    private MediaPlayer musicPlayer;
    private ImageButton playButton;
    private SeekBar seek;
    private Runnable runnable;
    private Handler handler;
    private String artist;
    private String song;
    private boolean flag=true;
    private int length;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.player_fragment, container, false);

        return rootView;
    }
    //TODO: 3 deksou 2 orismata 1 me to tragoudisti kai ena me to tragoudi,
    @Override
    public void onStart() {

        super.onStart();

        Bundle bundle=getArguments();
        artist=bundle.getString("artist");
        song=bundle.getString("song");

        playButton = (ImageButton) rootView.findViewById(R.id.start);
        seek = (SeekBar)  rootView.findViewById(R.id.seekbar);
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
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    musicPlayer.seekTo(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });




        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
            }
        });
        musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playButton.setImageResource(R.drawable.play);
                flag=!flag;
                Toast.makeText(getActivity(), "The Song is Over", Toast.LENGTH_SHORT).show();

                stopPlaying(playButton);

            }
        });

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

    private void stopPlaying(final ImageButton playButton) {
        if (musicPlayer != null) {
            musicPlayer.release();
            musicPlayer = null;
            musicPlayer = MediaPlayer.create(getActivity(), R.raw.kk);
            musicPlayer.setLooping(false);
            musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    playButton.setImageResource(R.drawable.play);
                    flag=!flag;
                    Toast.makeText(getActivity(), "The Song is Over", Toast.LENGTH_SHORT).show();
                    stopPlaying(playButton);

                }
            });
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
    //TODO: 4 anazhthse to tragoudi kai paikstro
    private class AsyncPlaySong extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... strings) {

            return null;
        }

        @Override
        protected void onPreExecute() {

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
}
