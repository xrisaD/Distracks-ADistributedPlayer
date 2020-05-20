package com.world.myapplication;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;


import androidx.fragment.app.Fragment;


import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

// result: all artist's song
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
        //get arguments from search
        artist = getArguments().getString("artist");
        Distracks distracks = (Distracks) getActivity().getApplication();
        if(distracks.lastSearch.equals(artist)) {
            if(distracks.lastSearchResult == null){
                //set null ui
                SongsUI.setNullUI("No songs for artist: "+ artist, getContext(), rootView);
            }else{
                ArrayList<Button> mySongs = SongsUI.setSearchResultUI(distracks.lastSearchResult, getContext(), rootView);
                SongsUI.setSongOnClickListener(artist ,mySongs, distracks.lastSearchResult, rootView, getActivity(), getContext());
            }
        }else{
            Log.e("artist", artist);
            if (artist != null && artist.length() > 0) {
                distracks.lastSearch = artist;
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
            //search for metadata
            ArrayList<MusicFileMetaData> musicFileMetaData = distracks.getConsumer().search(artistname);
            Distracks e = (Distracks) getActivity().getApplication();
            e.lastSearchResult = musicFileMetaData;

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
            //set UI
            Distracks distracks = (Distracks) getActivity().getApplication();
            if (result!=null && result.size() > 0) {
                // save last search result, optimization
                distracks.lastSearchResult = result;
                // show songs
                ArrayList<Button> mySongs = SongsUI.setSearchResultUI(result, getContext(), rootView);
                SongsUI.setSongOnClickListener(artist ,mySongs, result, rootView, getActivity(), getContext());

            }else{
                //set null ui
                // save last search result for optimization
                distracks.lastSearchResult = null;
                SongsUI.setNullUI("No songs for artist: "+ artist, getContext(), rootView);
            }

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }
}
