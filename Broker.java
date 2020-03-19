import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Broker {


	//artistName->Publisher's ip and port
	private Map<ArtistName, Component> artistToPublisher = Collections.synchronizedMap(new HashMap<ArtistName, Component>());
	//mapping hashValues->Broker's ip and port
	private Map<Integer, Component> hashValueToBroker = Collections.synchronizedMap(new HashMap<Integer, Component>());
	//hashValues for all Brokers
	private List<Integer> hashValues = Collections.synchronizedList(new ArrayList<Integer>());



	private List<Component> registeredUsers = Collections.synchronizedList(new ArrayList<Component>());
	private List<ArtistName> artists = Collections.synchronizedList(new ArrayList<ArtistName>());

	private List<Broker> brokers = Collections.synchronizedList(new ArrayList<Broker>());

	private String ip;
	private int port;
	private int hashValue;

	public void calculateKeys() {


	}
	public int findResponsibleBroker(int md5){
		System.out.println("HashValues : " + hashValues);
		System.out.println("Md5 = " + md5);
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

	public void acceptConnection(String ip, int port) {

	}

	public Consumer acceptConnection(Consumer consumer) {
		return consumer;
	}

	public void notifyPublisher(String notification) {
		System.out.println(notification);
	}

	public void pull(ArtistName artist) { }


	//constructor
	public Broker(String ip, int port, int hashValue){
		this.ip = ip;
		this.port = port;
		this.hashValue = hashValue;
	}

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
				in = new ObjectInputStream(socket.getInputStream());

				out = new ObjectOutputStream(socket.getOutputStream());

				Object test=in.readObject();

				if(test instanceof MusicFile){
					System.out.println("in music");
					MusicFile message = (MusicFile) test;
					byte[] temp=message.getMusicFileExtract();
					System.out.println(message);
					try (FileOutputStream stream = new FileOutputStream("C:\\Users\\tinoa\\Desktop\\Back.mp3")) {
						stream.write(message.getMusicFileExtract());
					}
				}else if(test instanceof String){
					String message = (String)test;
					String[] args = message.split("\\s");

					if(args[0].toLowerCase().equals("notify")){
						//message from Publisher
						String ip = args[1];
						int port = Integer.parseInt(args[2]);
						for(int i=3; i<args.length; i++){
							String artistName = args[i];
							if(isResponsible(artistName)){
								artistToPublisher.put(new ArtistName(artistName),new Component(ip,port));
							}
						}

					}
					//information querying about broker's state
					//Retuns the names of the artists for whom the broker is responsible
					if(args[0].toLowerCase().equals("status")){
						out = new ObjectOutputStream(socket.getOutputStream());
						String reply = "";
						for(ArtistName key : artistToPublisher.keySet()) {
							reply += key.getArtistName();
						}
						out.writeObject(reply);
					}
				}


				// TODO: else if args[0].toLowerCase().equals("pull"))


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



}