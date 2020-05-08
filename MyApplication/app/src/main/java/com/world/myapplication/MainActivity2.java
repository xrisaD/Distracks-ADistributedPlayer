package com.world.myapplication;

import android.os.AsyncTask;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MainActivity2 extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AsyncDownload asyncDownload = new AsyncDownload();
        asyncDownload.execute();
    }
    @Override
    protected void onStart() {
        super.onStart();

    }
    public class AsyncDownload extends AsyncTask<MusicFileMetaData, Integer, String> {

        @Override
        protected String doInBackground(MusicFileMetaData... artistAndSong) {

            Distracks distracks = ((Distracks) getApplication());
            File path = getFilesDir();
            distracks.getConsumer().setPath(path);
            try {
                distracks.getConsumer().playData(new ArtistName("Komiku"),"A good bass for gambling"  , true);
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                LocalDateTime now = LocalDateTime.now();
                System.out.println(dtf.format(now));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }


    }
}
