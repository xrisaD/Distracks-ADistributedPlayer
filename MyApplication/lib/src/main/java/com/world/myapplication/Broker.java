package com.world.myapplication;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.security.NoSuchAlgorithmException;

public class Broker {

    // Max number of songs that the broker maintaings in his cache
    private int CACHE_SIZE = 10;

    //artistName->Publisher's ip and port
    private Map<ArtistName, Component> artistToPublisher
            = Collections.synchronizedMap(new HashMap<ArtistName, Component>());
    //mapping hashValues->Broker's ip and port
    private Map<BigInteger, Component> hashValueToBroker
            = Collections.synchronizedMap(new HashMap<BigInteger, Component>());


    /**
     * A cache that stores mappings from music File meta data to incomplete lists of musicFiles. The key is metaData
     * because we would in another  case need a pair of String artistName and String songname which is troublesome.
     * storing Incomplete lists is necessary because another thread might request the same song while it's being streamed
     * to another consume. This allows us to start sending all available chunks to te new consumer and block the connection
     * if chunks of a song are still being transported to the Broker
     */
    private SynchronizedLRUCache<MusicFileMetaData , IncompleteList<MusicFile>> musicFileCache =
            new SynchronizedLRUCache<>(CACHE_SIZE);

    //hashValues for all Brokers
    private List<BigInteger> hashValues = Collections.synchronizedList(new ArrayList<BigInteger>());

    private String ip;
    private int port;
    private BigInteger hashValue;

    /**
     * returns the responsible broker's hash value
     */
    public BigInteger findResponsibleBroker(BigInteger md5){
        if( md5.compareTo(hashValues.get(hashValues.size() - 1))>0){
            return hashValues.get(0);
        }
        int index = Collections.binarySearch(hashValues, md5);
        if(index>0){
            return hashValues.get(index);
        }else{
            return hashValues.get(-index - 1);
        }
    }
    /**
     * check if this Broker is responsible for this artistName
     */
    public boolean isResponsible(String artistName){
        BigInteger md5 = Utilities.getMd5(artistName.trim().toLowerCase());
        BigInteger res = findResponsibleBroker(md5);
        //this broker is responsible for this artistName
        return (res.compareTo(this.hashValue) == 0);
    }

    // BROKER'S COMMUNICATION
    public void replyWithMalformedRequest(ObjectOutputStream out) throws IOException{
        Request.ReplyFromBroker reply = new Request.ReplyFromBroker();
        reply.statusCode = Request.StatusCodes.MALFORMED_REQUEST;
        out.writeObject(reply);
    }
    public void replyWithOK(ObjectOutputStream out, int numOfChunks) throws IOException{
        Request.ReplyFromBroker reply = new Request.ReplyFromBroker();
        reply.statusCode = Request.StatusCodes.OK;
        reply.numChunks = numOfChunks;
        out.writeObject(reply);
    }
    public void replyWithOKmetaData(ObjectOutputStream out, ArrayList<MusicFileMetaData> metaData) throws IOException{
        Request.ReplyFromBroker reply = new Request.ReplyFromBroker();
        reply.statusCode = Request.StatusCodes.OK;
        reply.metaData = metaData;
        out.writeObject(reply);
        System.out.printf("BROKER(%s , %d) Sent metadata %s \n" , getIp() , getPort() , metaData);
    }
    public void replyWithOKartists(ObjectOutputStream out, ArrayList<String> artists) throws IOException{
        Request.ReplyFromBroker reply = new Request.ReplyFromBroker();
        reply.statusCode = Request.StatusCodes.OK;
        reply.artists = artists;
        out.writeObject(reply);
    }
    public void replyWithNotFound(ObjectOutputStream out) throws IOException{
        Request.ReplyFromBroker reply = new Request.ReplyFromBroker();
        reply.statusCode = Request.StatusCodes.NOT_FOUND;
        out.writeObject(reply);
    }
    /**
     *  Choose the artistNames this broker is responsible for and remember which publisher owns them
     */
    public void notifyPublisher(String ip, int port,  ArrayList<String> artists) {
        for(String artistName:artists){
            if(isResponsible(artistName)){
                artistToPublisher.put(new ArtistName(artistName),new Component(ip,port));
            }
        }
    }

