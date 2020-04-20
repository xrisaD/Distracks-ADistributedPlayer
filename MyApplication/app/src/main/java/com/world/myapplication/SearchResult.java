package com.world.myapplication;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

//result: all artist's song
public class SearchResult extends Fragment {
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
        //get argmunets from search
        artist = getArguments().getString ("artist", "");
        //search for songs
        AsyncSearchResult runner = new AsyncSearchResult();
        runner.execute(artist);
    }

    private void setUI(final String artist, ArrayList<MusicFileMetaData> resultMetaData){
        LinearLayout myLayout = rootView.findViewById(R.id.search_layout);
        //color
        int colorBackground = Color.parseColor("#5F021F");
        int colorText = Color.parseColor("#ffffff");

        //create margin
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, 30);

        //download option Layout
        LinearLayout downloadLayout = new LinearLayout(getContext());
        downloadLayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView downloadText = new TextView(getContext());
        downloadText.setText("Download");
        downloadText.setTextSize(20);
        downloadText.setTextColor(colorText);
        downloadLayout.addView(downloadText);
        //switch for download or stream
        download = new Switch(getContext());
        downloadLayout.addView(download);
        downloadLayout.setLayoutParams(layoutParams);
        myLayout.addView(downloadLayout);

        ArrayList<Button> mySongs = new ArrayList<Button>();
        //set padding
        int padding = 30;
        for (int i = 0; i < resultMetaData.size(); i++) {
            // create a new textview
            // Create LinearLayout
            LinearLayout newLayout = new LinearLayout(getContext());
            newLayout.setOrientation(LinearLayout.VERTICAL);
            newLayout.setBackgroundColor(colorBackground);

            // Add title
            // Create Button
            final Button btn = new Button(getContext());
            btn.setBackgroundColor(colorBackground);
            btn.setText(resultMetaData.get(i).getTrackName());
            btn.setTextSize(12);
            btn.setTextColor(colorText);
            btn.setLayoutParams (new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 100));
            newLayout.addView(btn);
            mySongs.add(btn);

            //Add
            String info = "AlbumInfo: " + resultMetaData.get(i).getAlbumInfo() + "\n"+"Genre: " + resultMetaData.get(i).getGenre();
            TextView data = new TextView(getContext());
            data.setText(info);
            data.setTextSize(10);
            data.setTextColor(colorText);
            newLayout.addView(data);

            newLayout.setLayoutParams(layoutParams);

            newLayout.setPadding(padding,padding,padding,padding);
            // add the textview to the linearlayout
            myLayout.addView(newLayout);

        }
        //click buttons
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
                                AsyncDownload runner = new AsyncDownload();
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
                setUI(artist,s);
                Log.e("yeah", "yeah");
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

    //TODO: 2 Download Song + Notifiction oti katevike to tragoudi
    private class AsyncDownload extends AsyncTask<MusicFileMetaData, String, String> {

        @Override
        protected String doInBackground(MusicFileMetaData... artistAndSong) {
            return null;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }
    }

}
