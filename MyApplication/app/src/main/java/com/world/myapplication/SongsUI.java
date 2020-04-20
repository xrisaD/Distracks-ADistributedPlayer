package com.world.myapplication;


import android.content.Context;
import android.graphics.Color;
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

    public static ArrayList<Button> setUI(String artist, ArrayList<MusicFileMetaData> resultMetaData, Context context, View rootView){
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
        //click buttons
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
}