    /**
     * Called after a pull request from a Consumer
     * Return true if everything ok
     * @return
     */
    public boolean pull(ArtistName artist, String song, ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {
        //find Publisher for this artist
        Component publisherWithThisArtist = artistToPublisher.get(artist);
        //open connection with Publisher and request the specific song
        if(publisherWithThisArtist != null && artistToPublisher.size() !=0 ) {
            //If song exists is not in the cache
            MusicFileMetaData tmp = new MusicFileMetaData(song , artist.getArtistName() , null , null , null, 0, null);

            IncompleteList<MusicFile> musicFileChunks = musicFileCache.get(tmp);
            if(musicFileChunks == null) {
                System.out.printf("[BROKER %s % d] Song %s not in cache%n"
                        , getIp() , getPort() ,song );
                 return requestSongFromPublisher(publisherWithThisArtist, artist, song, out, in);
            }
            //Song is in cache
            else{
                System.out.printf("[BROKER %s % d] Song %s in cache%n"
                        , getIp() , getPort() ,song );
                return sendSongToConsumer(musicFileChunks, out);
            }
        }else{
            //404 : something went wrong
            replyWithNotFound(out);
        }
        return false;
    }

    /**
     * Called after a search request from a Consuemr
     */
    public void search(ArtistName artist, ObjectOutputStream  out) throws IOException {
        //find Publisher for this artist
        Component publisherWithThisArtist = artistToPublisher.get(artist);
        //open connection with Publisher and request the specific song
        if(publisherWithThisArtist != null && artistToPublisher.size() != 0 ) {
            requestMetaDataFromPublisher(publisherWithThisArtist, artist, out);
        }else{
            //404 : something went wrong
            replyWithNotFound(out);
        }
    }
    public void sendResponsibleBroker(ArtistName artist, ObjectOutputStream  out) throws IOException {
        //find responsible Broker and send
        BigInteger brokersHashValue = findResponsibleBroker(Utilities.getMd5(artist.getArtistName().trim().toLowerCase()));
        //it can't be null, there is at least one Broker, ourself
        Component broker = hashValueToBroker.get(brokersHashValue);
        //send message to Consumer with the ip and the port with the responsible broker
        //consumer will ask this Broker for the song
        Request.ReplyFromBroker reply = new Request.ReplyFromBroker();
        reply.statusCode = Request.StatusCodes.NOT_RESPONSIBLE;//300
        reply.responsibleBrokerIp = broker.getIp();
        reply.responsibleBrokerPort = broker.getPort();
        out.writeObject(reply);
    }
    public boolean sendSongToConsumer(IncompleteList<MusicFile> chunks , ObjectOutputStream  outToConsumer){
        try{
            //Replying to the consumer and notifying about the number of chunks
            replyWithOK(outToConsumer, chunks.size());
            System.out.println("Sending song with size "+chunks.size());
            //Writing every chunk to the consumer
            int i = 0;
            for(MusicFile chunk : chunks){
                System.out.println("Sending song chunk "+i++ + "with hashval " + Arrays.hashCode(chunk.getMusicFileExtract()));
                outToConsumer.writeObject(chunk);
            }
        }
        catch (IOException e){
            System.err.println("Failed while writing chunks to consumer ");
            return false;
        }

        return true;
    }
    // Request song's data from the appropriate publisher. Each chunk we get is transmitted to the consumer via the
    // outToConsumer outputStream
    // retur true if everything was ok
    public boolean requestSongFromPublisher(Component c, ArtistName artistName, String song, ObjectOutputStream outToConsumer, ObjectInputStream in) throws IOException, ClassNotFoundException {
        Socket s = null;
        ObjectInputStream inFromPublisher = null;
        ObjectOutputStream outToPublisher = null;
        int i = 0;
        int numOfChunks = 0;
        MusicFileMetaData musicFileReference = null;
        Utilities ut = null;
        try {
            s = new Socket(c.getIp(), c.getPort());

            //Creating the request to the Publisher
            Request.RequestToPublisher request = new Request.RequestToPublisher();
            request.method = Request.Methods.PUSH;
            request.artistName = artistName.getArtistName();
            request.songName = song;

            //send request to Publisher
            outToPublisher = new ObjectOutputStream(s.getOutputStream());
            System.out.println("CREATING OUTPUT STREAM " + s.getInetAddress().toString());
            outToPublisher.writeObject(request);

            inFromPublisher = new ObjectInputStream(s.getInputStream());
            Request.ReplyFromPublisher reply = (Request.ReplyFromPublisher) inFromPublisher.readObject();
            //if everything is ok
            if(reply.statusCode == Request.StatusCodes.OK){
                //Adding the musicFile to the cache
                //Music file meta data object only for use with the cache
                musicFileReference = new MusicFileMetaData(song , artistName.getArtistName(), null , null , null, 0, null);
                musicFileCache.put(musicFileReference, new IncompleteList<>(reply.numChunks));

                numOfChunks = reply.numChunks;
                //whatever you receive from Publisher send it to Consumer
                //Reply to the consumer
                replyWithOK(outToConsumer,  numOfChunks);
                //Transmitting the chunks
                ut = new Utilities();
                while(i<numOfChunks){
                    MusicFile chunk = (MusicFile) inFromPublisher.readObject();
                    //Adding chunk to the cache
                    musicFileCache.get(musicFileReference).add(chunk);
                    BigInteger brokermd5 = ut.getMd5(chunk.getMusicFileExtract());
                    System.out.println("Sending song chunk " + i + "with hashval " + Arrays.hashCode(chunk.getMusicFileExtract()));

                    outToConsumer.writeObject(chunk);
                    outToConsumer.flush();
                    i++;
                }
            }
            //404 : something went wrong
            else {
                replyWithNotFound(outToConsumer);
            }
        } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e ) {
            System.out.println("[BROKER] Error while requesting song from publisher " + e.getMessage());

            //consumer stop unexpected
            //we will continue cache all the elements from Publisher form future usage
            while(i<numOfChunks){
                MusicFile chunk = (MusicFile)inFromPublisher.readObject();
                //Adding chunk to the cache
                musicFileCache.get(musicFileReference).add(chunk);
                BigInteger brokermd5=ut.getMd5(chunk.getMusicFileExtract());
                System.out.println("Sending song chunk "+i + "with hashval " + Arrays.hashCode(chunk.getMusicFileExtract()));
                i++;
            }
            return false;
        } finally{
            try {
                if(inFromPublisher!=null) inFromPublisher.close();
                if(outToPublisher!=null) outToPublisher.close();
                if(s!=null) s.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return true;
    }
    public void requestMetaDataFromPublisher(Component c, ArtistName artistName, ObjectOutputStream  outToConsumer){
        Socket s = null;
        ObjectInputStream inFromPublisher = null;
        ObjectOutputStream outToPublisher = null;
        try {
            s = new Socket(c.getIp(), c.getPort());
            //Creating the request to the Publisher
            Request.RequestToPublisher request = new Request.RequestToPublisher();
            request.method = Request.Methods.SEARCH;
            request.artistName = artistName.getArtistName();

            //send request to Publisher
            outToPublisher = new ObjectOutputStream(s.getOutputStream());
            System.out.println("CREATING OUTPUT STREAM " + s.getInetAddress().toString());
            outToPublisher.writeObject(request);

            inFromPublisher = new ObjectInputStream(s.getInputStream());
            Request.ReplyFromPublisher reply = (Request.ReplyFromPublisher) inFromPublisher.readObject();
            //if everything is ok
            if(reply.statusCode == Request.StatusCodes.OK){
                //Reply to the consumer
                replyWithOKmetaData(outToConsumer, reply.metaData);
            }
            //404 : something went wrong
            else {
                replyWithNotFound(outToConsumer);
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    /**
     * start a server for Consumers and Publisher
     */
    public void startServer() {

        ServerSocket providerSocket = null;
        Socket connection = null;
        try {
            providerSocket = new ServerSocket(this.port, 10);
            System.out.println("Broker listening on port " + getPort());
            while (true) {
                //accept a connection
                connection = providerSocket.accept();
                //We start a thread
                //this thread will do the communication
                BrokerHandler bh = new BrokerHandler(connection);
                bh.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     *
     * @param fileName ip port hashValue
     */
    private ArrayList<Component> saveBrokersData(String fileName) {
        ArrayList<Component> brokers = new ArrayList<>();
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                String[] arrOfStr = data.split("\\s");
                String ip = arrOfStr[0];
                int port = Integer.parseInt(arrOfStr[1]);
                brokers.add(new Component(ip,port));
            }
            //close reader
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return brokers;
    }
    public void calculateKeys(ArrayList<Component> brokers) {
        for(Component b: brokers){
            this.hashValueToBroker.put(Utilities.getMd5(b.getIp()+b.getPort()),b);
        }
        //sort by hashValues
        Set<BigInteger> set = hashValueToBroker.keySet();
        synchronized (set){
            for ( BigInteger key :  set) {
                this.hashValues.add(key);
            }
            hashValues.sort(Comparator.naturalOrder());
        }
    }
    public static void main(String[] args){
        try{
            //arg[0]:ip
            //arg[1]:port
            //arg[2]:brokers.txt
            Broker b = new Broker(args[0],Integer.parseInt(args[1]));
            ArrayList<Component> brokers = b.saveBrokersData(args[2]);
            b.calculateKeys(brokers);
            b.startServer();
        }catch (Exception e) {
            System.out.println("Usage: java Broker ip port brokersFile");
            e.printStackTrace();
        }
    }

    public class BrokerHandler extends Thread{
        Socket socket;
        public BrokerHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run(){ //Protocol
            boolean everyThingOk = false;

            ObjectInputStream in = null;
            ObjectOutputStream out = null;
            try{
                out = new ObjectOutputStream(socket.getOutputStream());
                System.out.println("CREATING OUTPUT STREAM " + socket.getInetAddress().toString());
                in = new ObjectInputStream(socket.getInputStream());

                Request.RequestToBroker request = (Request.RequestToBroker) in.readObject();
                System.out.printf("[BROKER (%s,%d)] GOT A MESSSAGE <%s> %n" , getIp() , getPort() , request.toString());

                //Publisher notifies Broker about the artistNames he is responsible for
                if(request.method == Request.Methods.NOTIFY){
                    //message from
                    //Check that data is correct or send MALFORMED_REQUEST
                    if(request.publisherIp == null ||
                            request.publisherPort <= 0 ||
                            request.artistNames == null) {
                        replyWithMalformedRequest(out);
                        everyThingOk = false;
                    }
                    notifyPublisher(request.publisherIp, request.publisherPort, request.artistNames);
                    //the end
                    Request.RequestToPublisher requestToPublisher = new Request.RequestToPublisher();
                    requestToPublisher.method = Request.Methods.THE_END;
                    out.writeObject(requestToPublisher);
                    out.flush();
                    System.out.println("I  am responsible for artists : " + artistToPublisher.entrySet());
                }
                //pull means we got a request from Consumer for an artist's song
                else if (request.method == Request.Methods.PULL){
                    ArtistName artist = new ArtistName(request.pullArtistName);
                    String song = request.songName;

                    if(request.pullArtistName ==null || song==null){
                        replyWithMalformedRequest(out);
                        everyThingOk = false;
                    }else {
                        //check if th broker is responsible for this artist
                        if(isResponsible(artist.getArtistName())) {
                            everyThingOk = pull(artist, song, out, in);
                        }else{
                            sendResponsibleBroker(artist, out);
                            everyThingOk = true;
                        }
                    }
                }
                else if(request.method == Request.Methods.SEARCH){
                    ArtistName artist = new ArtistName(request.pullArtistName);
                    if(request.pullArtistName ==null){
                        replyWithMalformedRequest(out);
                        everyThingOk = false;
                    }else {
                        if (isResponsible(artist.getArtistName())) {
                            search(artist, out);
                            everyThingOk = true;
                        } else {
                            sendResponsibleBroker(artist, out);
                            everyThingOk = false;
                        }
                    }
                }
                //this  "else if" is for debug purposes
                else if(request.method == Request.Methods.STATUS){ 				//information querying about broker's state
                    //Returns the names of the artists for whom the broker is responsible
                    ArrayList<String> artists = new ArrayList<>();
                    for (ArtistName a : artistToPublisher.keySet()){
                        artists.add(a.getArtistName());
                    }
                    replyWithOKartists(out, artists);
                }
                //Unknown method so we return a reply informing of a malformed request
                else{
                    replyWithMalformedRequest(out);
                    //we want to close socket without ok from consumer
                    everyThingOk = false;
                }


            } catch (IOException | ClassNotFoundException e) {
                System.out.printf("[BROKER %s % d] terminating a connection due to an exception : %s %n"
                        , getIp() , getPort() , e.getMessage() );
            }
            finally {

                try {
                    if(!everyThingOk || ((Request.RequestToBroker) in.readObject()).method == Request.Methods.THE_END ) {
                        if (in != null) in.close();
                        if (out != null) out.close();
                        if (socket != null) socket.close();
                    }
                }
                catch(Exception e){
                    throw new RuntimeException(e);
                }
            }
        }
    }

    //constructor
    public Broker(String ip, int port){
        this.ip = ip;
        this.port = port;
        this.hashValue = Utilities.getMd5(this.ip+this.port);
    }

    //getter and setters
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public BigInteger getHashValue() {
        return hashValue;
    }
}
