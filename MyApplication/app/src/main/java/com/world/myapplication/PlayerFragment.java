package com.world.myapplication;

import java.util.Base64;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.media.MediaPlayer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class PlayerFragment extends Fragment {
    private Distracks distracks;
    private View rootView;
    private MediaPlayer musicPlayer;
    private ImageButton playButton;
    private TextView titleText;
    private TextView totalAmount;
    private TextView timeNow;
    private SeekBar seek;
    private Runnable runnable;
    private Handler handler;
    private ImageView imageView;
    private String artist;
    private String song;
    private byte[] imageBytes;
    private long duration;
    private boolean flag=true;
    private int length;
    //TODO: na asxolithoume me to interface edw!
    public static boolean isPlaying = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.player_fragment, container, false);
        return rootView;
    }
    @Override
    public void onStop(){
        super.onStop();
        isPlaying = false;
        Log.e("PLAYER" , "IS STOPPED");
    }
    @Override
    public void onStart() {
        isPlaying = true;
        super.onStart();

        distracks = (Distracks) getActivity().getApplication();
        seek = (SeekBar)  rootView.findViewById(R.id.seekbar);
        timeNow = (TextView) rootView.findViewById(R.id.continuous);
        timeNow.setText(converter(0));
        totalAmount = (TextView) rootView.findViewById(R.id.total);

        //get arguments
        if(getArguments() != null){
            boolean offline = getArguments().getBoolean("offline");
            if (offline) {
                //offline mode
                String path = getArguments().getString("path"); //get song's path
                artist= getArguments().getString("artist_name");
                song = getArguments().getString("song_name");
                String image  = getArguments().getString("image");
                duration = getArguments().getLong("duration");

                if(!image.equals("1")) { //default value
                    imageBytes = Base64.getDecoder().decode(image);
                }

                seek.setEnabled(true);
                seek.setMax((int)duration);
                distracks.setState(artist, song, imageBytes, duration);

                distracks.streamSongOffline(path);
            } else {
                //online mode
                artist= getArguments().getString("artist_name");
                song = getArguments().getString("song_name");
                String image  = getArguments().getString("image");
                duration = getArguments().getLong("duration");
                if(!image.equals("1")) { //default value
                    imageBytes = Base64.getDecoder().decode(image);
                }
                seek.setEnabled(true);
                seek.setMax((int)duration);
                distracks.setState(artist, song, imageBytes, duration);
                distracks.streamSongOnline(artist, song);
            }
        }//playNow
        else{
            if(!distracks.isStateNull()){
                //get current state
                artist = distracks.playNowArtist;
                song = distracks.playNowSong;
                imageBytes = distracks.imageBytesNow;
                duration = distracks.duration;
//                if(distracks.currentlyStreamingOnline){
//                    seek.setEnabled(false);
//                }else {
//                    seek.setEnabled(true);
//                }
                seek.setMax((int)duration);
            }else{
                //Screan: null
                PlayerUI.setNullUI("Nothing is playing now", getContext(), rootView);
                return;
            }
        }

        ImageView imageView = (ImageView) rootView.findViewById(R.id.album_image);
        playButton = (ImageButton) rootView.findViewById(R.id.start);
        totalAmount.setText(converter((int) duration));
        PlayerUI.setPlayerUI(imageView, imageBytes, distracks, getContext(), rootView);
        UpdatePlayerPosition updatePlayerPosition = new UpdatePlayerPosition();
        updatePlayerPosition.execute();

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

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.e("clicked", String.valueOf(progress));
                if(fromUser) {
                    distracks.seekTo(progress);
                    Log.e("clicked", "i just clicked");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        titleText = (TextView) rootView.findViewById(R.id.artistsong);
        titleText.setText(artist+ " - "+song);
        handler=new Handler();
        Distracks application= (Distracks) getActivity().getApplication();
        application.player = this;

    }

    public String converter(int millis){
        int minutes = (millis % 3600) / 60;
        int seconds = millis % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }
    public void updateSeconds(int seconds){
        seek.setProgress(seconds);
        timeNow.setText(converter(seconds));
    }
    public void setter(int seconds){
        timeNow.setText(converter(seconds));
    }
    public void updateSeek(){
        seek.setProgress(distracks.getCurrentPositionInSeconds());
        Log.e("in","in and "+distracks.getCurrentPositionInSeconds());
            runnable=new Runnable() {
                @Override
                public void run() {
                    updateSeek();
                }
            };
            handler.postDelayed(runnable,100);

    }

    class UpdatePlayerPosition extends AsyncTask<Void , Integer, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            int seconds=0;
            while(true) {
                if(getActivity() != null){
                    Distracks e = (Distracks) getActivity().getApplication();
                    seconds=e.getCurrentPositionInSeconds();
                }
                publishProgress(seconds);
                try {
                    Thread.sleep(600);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
                if(!isPlaying){
                    break;
                }
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            System.out.println("Updating seek to " + values[0]);
            updateSeconds(values[0]);

        }
    }
    public void enableSeekBar(){
        this.seek.setEnabled(true);
    }

}
