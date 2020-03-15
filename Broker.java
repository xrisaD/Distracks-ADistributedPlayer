import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Broker {

		private List<Consumer> registeredUsers = Collections.synchronizedList(new ArrayList<Consumer>());
		private List<Publisher> registeredPublishers = Collections.synchronizedList(new ArrayList<Publisher>());

		//artistName->Publisher's ip and port
		private ConcurrentHashMap<ArtistName, Component> artistToPublisher = new ConcurrentHashMap<ArtistName, Component>();




		//String's format= brokers_ip brokers_port
		private ConcurrentHashMap<ArtistName, String> artistToBroker = new ConcurrentHashMap<ArtistName, String>();

		private List<ArtistName> artists = Collections.synchronizedList(new ArrayList<ArtistName>());

		private List<Broker> brokers = Collections.synchronizedList(new ArrayList<Broker>());

		private String ip;
		private int port;
		private int hashValue;

		/**
		 *
		 *
		 */
		public void calculateKeys() {


		}
		public boolean isResponsible(String artistName){
			return false;
		}

		public void acceptConnection(Publisher publisher) {
			registeredPublishers.add(publisher);
			Set<ArtistName> keys = publisher.getArtistToValue().keySet();
			for(ArtistName key: keys) {
				//if this artist is mine

				artistToPublisher.put(key, publisher);
			}
		}

		public Consumer acceptConnection(Consumer consumer) {
			registeredUsers.add(consumer);
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
		//setters and getters
		public List<Consumer> getRegisteredUsers() {
			return registeredUsers;
		}

		public void setRegisteredUsers(List<Consumer> registeredUsers) {
			this.registeredUsers = registeredUsers;
		}

		public List<Publisher> getRegisteredPublishers() {
			return registeredPublishers;
		}

		public void setRegisteredPublishers(List<Publisher> registeredPublishers) {
			this.registeredPublishers = registeredPublishers;
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

		public static void main(String[] args){
			try{
				Broker b = new Broker(args[0],Integer.parseInt(args[1]),Integer.parseInt(args[2]));
				b.saveBrokersData(args[3]);
				b.startServer();
			}catch (Exception e) {
				System.out.println("Usage: java Broker ip port");
				System.out.println(e.getMessage());
				e.printStackTrace();
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


				}
				myReader.close();
			} catch (FileNotFoundException e) {
				System.out.println("An error occurred.");
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
					//out = new ObjectOutputStream(socket.getOutputStream());

					String message = (String)in.readObject();
					String[] args = message.split("\\s");

					if(args[0].toLowerCase().equals("notify")){
						//message from Publisher
						String ip = args[1];
						int port = Integer.parseInt(args[2]);
						for(int i=3; i<args.length; i++){
							String artistName = args[i];
							if(isResponsible(artistName)){
								artistToPublisher.add(new ArtistName(artistName),new Component(ip,port));
							}
						}
					}

					/*Object x = in.readObject();
					if (x instanceof Publisher) {
						//message from Broker
						Publisher p = (Publisher) x;
						//now the Broker knows for which artistName this artistName is responsible
						acceptConnection(p);
					}else if(x instanceof Consumer){
						//message from Consumer
						System.out.println("Broker got connection on port from consumer");

					}*/


					//Response to Broker' request for an Artist



				}catch (ClassNotFoundException c) {
					System.out.println("Class not found");
					c.printStackTrace();
					return;
				} catch (IOException ioException) {
					ioException.printStackTrace();
				}

			}
		}

}