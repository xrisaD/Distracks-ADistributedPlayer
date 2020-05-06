package com.world.myapplication;

import android.app.Application;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.FragmentTransaction;

import java.util.ArrayList;


public class Distracks extends Application {
    private Consumer consumer;


    @Override
    public void onCreate() {
        super.onCreate();
        consumer = new Consumer();
        consumer.addBroker(new Component("192.168.1.13", 5000));
        //this.readBroker(getFilesDir().getAbsolutePath()+"brokers.txt");
    }

    public Consumer getConsumer() {
        return consumer;
    }
}
