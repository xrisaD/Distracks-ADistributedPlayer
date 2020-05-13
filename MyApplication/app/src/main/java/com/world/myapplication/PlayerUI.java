package com.world.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerUI {
    static private int colorBackground = Color.parseColor("#5F021F");
    static private int colorText = Color.parseColor("#ffffff");

    public static void setNullUI(String info, Context context, View rootView){
        LinearLayout first = rootView.findViewById(R.id.first);
        first.setVisibility(View.GONE);

        LinearLayout second = rootView.findViewById(R.id.second);
        second.setVisibility(View.GONE);

        LinearLayout myLayout = rootView.findViewById(R.id.player_layout);
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
    public static void setPlayerUI(ImageView imageView, byte[] imageBytes, Distracks distracks, Context context, View rootView){
        Bitmap bmp;
        //set image
        if(imageBytes != null) {
            bmp  = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        }else{
            bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.logo);
        }
        imageView.setImageBitmap(Bitmap.createScaledBitmap(bmp, bmp.getWidth(), bmp.getHeight(), false));


    }
}
