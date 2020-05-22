package com.world.myapplication;


import android.util.Log;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Consumer {

    private ArrayList<Component> knownBrokers = new ArrayList<>();
    private Map<ArtistName, Component> artistToBroker = new HashMap<ArtistName, Component>();

    private ArrayList<MusicFile> chunks = new ArrayList<>();
    public Consumer(){}

    // Register the broker with ip c.getIp , port c.getPort as responsible for thie artistname
    public void register(Component c, ArtistName artist) {
        artistToBroker.put(artist, c);
        this.knownBrokers.add(c);
    }

    //add broker to known brokers
    public void addBroker(Component c){
        knownBrokers.add(c);
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
    //REQUESTS TO BROKER

    //Send a pull request to the broker at the end of the stream
    public void requestPullToBroker(ArtistName artist, String songName, ObjectOutputStream out) throws IOException {
        Request.RequestToBroker request = new Request.RequestToBroker();
        request.method = Request.Methods.PULL;
        request.pullArtistName = artist.getArtistName();
        request.songName = songName;
        out.writeObject(request);
    }
    // Send a search request to the broker at the end of the outputstream
    private void requestSearchToBroker(ArtistName artist, ObjectOutputStream out) throws IOException {
        Request.RequestToBroker request = new Request.RequestToBroker();
        request.method = Request.Methods.SEARCH;
        request.pullArtistName = artist.getArtistName();
        out.writeObject(request);
    }

    // Search for an artists and return all the metadata of the artist's songs
    public ArrayList<MusicFileMetaData> search(ArtistName artist){
        Component b = getBroker(artist);
        //set Broker's ip and port
        String ip = b.getIp();
        int port = b.getPort();
        Socket s = null;
        ObjectInputStream in = null;
        ObjectOutputStream out = null;
        try {
            //While we find a broker who is not responsible for the artistname
            Request.ReplyFromBroker reply=null;
            s = new Socket(ip, port);
            //Creating the request to Broker for this artist
            out = new ObjectOutputStream(s.getOutputStream());
            //search for artist's metadata
            requestSearchToBroker(artist, out);
            //Waiting for the reply
            in = new ObjectInputStream(s.getInputStream());
            reply = (Request.ReplyFromBroker) in.readObject();
            System.out.printf("[CONSUMER] Got reply from Broker(%s,%d) : %s", ip, port, reply);
            int statusCode = reply.statusCode;
            ip = reply.responsibleBrokerIp;
            port = reply.responsibleBrokerPort;

            //if everything ok, this message will go to the responsible broker
            if(statusCode == Request.StatusCodes.NOT_RESPONSIBLE){
                                s = new Socket(ip, port);
                //Creating the request to Broker for this artist
                out = new ObjectOutputStream(s.getOutputStream());
                //search for artist's metadata
                requestSearchToBroker(artist, out);
                //Waiting for the reply
                in = new ObjectInputStream(s.getInputStream());
                reply = (Request.ReplyFromBroker) in.readObject();
                System.out.printf("[CONSUMER] Got reply from Broker(%s,%d) : %s", ip, port, reply);
                statusCode = reply.statusCode;
                ip = reply.responsibleBrokerIp;
                port = reply.responsibleBrokerPort;
                //something went wrong
                if(statusCode == Request.StatusCodes.NOT_RESPONSIBLE){
                    // reply with the_end so broker can close the socket
                    Log.e("NOT_RESPONSIBLE.","Can't found responsible broker. Check your brokers ip and port");
                    return null;
                }
            }
            if(statusCode == Request.StatusCodes.NOT_FOUND){
                // reply with the_end so broker can close the socket
                Request.RequestToBroker requestToBroker= new Request.RequestToBroker();
                requestToBroker.method = Request.Methods.THE_END;
                out.writeObject(requestToBroker);
                out.flush();
                System.out.println("Song or Artist does not exist");
                throw new Exception("Song or Artist does not exist");
            }
            //Song exists and the broker is responsible for the artist
            else if(statusCode == Request.StatusCodes.OK){
                //Save the information that this broker is responsible for the requested artist
                register(new Component(s.getInetAddress().getHostAddress(),s.getPort()) , artist);
                //get MetaData of songs
                ArrayList<MusicFileMetaData> metaData = reply.metaData;
                // print meta data
                int i = 0;
                for(MusicFileMetaData song: metaData){
                    System.out.println("Song with number: "+ (i++) +" is "+song.getTrackName());
                }
                // reply with the_end so broker can close the socket
                Request.RequestToBroker requestToBroker= new Request.RequestToBroker();
                requestToBroker.method = Request.Methods.THE_END;
                out.writeObject(requestToBroker);
                out.flush();
                return reply.metaData;
            }
            //In this case the status code is MALFORMED_REQUEST
            else{
                System.out.println("MALFORMED_REQUEST");
                throw new Exception("MALFORMED_REQUEST");
            }

        }
        catch(ClassNotFoundException e){
            //Protocol Error (Unexpected Object Caught) its a protocol error
            System.out.printf("[CONSUMER] Unexpected object on playData %s " , e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e){
            System.out.printf("[CONSUMER] Error on playData %s " , e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            e.printStackTrace();
        }

        try {
            if (s != null) s.close();
        }
        catch(Exception e){
            System.out.printf("[CONSUMER] Error while closing socket on playData %s " , e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}