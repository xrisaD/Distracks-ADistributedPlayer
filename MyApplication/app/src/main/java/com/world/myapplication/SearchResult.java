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

    private View rootView;
    private String artist;
    Switch download;

    Socket s = null;
    ObjectInputStream in = null;
    ObjectOutputStream out = null;
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
        //get argmunets from search
        artist = getArguments().getString("artist");
        Distracks distracks = (Distracks) getActivity().getApplication();
        if(distracks.lastSearch.equals(artist)) {
            if(distracks.lastSearchResult == null){
                //set null ui
                SongsUI.setNullUI("No songs for artist: "+ artist, getContext(), rootView);
            }else{
                ArrayList<Button> mySongs = SongsUI.setSearchResultUI(distracks.lastSearchResult, getContext(), rootView);
                SongsUI.setSongOnClickListener(artist ,mySongs, rootView, getActivity(), getContext());
            }
        }else{
            Log.e("artist", artist);
            if (artist != null && artist.length() > 0) {
                //search for songs
                AsyncSearchResult runnerSearch = new AsyncSearchResult();
                runnerSearch.execute(new ArtistName(artist));
            } else {
                SongsUI.setNullUI("No songs for artist: " + artist, getContext(), rootView);
            }
        }
    }
    private class AsyncSearchResult extends AsyncTask<ArtistName, String, ArrayList<MusicFileMetaData>> {
        ProgressDialog progressDialog;

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
        protected void onPostExecute(ArrayList<MusicFileMetaData> result) {
            progressDialog.dismiss();
            Distracks distracks = (Distracks) getActivity().getApplication();
            if (result!=null && result.size() > 0) {
                distracks.lastSearchResult = result;
                ArrayList<Button> mySongs = SongsUI.setSearchResultUI(result, getContext(), rootView);
                SongsUI.setSongOnClickListener(artist ,mySongs, rootView, getActivity(), getContext());

            }else{
                //set null ui
                SongsUI.setNullUI("No songs for artist: "+ artist, getContext(), rootView);
                distracks.lastSearchResult = null;
            }

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }
}
