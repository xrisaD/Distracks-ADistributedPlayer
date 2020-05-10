package com.world.myapplication;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;


import java.util.ArrayList;

public class SongsUI {
    static Switch download;
    static String artist;
    //color
    static private int colorBackground = Color.parseColor("#5F021F");
    static private int colorText = Color.parseColor("#ffffff");

    public static ArrayList<Button> setSearchResultUI(ArrayList<MusicFileMetaData> resultMetaData, Context context, View rootView){
        //everytime it have to be set as null to avoid wrong behavior
        download = null;

        LinearLayout myLayout = rootView.findViewById(R.id.search_layout);

        //create margin
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, 30);

        //download option Layout
        LinearLayout downloadLayout = new LinearLayout(context);
        downloadLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView downloadText = new TextView(context);
        downloadText.setText("Download");
        downloadText.setTextSize(20);
        downloadText.setTextColor(colorText);
        downloadLayout.addView(downloadText);
        //switch for download or stream
        download = new Switch(context);
        downloadLayout.addView(download);
        downloadLayout.setLayoutParams(layoutParams);
        myLayout.addView(downloadLayout);

        ArrayList<Button> mySongs = new ArrayList<Button>();
        //set padding
        int padding = 30;
        for (int i = 0; i < resultMetaData.size(); i++) {
            // create a new textview
            // Create LinearLayout
            LinearLayout newLayout = new LinearLayout(context);
            newLayout.setOrientation(LinearLayout.VERTICAL);
            newLayout.setBackgroundColor(colorBackground);

            // Add title
            // Create Button
            final Button btn = new Button(context);
            btn.setBackgroundColor(colorBackground);
            btn.setText(resultMetaData.get(i).getTrackName());
            btn.setTextSize(12);
            btn.setTextColor(colorText);
            btn.setLayoutParams (new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100));
            newLayout.addView(btn);
            mySongs.add(btn);

            //Add
            String info = "\n" + "AlbumInfo: " + resultMetaData.get(i).getAlbumInfo() + "\n"+"Genre: " + resultMetaData.get(i).getGenre();
            TextView data = new TextView(context);
            data.setText(info);
            data.setTextSize(10);
            data.setTextColor(colorText);
            newLayout.addView(data);

            newLayout.setLayoutParams(layoutParams);

            newLayout.setPadding(padding,padding,padding,padding);
            // add the textview to the linearlayout
            myLayout.addView(newLayout);

        }
        return mySongs;
    }

    public static void setNullUI(String info, Context context, View rootView){
        LinearLayout myLayout = rootView.findViewById(R.id.search_layout);
        //create margin
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, 30);
        //set padding
        int padding = 30;
        //download option Layout
        LinearLayout nullLayout = new LinearLayout(context);
        nullLayout.setOrientation(LinearLayout.HORIZONTAL);
        nullLayout.setBackgroundColor(colorBackground);
        nullLayout.setPadding(padding,padding,padding,padding);

        TextView data = new TextView(context);
        data.setText(info);
        data.setTextSize(15);
        data.setTextColor(colorText);
        nullLayout.addView(data);

        myLayout.addView(nullLayout);
    }

    public static ArrayList<Button> setSavedSongsUI(ArrayList<MusicFileMetaData> resultMetaData, Context context, View rootView){
        LinearLayout myLayout = rootView.findViewById(R.id.saved_layout);

        //create margin
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, 30);

        ArrayList<Button> mySongs = new ArrayList<Button>();
        //set padding
        int padding = 30;
        for (int i = 0; i < resultMetaData.size(); i++) {
            // create a new textview
            // Create LinearLayout
            LinearLayout newLayout = new LinearLayout(context);
            newLayout.setOrientation(LinearLayout.VERTICAL);
            newLayout.setBackgroundColor(colorBackground);

            // Add title
            // Create Button
            final Button btn = new Button(context);
            btn.setBackgroundColor(colorBackground);
            btn.setText(resultMetaData.get(i).getTrackName());
            btn.setTextSize(12);
            btn.setTextColor(colorText);
            btn.setLayoutParams (new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100));
            newLayout.addView(btn);
            mySongs.add(btn);

            //Add
            String info = "Artist name: "+resultMetaData.get(i).getArtistName() + "\n" + "AlbumInfo: " + resultMetaData.get(i).getAlbumInfo() + "\n"+"Genre: " + resultMetaData.get(i).getGenre();
            TextView data = new TextView(context);
            data.setText(info);
            data.setTextSize(10);
            data.setTextColor(colorText);
            newLayout.addView(data);

            newLayout.setLayoutParams(layoutParams);

            newLayout.setPadding(padding,padding,padding,padding);
            // add the textview to the linearlayout
            myLayout.addView(newLayout);

        }
        return mySongs;
    }



    //set songs on screen and set onclick listener
    public static void setSongOnClickListener(ArrayList<Button> mySongs, View rootView, final Activity activity, Context context) {
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
                                    Distracks distracks= (Distracks) activity.getApplication();
                                    MusicFileMetaData artistAndSong = new MusicFileMetaData();
                                    artistAndSong.setArtistName(artist);
                                    artistAndSong.setTrackName(song);

                                    distracks.download(artistAndSong);

                                }else{
                                    //StreamImmediately
                                    Distracks distracks= (Distracks) activity.getApplication();
                                    MusicFileMetaData artistAndSong = new MusicFileMetaData();
                                    artistAndSong.setArtistName(artist);
                                    artistAndSong.setTrackName(song);
                                    distracks.streamSong(artistAndSong);
                                }

                            }
                        });
            }

    }
    //set songs on screen and set onclick listener
    public static void setSongOnClickListenerPlayer(ArrayList<Button> mySongs, View rootView, final Activity activity, Context context) {
        for(Button b: mySongs){
            b.setOnClickListener(
                    new View.OnClickListener()
                    {
                        public void onClick(View view)
                        {
                            Button thisBtn = (Button) view;
                            String song = thisBtn.getText().toString();

                            //StreamImmediately
                            Distracks distracks= (Distracks) activity.getApplication();
                            MusicFileMetaData artistAndSong = new MusicFileMetaData();
                            artistAndSong.setArtistName(artist);
                            artistAndSong.setTrackName(song);

                            distracks.streamSong(artistAndSong);
                        }
                    });
        }

    }
}
