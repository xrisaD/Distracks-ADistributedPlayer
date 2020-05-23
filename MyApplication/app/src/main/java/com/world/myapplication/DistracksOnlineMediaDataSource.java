package com.world.myapplication;

import android.media.MediaDataSource;
import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class DistracksOnlineMediaDataSource extends MediaDataSource {
    Consumer consumer;
    MusicFileMetaData musicFileMetaData;
    volatile byte[] songDataBuffer;


    volatile boolean allChunksHaveBeenDownloaded;

    volatile Integer estimatedSize;

    public DistracksOnlineMediaDataSource(Consumer consumer , MusicFileMetaData musicFileMetaData){
        this.consumer = consumer;
        this.musicFileMetaData = musicFileMetaData;
        songDataBuffer = new byte[0];
        allChunksHaveBeenDownloaded = false;
        StreamSong streamSong = new StreamSong();
        streamSong.execute(musicFileMetaData);
    }
    //Called whenever a new chunk arrives
    private void handleChunk(MusicFile chunk){
        byte[] newBuffer = new byte[songDataBuffer.length + chunk.getMusicFileExtract().length];
        //Copying the old buffer to the new
        for(int i = 0 ; i < songDataBuffer.length ; i++){
            newBuffer[i] = songDataBuffer[i];
        }
        //Adding the new contents to the buffer
        for(int i = songDataBuffer.length; i < newBuffer.length ; i++){
            newBuffer[i] = chunk.getMusicFileExtract()[i - songDataBuffer.length];
        }
        songDataBuffer = newBuffer;
    }
    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
       // System.out.println("[MEDIADATASTOUCE] Requested " + size + "bytes at position " + position );
        //System.out.println("[MEDIADATASTOUCE] Current songbuffer size " + songDataBuffer.length );
        //Waiting until the requested bytes have been received
        while( position + size >= songDataBuffer.length){
            if(allChunksHaveBeenDownloaded && position >= songDataBuffer.length){
                //In this case all the song's data is in the buffer but the request was at the end
                // of the buffer which means we should indicate that its an end of stream
               // System.out.println("[MEDIADATASTOUCE] returbning " + -1);
                return -1;
            }

            if(allChunksHaveBeenDownloaded){
                //ALl the songs buffer is in the data nad pos < songDataBuffer.length
                //and pos + size >= songDataBuffer,length
                int j = 0;
                for(int i = (int)position ; i < songDataBuffer.length ; i++){
                    buffer[offset + j] = songDataBuffer[i];
                    j++;
                }
               // System.out.println("[MEDIADATASTOUCE] returbning " + (songDataBuffer.length - (int)position));
                return songDataBuffer.length - (int)position;
            }
        }
        //We return the requested bytes
        for(int i = offset ; i < size + offset ; i++){
            buffer[i] = songDataBuffer[(int)position + i - offset];
        }
        //System.out.println("[MEDIADATASTOUCE] returbning " + size);
        return size;
    }

    @Override
    public long getSize() throws IOException {
        //There is no need to provide an exact number , just an upperbound
        // The application never allows the user to seek beyond the duration of the song
        return  99999999999999999L;
    }

    @Override
    public void close() throws IOException {

    }

    private class StreamSong extends AsyncTask<MusicFileMetaData , Void , Void> {
        MusicFileMetaData musicFileMetaData;
        @Override
        protected Void doInBackground(MusicFileMetaData... artistAndSong) {
            musicFileMetaData = artistAndSong[0];
            //get data
            ArtistName artist = new ArtistName(musicFileMetaData.getArtistName());
            String songName = musicFileMetaData.getTrackName();

            Component b = consumer.getBroker(artist);
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
                consumer.requestPullToBroker(artist, songName, out);
                //Waiting for the reply
                in = new ObjectInputStream(s.getInputStream());
                reply = (Request.ReplyFromBroker) in.readObject();
                System.out.printf("[CONSUMER] Got reply from Broker(%s,%d) : %s%n", ip, port, reply);
                int statusCode = reply.statusCode;
                ip = reply.responsibleBrokerIp;
                port = reply.responsibleBrokerPort;
                //ask  for broker
                if(statusCode == Request.StatusCodes.NOT_RESPONSIBLE){
                    s = new Socket(ip, port);
                    //Creating the request to Broker for this artist
                    out = new ObjectOutputStream(s.getOutputStream());
                    consumer.requestPullToBroker(artist, songName, out);
                    //Waiting for the reply
                    in = new ObjectInputStream(s.getInputStream());
                    reply = (Request.ReplyFromBroker) in.readObject();
                    System.out.printf("[CONSUMER] Got reply from Broker(%s,%d) : %s%n", ip, port, reply);
                    statusCode = reply.statusCode;
                    ip = reply.responsibleBrokerIp;
                    port = reply.responsibleBrokerPort;
                    //something went wrong
                    if(statusCode == Request.StatusCodes.NOT_RESPONSIBLE){
                        Log.e("NOT_RESPONSIBLE.","Can't found responsible broker. Check your brokers' ip and port");
                        return null;
                    }
                }
                if(statusCode == Request.StatusCodes.NOT_FOUND){
                    System.out.println("Song or Artist does not exist");
                    throw new Exception("Song or Artist does not exist");
                }
                //Song exists and the broker is responsible for the artist
                else if(statusCode == Request.StatusCodes.OK){
                    //Save the information that this broker is responsible for the requested artist
                    consumer.register(new Component(s.getInetAddress().getHostAddress(),s.getPort()) , artist);
                    //download mp3 to the device
                    stream(reply.numChunks, in , out, songName);
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
            catch (Exception e){
                System.out.printf("[CONSUMER] Error on playData %s " , e.getMessage());
                e.printStackTrace();
            }
            finally {
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (s != null) s.close();
                }
                catch(Exception e){
                    System.out.printf("[CONSUMER] Error while closing socket on playData %s " , e.getMessage());
                    e.printStackTrace();
                }

            }
            return null;
        }
        private void stream(final int numChunks, ObjectInputStream in, ObjectOutputStream out, String filename) throws IOException, ClassNotFoundException {
            int size = 0;
            //Start reading chunks
            for (int i = 0; i < numChunks; i++) {
                //HandleCHunks
                Object object = in.readObject();
                if (object instanceof MusicFile) {
                    MusicFile chunk = (MusicFile) object;

                    System.out.println("[CONSUMER] got chunk Number " + i);
                    System.out.println();
                    handleChunk(chunk);
                    size += chunk.getMusicFileExtract().length;
                    //Add chunk to the icomplete lis
                }
            }
            //send the end so broker can close socket
            Request.RequestToBroker requestToBroker= new Request.RequestToBroker();
            requestToBroker.method = Request.Methods.THE_END;
            out.writeObject(requestToBroker);
            out.flush();
            allChunksHaveBeenDownloaded = true;
        }

    }
}
