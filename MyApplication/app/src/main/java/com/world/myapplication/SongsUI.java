package com.world.myapplication;

import java.util.Base64;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;


import androidx.navigation.Navigation;

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
            newLayout.setOrientation(LinearLayout.HORIZONTAL);
            newLayout.setBackgroundColor(colorBackground);

            //image layout
            RelativeLayout leftLayout = new RelativeLayout(context);
            leftLayout.setGravity(Gravity.LEFT);

            ImageView imgView = new ImageView(context);
            Bitmap bmp = null;
            if(resultMetaData.get(i).getImage()!=null){
                bmp = BitmapFactory.decodeByteArray(resultMetaData.get(i).getImage(), 0, resultMetaData.get(i).getImage().length);
            }else{
                bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
            }
            imgView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 200, 200, false));
            leftLayout.addView(imgView);

            RelativeLayout rightLayout = new RelativeLayout(context);
            rightLayout.setGravity(Gravity.RIGHT);

            LinearLayout dataLayout = new LinearLayout(context);
            dataLayout.setOrientation(LinearLayout.VERTICAL);

            // Add title
            // Create Button
            final Button btn = new Button(context);
            btn.setBackgroundColor(colorBackground);
            btn.setText(resultMetaData.get(i).getTrackName());
            btn.setTextSize(12);
            btn.setTextColor(colorText);
            btn.setLayoutParams (new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100));
            dataLayout.addView(btn);
            mySongs.add(btn);

            //Add
            String info = "\n" + "AlbumInfo: " + resultMetaData.get(i).getAlbumInfo() + "\n"+"Genre: " + resultMetaData.get(i).getGenre();
            TextView data = new TextView(context);
            data.setText(info);
            data.setTextSize(10);
            data.setTextColor(colorText);
            dataLayout.addView(data);

            dataLayout.setPadding(padding, 0, 0, 0);

            rightLayout.addView(dataLayout);

            newLayout.addView(leftLayout);
            newLayout.addView(rightLayout);
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

        if(myLayout != null) myLayout.addView(nullLayout);
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

            // Create LinearLayout
            LinearLayout newLayout = new LinearLayout(context);
            newLayout.setOrientation(LinearLayout.HORIZONTAL);
            newLayout.setBackgroundColor(colorBackground);

            //image layout
            RelativeLayout leftLayout = new RelativeLayout(context);
            leftLayout.setGravity(Gravity.LEFT);

            ImageView imgView = new ImageView(context);
            Bitmap bmp = null;
            if(resultMetaData.get(i).getImage()!=null){
                 bmp = BitmapFactory.decodeByteArray(resultMetaData.get(i).getImage(), 0, resultMetaData.get(i).getImage().length);
            }else{
                bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
            }
            imgView.setImageBitmap(Bitmap.createScaledBitmap(bmp, 200, 200, false));
            leftLayout.addView(imgView);


            RelativeLayout rightLayout = new RelativeLayout(context);
            rightLayout.setGravity(Gravity.RIGHT);

            LinearLayout dataLayout = new LinearLayout(context);
            dataLayout.setOrientation(LinearLayout.VERTICAL);

            // Add title
            // Create Button
            final Button btn = new Button(context);
            btn.setBackgroundColor(colorBackground);
            btn.setText(resultMetaData.get(i).getTrackName());
            btn.setTextSize(12);
            btn.setTextColor(colorText);
            btn.setLayoutParams (new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100));
            dataLayout.addView(btn);
            mySongs.add(btn);

            //Add
            String info = "Artist name: "+resultMetaData.get(i).getArtistName() + "\n" + "AlbumInfo: " + resultMetaData.get(i).getAlbumInfo() + "\n"+"Genre: " + resultMetaData.get(i).getGenre();
            TextView data = new TextView(context);
            data.setText(info);
            data.setTextSize(10);
            data.setTextColor(colorText);
            dataLayout.addView(data);
            dataLayout.setPadding(padding, 0, 0, 0);

            rightLayout.addView(dataLayout);

            newLayout.addView(leftLayout);
            newLayout.addView(rightLayout);
            newLayout.setLayoutParams(layoutParams);
            newLayout.setPadding(padding,padding,padding,padding);


            // add the textview to the linearlayout
            myLayout.addView(newLayout);

        }
        return mySongs;
    }



    //set songs on screen and set onclick listener
    public static void setSongOnClickListener(final String artist, ArrayList<Button> mySongs, final ArrayList<MusicFileMetaData> result, View rootView, final Activity activity, final Context context) {
            //get switch
            if(download!=null) {
                download = SongsUI.download;
            }else{
                download.setChecked(false);
            }

        for(int i = 0; i < mySongs.size(); i++){
            final int finalI = i;
                mySongs.get(i).setOnClickListener(
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

                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean("offline", false);
                                    bundle.putString("artist_name", artist);
                                    bundle.putString("song_name", song);

                                    byte[] image = result.get(finalI).getImage();
                                    String encodeBytes = "1";
                                    if(image!=null && image.length>0) {
                                        encodeBytes = Base64.getEncoder().encodeToString(image);
                                    }
                                    bundle.putString("image", encodeBytes);
                                    bundle.putLong("duration", result.get(finalI).getDuration());
                                    Navigation.findNavController(view).navigate(R.id.result_to_player, bundle);
                                }

                            }
                        });
            }

    }
    //set songs on screen and set onclick listener
    public static void setSongOnClickListenerPlayer(ArrayList<Button> mySongs, final ArrayList<MusicFileMetaData> savedMetadata, final Activity activity, final Context context, View rootView) {

        for(int i = 0; i < mySongs.size(); i++){
            final int finalI = i;
            mySongs.get(i).setOnClickListener(
                    new View.OnClickListener()
                    {
                        public void onClick(View view)
                        {
                            Button thisBtn = (Button) view;
                            String song = thisBtn.getText().toString();

                            //StreamImmediately
                            //search for artist's songs
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("offline", true);
                            bundle.putString("path", savedMetadata.get(finalI).getPath());
                            bundle.putString("artist_name", savedMetadata.get(finalI).getArtistName());
                            bundle.putString("song_name", savedMetadata.get(finalI).getTrackName());
                            byte[] image = savedMetadata.get(finalI).getImage();
                            String encodeBytes = "1";
                            if(image!=null && image.length>0) {
                                encodeBytes = Base64.getEncoder().encodeToString(image);
                            }
                            bundle.putString("image", encodeBytes);
                            bundle.putLong("duration", savedMetadata.get(finalI).getDuration());
                            Navigation.findNavController(view).navigate(R.id.saved_to_player, bundle);
                        }
                    });
        }

    }
}
