package com.world.myapplication;

import android.media.MediaPlayer;

public class SeekToAndStartWhenPrepared implements MediaPlayer.OnPreparedListener {
    int millis;
    public SeekToAndStartWhenPrepared(int millis){
        this.millis = millis;
    }
    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.seekTo(millis);
        mp.start();
    }
}
