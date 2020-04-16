package com.world.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

//result: all artist's song
public class SearchResult extends Fragment {
    private View rootView;
    private String artist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.search_result, container, false);
        //get argmunets from search
        artist = getArguments().getString ("artist", "");
        //search for songs
        //AsyncSearchResult runner = new AsyncSearchResult();
        //runner.execute();

        LinearLayout myLayout = rootView.findViewById(R.id.search_layout);
        int colorBackground = Color.parseColor("#5F021F");
        ArrayList<LinearLayout> mySongs = new ArrayList<LinearLayout>();
        final int N = 100; // total number of textviews to add

        for (int i = 0; i < N; i++) {
            // create a new textview
            // Create LinearLayout
            LinearLayout newLayout = new LinearLayout(getContext());
            newLayout.setOrientation(LinearLayout.HORIZONTAL);
            newLayout.setBackgroundColor(colorBackground);

            // Add title
            TextView title = new TextView(getContext());
            title.setText("TITLE");
            title.setTextSize(100);
            newLayout.addView(title);

            //Add
            TextView data = new TextView(getContext());
            title.setText("data");
            data.setTextSize(70);
            newLayout.addView(data);

            // Create Button
            final Button btn = new Button(getContext());

            // add the textview to the linearlayout
            myLayout.addView(btn);
            myLayout.addView(newLayout);

            // save a reference to the textview for later
            //myTextViews[i] = rowTextView;
        }

        return rootView;
    }

    private class AsyncSearchResult extends AsyncTask<String, String, String> {
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... strings) {
            //TODO: Search for metadata
            return null;
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getContext(),
                    "ProgressDialog",
                    "Searching for "+ artist + "...");
        }

    }

}
