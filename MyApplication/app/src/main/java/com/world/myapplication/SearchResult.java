package com.world.myapplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
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
        //color
        int colorBackground = Color.parseColor("#5F021F");
        int colorText = Color.parseColor("#ffffff");

        //download option Layout
        LinearLayout downloadLayout = new LinearLayout(getContext());
        downloadLayout.setOrientation(LinearLayout.HORIZONTAL);


        TextView downloadText = new TextView(getContext());
        downloadText.setText("Download");
        downloadText.setTextSize(30);
        downloadText.setTextColor(colorText);
        downloadLayout.addView(downloadText);
        //switch for download or stream
        Switch download = new Switch(getContext());
        downloadLayout.addView(download);
        myLayout.addView(downloadLayout);


        ArrayList<LinearLayout> mySongs = new ArrayList<LinearLayout>();
        final int N = 10; // total number of textviews to add

        for (int i = 0; i < N; i++) {
            // create a new textview
            // Create LinearLayout
            LinearLayout newLayout = new LinearLayout(getContext());
            newLayout.setOrientation(LinearLayout.VERTICAL);
            newLayout.setBackgroundColor(colorBackground);

            // Add title
            TextView title = new TextView(getContext());
            title.setText("Title "+i);
            title.setTextSize(30);
            title.setTextColor(colorText);
            newLayout.addView(title);

            //Add
            TextView data = new TextView(getContext());
            data.setText("data");
            data.setTextSize(10);
            data.setTextColor(colorText);
            newLayout.addView(data);

            // Create Button
            final Button btn = new Button(getContext());
            newLayout.addView(btn);
            btn.setBackgroundColor(colorBackground);
            btn.setText("TITLE");
            // add the textview to the linearlayout
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
