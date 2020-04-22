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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigInteger;
import java.net.Socket;
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
    private String ip;
    private int port;
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
        ip=bundle.getString("ip");
        port=bundle.getInt("port");

        playButton = (ImageButton) rootView.findViewById(R.id.start);
        seek = (SeekBar)  rootView.findViewById(R.id.seekbar);
        titleText = (TextView) rootView.findViewById(R.id.artistsong);
        titleText.setText(artist+ " - "+song);
        handler=new Handler();
        musicPlayer = MediaPlayer.create(getActivity(), R.raw.kk);
        PlayerFragment.AsyncPlaySong runner = new PlayerFragment.AsyncPlaySong();
        ArrayList<String> settings = new ArrayList<>();
        settings.add(ip);
        settings.add(String.valueOf(port));
        settings.add(artist);
        settings.add(song);
        runner.execute(settings);

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
            // [0]:ip [1]:port [2]:artist [3]:song [4]
            ArrayList<String> settings = arrayLists[0];
            String ip=settings.get(0);
            int port = Integer.valueOf(settings.get(1));
            String artist = settings.get(2);
            String songName = settings.get(3);
            Socket s = null;
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            try {
                //While we find a broker who is not responsible for the artistname
                Request.ReplyFromBroker reply=null;
                s = new Socket(ip, port);
                    //Creating the request to Broker for this artist
                    out = new ObjectOutputStream(s.getOutputStream());
                    requestPullToBroker(new ArtistName(artist), songName, out);
                    //Waiting for the reply
                    in = new ObjectInputStream(s.getInputStream());
                    reply = (Request.ReplyFromBroker) in.readObject();
                    Log.e("test", ip + port + reply);
                    ip = reply.responsibleBrokerIp;
                    port = reply.responsibleBrokerPort;
                    int numChunks = reply.numChunks;

                //Song exists and the broker is responsible for the artist

                    int size = 0;

                    //Utilities util=new Utilities();
                    for (int i = 0; i <= numChunks; i++) {
                        //HandleCHunks
                        MusicFile chunk = (MusicFile) in.readObject();
                        size += chunk.getMusicFileExtract().length;
                        //BigInteger brokermd5=util.getMd5(chunk.getMusicFileExtract());
                        //System.out.println(chunk.biggie.compareTo(brokermd5)+"   COMPARE UP TO CHUNK CONSUMER"+i);
                        Log.e("check", i +" " + String.valueOf(size));
                        //Add chunk to the icomplete list

                        //mp.addChunk(chunk);
                    }


            }
            catch(ClassNotFoundException e){
                //Protocol Error (Unexpected Object Caught) its a protocol error
                System.out.printf("[CONSUMER] Unexpected object on playData %s " , e.getMessage());
            }
            catch (IOException e){
                System.out.printf("[CONSUMER] Error on playData %s " , e.getMessage());
            }
            finally {
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (s != null) s.close();
                }
                catch(Exception e){
                    System.out.printf("[CONSUMER] Error while closing socket on playData %s " , e.getMessage());
                }

            }
            return null;
        }

        @Override
        protected void onPreExecute() {
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
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        private void requestPullToBroker(ArtistName artist, String songName, ObjectOutputStream out) throws IOException {
            Request.RequestToBroker request = new Request.RequestToBroker();
            request.method = Request.Methods.PULL;
            request.pullArtistName = artist.getArtistName();
            request.songName = songName;
            out.writeObject(request);
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

        public void playData(ArtistName artist, String  songName , boolean download) throws Exception {

            //set Broker's ip and port

        }

    }
}
