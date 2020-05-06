package com.world.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;


import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

//result: all artist's song
public class SearchResult extends Fragment {

    private static final String CHANNEL_ID = "BasicChannel";

    private View rootView;
    private String artist;
    Switch download;

    Socket s = null;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
    AsyncDownload runner;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.search_result, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        createNotificationChannel();
        //get argmunets from search
        artist = getArguments().getString ("artist", "");
        //search for songs
        AsyncSearchResult runnerSearch = new AsyncSearchResult();
        runnerSearch.execute(new ArtistName(artist));
    }
    private class AsyncSearchResult extends AsyncTask<ArtistName, String, ArrayList<MusicFileMetaData>> {
        ProgressDialog progressDialog;
        String BrokerIp;
        int BrokerPort;

        @Override
        protected ArrayList<MusicFileMetaData> doInBackground(ArtistName... params) {
            ArtistName artistname = params[0];
            Distracks distracks = ((Distracks) getActivity().getApplication());
            ArrayList<MusicFileMetaData> musicFileMetaData = distracks.getConsumer().search(artistname);
            return musicFileMetaData;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getContext(),
                    "ProgressDialog",
                    "Searching for "+ artist + "...");
        }
        @Override
        protected void onPostExecute(ArrayList<MusicFileMetaData> s) {
            progressDialog.dismiss();
            setSongs(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }


    public class AsyncDownload extends AsyncTask<MusicFileMetaData, Integer, String> {
        int PROGRESS_MAX;
        int PROGRESS_CURRENT;
        int notificationId;
        NotificationManagerCompat notificationManager;
        NotificationCompat.Builder builder;

        @Override
        protected String doInBackground(MusicFileMetaData... artistAndSong) {

            MusicFileMetaData musicFileMetaData = artistAndSong[0];
            Distracks distracks = ((Distracks) getActivity().getApplication());
            File path = getContext().getFilesDir();
            distracks.getConsumer().setPath(path);

            try {
                distracks.getConsumer().playData(new ArtistName(musicFileMetaData.getArtistName()), musicFileMetaData.getTrackName() , true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            /*notificationManager = NotificationManagerCompat.from(getContext());
            builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_audiotrack_light)
                    .setContentTitle("Download Song")
                    .setContentText("Download in progress...")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            PROGRESS_MAX = 100;
            PROGRESS_CURRENT = 0;
            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            notificationId = 1;
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(1, builder.build());*/

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            /*if(s.equals("ok")){
                builder.setContentText("Download complete")
                        .setProgress(0, 0, false);
                notificationManager.notify(notificationId, builder.build());
            }else{
                builder.setContentText("Can't download")
                        .setProgress(0, 0, false);
                notificationManager.notify(notificationId, builder.build());
            }*/
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            /*if(values[0]>0) {
                PROGRESS_CURRENT = values[0];
                builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            }
            notificationManager.notify(notificationId, builder.build());*/
        }
    }
    private void setSongs(ArrayList<MusicFileMetaData> resultMetaData) {

        if(resultMetaData!=null && resultMetaData.size()>0){
            Log.e("size of meta data songs", String.valueOf(resultMetaData.size()));
            ArrayList<Button> mySongs = SongsUI.setUI(artist, resultMetaData, getContext(), rootView);
            //get switch
            download = SongsUI.download;

            for(Button b: mySongs){
                b.setOnClickListener(
                        new View.OnClickListener()
                        {
                            public void onClick(View view)
                            {
                                Button thisBtn = (Button) view;
                                String song = thisBtn.getText().toString();
                                if(download.isChecked()){
                                    //download async: download song
                                    runner = new SearchResult.AsyncDownload();
                                    MusicFileMetaData artistAndSong = new MusicFileMetaData();
                                    artistAndSong.setArtistName(artist);
                                    artistAndSong.setTrackName(song);
                                    runner.execute(artistAndSong);

                                }else{
                                    //go to player fragment
                                    PlayerFragment playerFragment = new PlayerFragment();
                                    Bundle args = new Bundle();
                                    args.putString("artist", artist);
                                    args.putString("song", song);
                                    playerFragment.setArguments(args);
                                    FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                    transaction.replace(R.id.nav_host_fragment, playerFragment);
                                    transaction.addToBackStack(null);
                                    transaction.commit();
                                }

                            }
                        });
            }
        }else{
            //set null ui
            Log.e("null","null");
            SongsUI.setNullUI("No songs for artist: "+ artist,getContext(), rootView);
        }
    }



    private void createNotificationChannel() {
        String channel_name = "Basic chanel";
        String channel_description = "Download";
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = channel_name;
            String description = channel_description;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
