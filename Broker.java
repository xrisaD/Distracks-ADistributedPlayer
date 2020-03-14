import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

public class Broker {

	private List<Consumer> registeredUsers;
	private List<Publisher> registeredPublishers;
	private String ip;
	private int port;
	//file contains ip and ports of all the brokers
	private String brokersFile;


	public void calculateKeys() { }

	public Publisher acceptConnection(Publisher publisher) {
		
	}

	public Consumer acceptConnection(Consumer consumer) { return null; }

	public void notifyPublisher(String notification) { }

	public void pull(ArtistName artist) { }
	
	
	//constructor
	public Broker(String ip, int port,String brokersFile){
		this.ip = ip;
		this.port = port;
		this.brokersFile = brokersFile;
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

	/**
	 * start a server for Consumers and Publisher
	 */
	public void startServer() {
		ServerSocket providerSocket = null;
		Socket connection = null;
		try {
			providerSocket = new ServerSocket(this.port, 10);
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
			new Broker(args[0],Integer.parseInt(args[1]),args[2]).startServer();
		}catch (Exception e) {
			System.out.print("Usage: java Publisher ip port");
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
				Object x = in.readObject();
				if (x instanceof Publisher) {
					//message from Broker
					Publisher p = (Publisher) x;
					p = acceptConnection(p);
					if(p!=null){
						System.out.println("Broker accept Connection with Publisher!");
					}

				}else if(x instanceof Consumer){
					//message from Consumer
				}


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