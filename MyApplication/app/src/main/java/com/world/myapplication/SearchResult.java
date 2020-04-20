package com.world.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

//result: all artist's song
public class SearchResult extends Fragment {
    private static final String CHANNEL_ID = "BasicChannel";
    private View rootView;
    private String artist;
    Switch download;
    private ArrayList<MusicFileMetaData> resultMetaData = new ArrayList<MusicFileMetaData>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.search_result, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        createNotificationChannel();
        //get argmunets from search
        artist = getArguments().getString ("artist", "");
        //search for songs
        AsyncSearchResult runner = new AsyncSearchResult();
        runner.execute(artist);
    }

    private class AsyncSearchResult extends AsyncTask<String, String, ArrayList<MusicFileMetaData>> {
        ProgressDialog progressDialog;

        @Override
        protected ArrayList<MusicFileMetaData> doInBackground(String... params) {
            String artistname = params[0];
            Consumer c = ((Consumer) getActivity().getApplication());
            ArtistName artist = new ArtistName(artistname);
            Component b = c.getBroker(artist);

            //set Broker's ip and port
            String ip = b.getIp();
            int port = b.getPort();
            Socket s = null;
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            try {
                //While we find a broker who is not responsible for the artistname
                Request.ReplyFromBroker reply=null;
                int statusCode = Request.StatusCodes.NOT_RESPONSIBLE;
                while(statusCode == Request.StatusCodes.NOT_RESPONSIBLE){
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
                }
                if(statusCode == Request.StatusCodes.NOT_FOUND){
                    System.out.println("Song or Artist does not exist");
                    throw new Exception("Song or Artist does not exist");
                }
                //Song exists and the broker is responsible for the artist
                else if(statusCode == Request.StatusCodes.OK){

                    //Save the information that this broker is responsible for the requested artist
                    c.register(new Component(s.getInetAddress().getHostAddress(),s.getPort()) , artist);
                    //get MetaData of songs
                    ArrayList<MusicFileMetaData> metaData = reply.metaData;
                    int i = 0;
                    for(MusicFileMetaData song: metaData){
                        System.out.println("Song with number: "+ (i++) +" is "+song.getTrackName());
                    }

                    return reply.metaData;
                }
                //In this case the status code is MALFORMED_REQUEST
                else{
                    System.out.println("MALFORMED_REQUEST");
                    throw new Exception("MALFORMED_REQUEST");
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                try {
                    if (s != null) s.close();
                } catch (Exception e) {
                    System.out.printf("[CONSUMER] Error while closing socket on search %s ", e.getMessage());
                }
            }
            return new ArrayList<MusicFileMetaData>();
        }

        // Send a search request to the broker at the end of the outputstream
        private void requestSearchToBroker(ArtistName artist, ObjectOutputStream out) throws IOException {
            Request.RequestToBroker request = new Request.RequestToBroker();
            request.method = Request.Methods.SEARCH;
            request.pullArtistName = artist.getArtistName();
            out.writeObject(request);
        }


        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(getContext(),
                    "ProgressDialog",
                    "Searching for "+ artist + "...");
        }
        @Override
        protected void onPostExecute(ArrayList<MusicFileMetaData> s) {
            progressDialog.dismiss();
            if(s.size()>0){
                ArrayList<Button> mySongs = SongsUI.setUI(artist,s,getContext(), rootView);
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
                                        SearchResult.AsyncDownload runner = new SearchResult.AsyncDownload();
                                        MusicFileMetaData artistAndSong = new MusicFileMetaData();
                                        artistAndSong.setArtistName(artist);
                                        artistAndSong.setTrackName(song);
                                        runner.execute(artistAndSong);

                                    }else{
                                        //go to player fragment
                                        PlayerFragment playerFragment = new PlayerFragment();
                                        Bundle args = new Bundle();
                                        args.putString("artist", artist);
                                        args.putString("song", song);
                                        playerFragment.setArguments(args);
                                        FragmentTransaction transaction = getFragmentManager().beginTransaction();
                                        transaction.replace(R.id.nav_host_fragment, playerFragment);
                                        transaction.addToBackStack(null);
                                        transaction.commit();
                                    }

                                }
                            });
                }
            }else{
                //set null ui
                Log.e("null","null");
                SongsUI.setNullUI("No songs for artist: "+ artist,getContext(), rootView);
            }

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    //TODO: 2 Download Song
    private class AsyncDownload extends AsyncTask<MusicFileMetaData, Integer, String> {
        int PROGRESS_MAX;
        int PROGRESS_CURRENT;
        int notificationId;
        NotificationManagerCompat notificationManager;
        NotificationCompat.Builder builder;
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        protected String doInBackground(MusicFileMetaData... artistAndSong) {
            MusicFileMetaData artistMusicFile = artistAndSong[0];
            Consumer c = ((Consumer) getActivity().getApplication());
            ArtistName artist = new ArtistName(artistMusicFile.getArtistName());
            Component b = c.getBroker(artist);
            String songName =artistMusicFile.getTrackName();

            //set Broker's ip and port
            String ip = b.getIp();
            int port = b.getPort();

            Socket s = null;
            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            try {
                //While we find a broker who is not responsible for the artistname
                Request.ReplyFromBroker reply=null;
                int statusCode = Request.StatusCodes.NOT_RESPONSIBLE;
                while(statusCode == Request.StatusCodes.NOT_RESPONSIBLE){
                    s = new Socket(ip, port);
                    //Creating the request to Broker for this artist
                    out = new ObjectOutputStream(s.getOutputStream());
                    requestPullToBroker(artist, songName, out);
                    //Waiting for the reply
                    in = new ObjectInputStream(s.getInputStream());
                    reply = (Request.ReplyFromBroker) in.readObject();
                    System.out.printf("[CONSUMER] Got reply from Broker(%s,%d) : %s%n", ip, port, reply);
                    statusCode = reply.statusCode;
                    ip = reply.responsibleBrokerIp;
                    port = reply.responsibleBrokerPort;
                }
                if(statusCode == Request.StatusCodes.NOT_FOUND){
                    System.out.println("Song or Artist does not exist");
                    return "";

                }
                //Song exists and the broker is responsible for the artist
                else if(statusCode == Request.StatusCodes.OK){
                    int progress = PROGRESS_MAX/(reply.numChunks*2);
                    //Save the information that this broker is responsible for the requested artist
                    c.register(new Component(s.getInetAddress().getHostAddress(),s.getPort()) , artist);
                    //download mp3 to the device
                     //download(reply.numChunks, in ,songName);
                    ArrayList<MusicFile> chunks = new ArrayList<>();
                    int size = 0;
                    //Start reading chunks
                    for (int i = 0; i < reply.numChunks; i++) {
                        //HandleCHunks
                        publishProgress(PROGRESS_CURRENT + progress);
                        MusicFile chunk = (MusicFile) in.readObject();
                        System.out.println("[CONSUMER] got chunk Number " + i);
                        size += chunk.getMusicFileExtract().length;
                        //Add chunk to the icomplete list
                        chunks.add(chunk);

                    }
                    String filename = songName +".mp3";
                    //save chunk
                    Log.e("aloha200",filename);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();//baos stream gia bytes
                    for(int k = 0 ; k < chunks.size() ; k++){
                        publishProgress(PROGRESS_CURRENT + progress);
                        baos.write(chunks.get(k).getMusicFileExtract());
                    }
                    byte[] concatenated_byte_array = baos.toByteArray();//metatrepei to stream se array
                    File path = getContext().getFilesDir();
                    Log.e("pathaki", path + filename);
                    Log.e("aloha",filename);
                    try (FileOutputStream fos = new FileOutputStream(path + filename)) {
                        fos.write(concatenated_byte_array);
                    }catch (Exception e){
                        return "";
                    }
                    return "ok";
                }
                //In this case the status code is MALFORMED_REQUEST
                else{
                    System.out.println("MALFORMED_REQUEST");
                    return "";
                }
            }
            catch(ClassNotFoundException e){
                //Protocol Error (Unexpected Object Caught) its a protocol error
                System.out.printf("[CONSUMER] Unexpected object on playData %s " , e.getMessage());
            }
            catch (IOException e){
                System.out.printf("[CONSUMER] Error on playData %s " , e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null) in.close();
                    if (out != null) out.close();
                    if (s != null) s.close();
                }
                catch(Exception e){
                    System.out.printf("[CONSUMER] Error while closing socket on playData %s " , e.getMessage());
                }

            }
            return "";
        }
        //Send a pull request to the broker at the end of the stream
        private void requestPullToBroker(ArtistName artist, String songName, ObjectOutputStream out) throws IOException {
            Request.RequestToBroker request = new Request.RequestToBroker();
            request.method = Request.Methods.PULL;
            request.pullArtistName = artist.getArtistName();
            request.songName = songName;
            out.writeObject(request);
        }

        @Override
        protected void onPreExecute() {
            notificationManager = NotificationManagerCompat.from(getContext());
            builder = new NotificationCompat.Builder(getContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_audiotrack_light)
                    .setContentTitle("Download Song")
                    .setContentText("Download in progress...")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            PROGRESS_MAX = 100;
            PROGRESS_CURRENT = 0;
            builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            notificationId = 1;
            // notificationId is a unique int for each notification that you must define
            notificationManager.notify(1, builder.build());

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if(s.equals("ok")){
                builder.setContentText("Download complete")
                        .setProgress(0, 0, false);
                notificationManager.notify(notificationId, builder.build());
            }else{
                builder.setContentText("Can't download")
                        .setProgress(0, 0, false);
                notificationManager.notify(notificationId, builder.build());
            }
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(values[0]>0) {
                PROGRESS_CURRENT = values[0];
                builder.setProgress(PROGRESS_MAX, PROGRESS_CURRENT, false);
            }
            notificationManager.notify(notificationId, builder.build());
        }
    }
    private void createNotificationChannel() {
        String channel_name = "Basic chanel";
        String channel_description = "Download";
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = channel_name;
            String description = channel_description;
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
