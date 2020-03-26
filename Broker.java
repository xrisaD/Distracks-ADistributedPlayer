
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Broker {

	//artistName->Publisher's ip and port
	private Map<ArtistName, Component> artistToPublisher = Collections.synchronizedMap(new HashMap<ArtistName, Component>());
	//mapping hashValues->Broker's ip and port
	private Map<Integer, Component> hashValueToBroker = Collections.synchronizedMap(new HashMap<Integer, Component>());
	//hashValues for all Brokers
	private List<Integer> hashValues = Collections.synchronizedList(new ArrayList<Integer>());

	private String ip;
	private int port;
	private int hashValue;

	public void calculateKeys() {
	}

	/**
	 *
	 * @param md5 hash value
	 * @return hash value to repsonsible broker
	 */
	public int findResponsibleBroker(int md5){
		if( md5 > hashValues.get(hashValues.size() - 1)){
			return hashValues.get(0);
		}
		int index = Collections.binarySearch(hashValues, md5);
		if(index>0){
			return hashValues.get(index);
		}else{
			return hashValues.get(-index - 1);
		}
	}
	/*
	 * check if this Broker is responsible for this artistName
	 */
	public boolean isResponsible(String artistName){
		int md5 = Utilities.getMd5(artistName).hashCode();
		int res = findResponsibleBroker(md5);
		//this broker is responsible for this artistName
		return ( res == this.hashValue);
	}


	public void acceptConnection(Publisher p) {

	}

	public Consumer acceptConnection(Consumer consumer) {
		return consumer;
	}

	public void replyWithMalformedRequest(ObjectOutputStream out) throws IOException{
		Request.ReplyFromBroker reply = new Request.ReplyFromBroker();
		reply.statusCode = Request.StatusCodes.MALFORMED_REQUEST;
		out.writeObject(reply);
	}
	public void replyWithOK(ObjectOutputStream out) throws IOException{
		Request.ReplyFromBroker reply = new Request.ReplyFromBroker();
		reply.statusCode = Request.StatusCodes.OK;
		out.writeObject(reply);
	}
	public void replyWithNotFound(ObjectOutputStream out) throws IOException{
		Request.ReplyFromBroker reply = new Request.ReplyFromBroker();
		reply.statusCode = Request.StatusCodes.NOT_FOUND;
		out.writeObject(reply);
	}
	/*
	 * accept connection with Publicher: notify Publisher
	 */
	public void notifyPublisher(String ip, int port,  ArrayList<String> artists) {
		System.out.println();
		for(String artistName:artists){
			System.out.println(artistName);
			if(isResponsible(artistName)){
				artistToPublisher.put(new ArtistName(artistName),new Component(ip,port));
			}
		}
	}

	public void  pull(ArtistName artist, String song, ObjectOutputStream  out) throws IOException {
		//check if th broker is responsible for this artist
		if(isResponsible(artist.getArtistName())){
			//find Publisher for this artist
			Component publisherWithThisArtist = artistToPublisher.get(artist);
			//open connection with Publisher and request the specific song
			if(publisherWithThisArtist != null || artistToPublisher.size()==0) {
				requestSongFromPublisher(publisherWithThisArtist, artist, song, out);
			}else{
				//404 : something went wrong
				replyWithNotFound(out);
			}
		}else{
			//find responsible Broker and send
			int brokersHashValue = findResponsibleBroker(Utilities.getMd5(artist.getArtistName()).hashCode());
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
	}

	public void requestSongFromPublisher(Component c, ArtistName artistName, String song, ObjectOutputStream  outToConsumer) {
		Socket s = null;
		ObjectInputStream inFromPublisher = null;
		ObjectOutputStream outToPublisher = null;
		try {
			s = new Socket(c.getIp(), c.getPort());

			//Creating the request to the Publisher
			Request.RequestToPublisher request = new Request.RequestToPublisher();
			request.method = Request.Methods.PUSH;
			request.artistName = artistName.getArtistName();
			request.songName = song;

			outToPublisher = new ObjectOutputStream(s.getOutputStream());
			outToPublisher.writeObject(request);

			inFromPublisher = new ObjectInputStream(s.getInputStream());
			//wait from Publisher to send to Broker songs data
			Request.ReplyFromPublisher reply = (Request.ReplyFromPublisher) inFromPublisher.readObject();

			 //if everithing is ok
			if(reply.statusCode == Request.StatusCodes.OK){
				int numOfChunks = reply.numChunks;
				//whatever you receive from Publisher send it to Consumer
				//Reply to the consumer
				Request.ReplyFromBroker replyToConsumer = new Request.ReplyFromBroker();
				replyToConsumer.statusCode = Request.StatusCodes.OK;
				replyToConsumer.numChunks = numOfChunks;
				outToConsumer.writeObject(replyToConsumer);

				for(int i=0; i<numOfChunks; i++){
					 MusicFile chunk = (MusicFile)inFromPublisher.readObject();
					 outToConsumer.writeObject(chunk);
				}
			}
			//404 : something went wrong
			else {
				replyWithNotFound(outToConsumer);
			}
		} catch (IOException | ClassNotFoundException e) {
			System.out.println("[BROKER] Error while requesting song from publisher " + e.getMessage());
		} finally{
			try {
				if(inFromPublisher!=null) inFromPublisher.close();
				if(outToPublisher!=null) outToPublisher.close();
				if(s!=null) s.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
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
	private void saveBrokersData(String fileName) {
		try {
			File myObj = new File(fileName);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {

				String data = myReader.nextLine();

				String[] arrOfStr = data.split("\\s");
				String ip = arrOfStr[0];
				int port = Integer.parseInt(arrOfStr[1]);
				int hashValue = Integer.parseInt(arrOfStr[2]);

				Component c = new Component(ip,port);
				this.hashValueToBroker.put(hashValue,c);
			}
			//sort by hashValues
			Set<Integer> set = hashValueToBroker.keySet();
			synchronized (set){
				for ( int key :  set) {
					this.hashValues.add(key);
				}
				hashValues.sort(Comparator.naturalOrder());
			}
			//close reader
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}

	public static void main(String[] args){
		try{
			//arg[0]:ip
			//arg[1]:port
			//arg[2]:hashValue
			Broker b = new Broker(args[0],Integer.parseInt(args[1]),Integer.parseInt(args[2]));
			b.saveBrokersData(args[3]);
			b.startServer();
		}catch (Exception e) {
			System.out.println("Usage: java Broker ip port hashValue brokersFile");
			System.out.println(e.getMessage());
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
			ObjectInputStream in = null;
			ObjectOutputStream out = null;
			try{
				out = new ObjectOutputStream(socket.getOutputStream());
				in = new ObjectInputStream(socket.getInputStream());

				Request.RequestToBroker request = (Request.RequestToBroker) in.readObject();
				System.out.printf("[Broker (%s,%d)] GOT A MESSSAGE <%s> %n" , getIp() , getPort() , request.toString());

				//Publisher notifies Broker about the artistNames he is responsible for
				if(request.method == Request.Methods.NOTIFY){
					System.out.println("[ Broker ] notify true"+getPort());
					//message from
					//Check that data is correct or send MALFORMED_REQUEST
					if(request.publisherIp == null ||
							request.publisherPort <= 0 ||
							request.artistNames == null) {
						System.out.println("request true"+getPort());
						replyWithMalformedRequest(out);

					}
					notifyPublisher(request.publisherIp, request.publisherPort, request.artistNames);
					//replyWithOK(out); ?
				}
				//this  "else if" is for debug purposes
				else if(request.method == Request.Methods.STATUS){ 				//information querying about broker's state
					//Returns the names of the artists for whom the broker is responsible
					Request.ReplyFromBroker reply = new Request.ReplyFromBroker();
					ArrayList<String> artists = new ArrayList<>();
					for (ArtistName a : artistToPublisher.keySet()){
						artists.add(a.getArtistName());
					}
					reply.statusCode = Request.StatusCodes.OK;
					reply.artists = artists;
					out.writeObject(reply);
				}
				//pull means we got a request from Consumer for an artist's song
				else if (request.method == Request.Methods.PULL){
					System.out.println("PULL to Broker with port: "+ getPort());
					ArtistName artistName = new ArtistName(request.pullArtistName);
					String song = request.songName;

					if(request.pullArtistName ==null || song==null){
						replyWithMalformedRequest(out);
					}else {
						pull(artistName, song, out);
					}
				}
				//Unknown method so we return a reply informing of a malformed request
				else{
					replyWithMalformedRequest(out);
				}

				//Response to Broker' request for an Artist
				/**
				 System.out.printf("[BROKER %s % d] STATUS ----------------- %n" , getIp() , getPort() );
				 System.out.println(artistToPublisher);
				 System.out.println(hashValueToBroker);
				 System.out.println(hashValueToBroker);
				 System.out.printf("[BROKER %s % d] ----------------- %n" , getIp() , getPort() );
				 **/

			} catch (IOException | ClassNotFoundException e) {
				System.out.printf("[BROKER %s % d] terminating a connection due to an exception : %s %n"
						, getIp() , getPort() , e.getMessage() );
			}
			finally {
				try {
					if (in != null) in.close();
					if (out != null) out.close();
					if(socket != null) socket.close();
				}
				catch(Exception e){
					throw new RuntimeException(e);
				}
			}

		}
	}

	//constructor
	public Broker(String ip, int port, int hashValue){
		this.ip = ip;
		this.port = port;
		this.hashValue = hashValue;
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

	public int getHashValue() {
		return hashValue;
	}
}
