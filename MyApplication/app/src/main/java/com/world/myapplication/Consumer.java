package com.world.myapplication;

import android.app.Application;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

public class Consumer extends Application {
    private ArrayList<Component> knownBrokers = new ArrayList<>();
    private Map<ArtistName, Component> artistToBroker = new HashMap<ArtistName, Component>();
    @Override
    public void onCreate() {
        super.onCreate();

        knownBrokers.add(new Component("192.168.1.2", 5000));
        //this.readBroker(getFilesDir().getAbsolutePath()+"brokers.txt");
    }

    //TODO:read Brokers file from resraw
    // isos xrhsimos o kato kodikas h proth grammh, isws diavasma me input stream
    public void readBroker(String fileName) {

        InputStream ins = getResources().openRawResource(
                getResources().getIdentifier("FILENAME_WITHOUT_EXTENSION",
                        "raw", getPackageName()));

        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            if (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] arrOfStr = data.split("\\s");
                String ip = arrOfStr[0];
                int port = Integer.parseInt(arrOfStr[1]);
                //add the first broker
                knownBrokers.add(new Component(ip,port));
            }
            //close reader
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    //Find the reposible broker for this artist , or, If he is not yet known return a random known Broker
    public Component getBroker(ArtistName artist){
        String ip = null;
        int port = 0;
        //try to find the responsible broker
        Component c = artistToBroker.get(artist);
        if(c!=null){
            //this consumer have done this search before
            ip = c.getIp();
            port = c.getPort();
        }//take a random broker
        else{
            System.out.println("known brokers" + knownBrokers);
            int index = new Random().nextInt(knownBrokers.size());
            ip = knownBrokers.get(index).getIp();
            port = knownBrokers.get(index).getPort();
        }
        return new Component(ip, port);
    }
    // Register the broker with ip c.getIp , port c.getPort as responsible for thie artistname
    public void register(Component c, ArtistName artist) {
        artistToBroker.put(artist,c);
        this.knownBrokers.add(c);
    }
}
