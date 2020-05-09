package com.world.myapplication;


import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Consumer {

    private ArrayList<Component> knownBrokers = new ArrayList<>();
    private Map<ArtistName, Component> artistToBroker = new HashMap<ArtistName, Component>();
    private File path;
    private ArrayList<MusicFile> chunks = new ArrayList<>();
    public void setPath(File path) {
        this.path = path;
    }

    public String getPath() {
        return path + "/";
    }

    //private MusicPlayer mp;

    public Consumer(){}
    // Register the broker with ip c.getIp , port c.getPort as responsible for thie artistname
    public void register(Component c, ArtistName artist) {
        artistToBroker.put(artist, c);
        this.knownBrokers.add(c);
    }

    //Send a pull request to the broker at the end of the stream
    private void requestPullToBroker(ArtistName artist, String songName, ObjectOutputStream out) throws IOException {
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
    //Find the reposible broker for this artist , or, If he is not yet known return a random known Broker
    private Component getBroker(ArtistName artist){
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
    // Method that downloads the song if download == true or streams the song if download == false
    public synchronized void playData(ArtistName artist, String  songName , boolean download ) throws Exception {
        chunks = null;
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
                throw new Exception("Song or Artist does not exist");
            }
            //Song exists and the broker is responsible for the artist
            else if(statusCode == Request.StatusCodes.OK){
                //Save the information that this broker is responsible for the requested artist
                register(new Component(s.getInetAddress().getHostAddress(),s.getPort()) , artist);
                //download mp3 to the device

                if(download) {
                    download(reply.numChunks, in , out,songName);
                }
                //Play the music now
                else{
                    stream(reply.numChunks, in , songName);

                }
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
    }
    // Stream the song that is coming from the input stream
   /* private void stream(int numChunks, ObjectInputStream in) throws IOException, ClassNotFoundException, NoSuchAlgorithmException {
        int size = 0;
        mp = new MusicPlayer(numChunks);
        mp.play();
        Utilities util=new Utilities();
        for (int i = 0; i < numChunks; i++) {
            //HandleCHunks
            MusicFile chunk = (MusicFile) in.readObject();
            size += chunk.getMusicFileExtract().length;
            BigInteger brokermd5=util.getMd5(chunk.getMusicFileExtract());
            System.out.println(chunk.biggie.compareTo(brokermd5)+"   COMPARE UP TO CHUNK CONSUMER"+i);
            //Add chunk to the icomplete list
            mp.addChunk(chunk);
        }
    }*/
    //Download song and save to filename

    private void download(int numChunks, ObjectInputStream in, ObjectOutputStream out, String filename) throws IOException, ClassNotFoundException {
        //Initializing donwload incomplete list
        chunks = new ArrayList<>(numChunks);
        int size = 0;
        //Start reading chunks

        for (int i = 0; i < numChunks; i++) {
            //ask for the next chunk
            Request.RequestToBroker requestToBroker= new Request.RequestToBroker();
            requestToBroker.method = Request.Methods.NEXT_CHUNK;
            out.writeObject(requestToBroker);
            out.flush();

            //HandleCHunks
            Object object = in.readObject();
            if(object instanceof MusicFile) {

                MusicFile chunk = (MusicFile) object;

                System.out.println("[CONSUMER] got chunk Number " + i);
                System.out.println();

                size += chunk.getMusicFileExtract().length;
                //Add chunk to the icomplete list
                chunks.add(chunk);
            }

        }

        Request.RequestToBroker requestToBroker= new Request.RequestToBroker();
        requestToBroker.method = Request.Methods.THE_END;
        out.writeObject(requestToBroker);
        out.flush();

        if(chunks.size() == numChunks) {
            save(chunks, filename + ".mp3");
        }
    }

    private void stream(int numChunks, ObjectInputStream in, String filename) throws IOException, ClassNotFoundException {
        //Initializing donwload incomplete list
        chunks = new ArrayList<>(numChunks);
        int size = 0;
        //Start reading chunks
        System.out.println("Created output file " + getPath() + filename);
        try(FileOutputStream fos = new FileOutputStream(getPath() + filename)){
            for (int i = 0; i < numChunks; i++) {
                //HandleCHunks
                Object object = in.readObject();
                if (object instanceof MusicFile) {
                    MusicFile chunk = (MusicFile) object;

                    //System.out.println("[CONSUMER] got chunk Number " + i);
                    System.out.println();
                    size += chunk.getMusicFileExtract().length;
                    //Add chunk to the icomplete list
                    chunks.add(chunk);
                    fos.write(chunk.getMusicFileExtract());
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                    LocalDateTime now = LocalDateTime.now();
                    //System.out.println(dtf.format(now));
                } else {
                    System.out.println("No no no");
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    public boolean hasReadFirstChunk(){
        return chunks!=null && chunks.size() > 0;
    }
    // Save a list of music files as entire mp3 with the given filename
    private void save(ArrayList<MusicFile> chunks , String filename) throws IOException {
        System.out.println("Saving a song to " + getPath()+filename);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();//baos stream gia bytes
        for(MusicFile chunk : chunks){
            baos.write(chunk.getMusicFileExtract());
        }
        byte[] concatenated_byte_array = baos.toByteArray();//metatrepei to stream se array
        try (FileOutputStream fos = new FileOutputStream(getPath()+filename)) {
            fos.write(concatenated_byte_array);
        }
    }

    // Reads information about the first broker in the file name
    public void readBroker(String fileName) {
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] arrOfStr = data.split("\\s");
                String ip = arrOfStr[0];
                int port = Integer.parseInt(arrOfStr[1]);

                knownBrokers.add(new Component(ip,port));
            }
            //close reader
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
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
            int statusCode = Request.StatusCodes.NOT_RESPONSIBLE;
            while(statusCode == Request.StatusCodes.NOT_RESPONSIBLE){
                System.out.println("NOT RESPONSIBLE");
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
                register(new Component(s.getInetAddress().getHostAddress(),s.getPort()) , artist);
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
    public void addBroker(Component c){
        knownBrokers.add(c);
    }
//    public static void main(String[] args){
//        try {
//            Consumer c = new Consumer();
//            c.readBroker(args[0]); //this shouldn't happen.. and how is the consumer going to know which broker to
//            //send requests to?
//            c.playData(new ArtistName("Komiku"),"A good bass for gambling" , false);
//
//        }
//        catch(Exception e){
//            System.err.println("Usage : java Consumer <brokerFile>");
//            e.printStackTrace();
//        }
//    }
}