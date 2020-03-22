
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
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
		//System.out.println("HashValues : " + hashValues);
		//System.out.println("Md5 = " + md5);
		if( md5 > hashValues.get(hashValues.size() - 1)){
			return hashValues.get(0);
		}
		int index = Collections.binarySearch(hashValues, md5);
		System.out.println("Index = " + index);
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

	/*
	 * accept connection with Publicher: notify Publisher
	 */
	public void notifyPublisher(String[] args) {
		System.out.println("IN NOTIFY");
		System.out.println();
		String ip = args[1];
		int port = Integer.parseInt(args[2]);
		for(int i=3; i<args.length; i++){
			String artistName = args[i];
			if(isResponsible(artistName)){
				artistToPublisher.put(new ArtistName(artistName),new Component(ip,port));
			}
		}
	}

	public void  pull(ArtistName artist, String song, ObjectOutputStream  out) throws IOException {
		//check if th broker is responsible for this artist
		if(isResponsible(artist.getArtistName())){
			//find Publisher for this artist
			Component publiserWithThisArtist = artistToPublisher.get(artist);
			//open connection with Publisher and request the specific song
			if(publiserWithThisArtist != null) {
				requestSongFromPublisher(publiserWithThisArtist, artist, song, out);
			}else{
				//404 : something went wrong
				out.writeObject("404");
			}
		}else{
			//find responsible Broker and send
			int brokersHashValue = findResponsibleBroker(Utilities.getMd5(artist.getArtistName()).hashCode());
			//it can't be null, there is at least one Broker, ourself
			Component broker = hashValueToBroker.get(brokersHashValue);
			//send message to Consumer with the ip and the port with the responsible broker
			//consumer will ask this Broker for the song
			out.writeObject("402 " + broker.getIp() + " " + broker.getPort());
		}
	}

	public void requestSongFromPublisher(Component c, ArtistName artistName , String song, ObjectOutputStream  outToConsumer) {
		Socket s = null;
		ObjectInputStream in = null;
		ObjectOutputStream outToPublisher = null;
		try {
			s = new Socket(c.getIp(), c.getPort());

			//push artistName song
			String messageToPublisher = "push " + artistName.getArtistName() + " " + song;
			outToPublisher = new ObjectOutputStream(s.getOutputStream());
			outToPublisher.writeObject(messageToPublisher);

			//wait from Publisher to send to Broker songs data
			String reply = (String)in.readObject();

			 String[] arrOfStr = reply.split("\\s");
			 //if everithing is ok
			if(arrOfStr[0].equals("200")){
				int numOfChunks = Integer.parseInt(arrOfStr[1]);
				//whatever you receive from Publisher send it to Consumer
				outToConsumer.writeObject(reply);
				for(int i=0; i<numOfChunks; i++){
					 MusicFile chunk = (MusicFile)in.readObject();
					outToConsumer.writeObject(chunk);
				}
			}
			//404 : something went wrong
			else{
				outToConsumer.writeObject("404");
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally{
			try {
				if(in!=null) in.close();
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
				hashValueToBroker.put(hashValue,c);
			}
			//sort by hashValues
			Set<Integer> set = hashValueToBroker.keySet();
			synchronized (set){
				for ( int key :  set) {
					hashValues.add(key);
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
					//message from Publisher
					notifyPublisher(args);
				}
				//this  "else if" is useless, it's for debug purposes
				else if(args[0].toLowerCase().equals("status")){ 				//information querying about broker's state
					//Retuns the names of the artists for whom the broker is responsible
					String reply = "";
					for(ArtistName key : artistToPublisher.keySet()) {
						reply += key.getArtistName();
					}
					out.writeObject(reply);
				}
				//pull means we got a request from Consumer for an astist's song
				else if (args[0].toLowerCase().equals("pull")){
					ArtistName artistName = new ArtistName(args[1]);
					String song = args[2];
					pull(artistName, song, out);
				}

				//Response to Broker' request for an Artist
				/**
				 System.out.printf("[BROKER %s % d] STATUS ----------------- %n" , getIp() , getPort() );
				 System.out.println(artistToPublisher);
				 System.out.println(hashValueToBroker);
				 System.out.println(hashValueToBroker);
				 System.out.printf("[BROKER %s % d] ----------------- %n" , getIp() , getPort() );
				 **/

			}catch (ClassNotFoundException c) {
				System.out.println("Class not found");
				c.printStackTrace();
				return;
			} catch (IOException ioException) {
				ioException.printStackTrace();
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
}