package com.world.myapplication;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.file.Path;
import java.util.ArrayList;

public class SavedSongs extends Fragment {
    View rootView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.saved_songs, container, false);
        return rootView;
    }
    @Override
    public void onStart() {
        super.onStart();

       File f = new File(String.valueOf(getActivity().getFilesDir())+"/");
        File[] matchingFiles = f.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return !name.startsWith("temp")  && name.endsWith("mp3");
            }
        });
        ArrayList<MusicFileMetaData> savedMetadata = new ArrayList<MusicFileMetaData>();

        if(matchingFiles.length > 0){
            for(File file:matchingFiles){
                MusicFileMetaData musicFileMetaData = MP3Cutter.ID3(file);
                savedMetadata.add(musicFileMetaData);
            }
            ArrayList<Button> buttons = SongsUI.setSavedSongsUI(savedMetadata, getContext(), rootView);
            SongsUI.setSongOnClickListenerPlayer(buttons, savedMetadata, getActivity(), getContext(), rootView);
        }else{
            SongsUI.setNullUI("No downloaded songs.", getContext(), rootView);
        }
    }
}