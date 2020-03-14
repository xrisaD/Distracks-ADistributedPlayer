import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Broker {

	private List<Consumer> registeredUsers = Collections.synchronizedList(new ArrayList<Consumer>());
	private List<Publisher> registeredPublishers = Collections.synchronizedList(new ArrayList<Publisher>());
	private String ip;
	private int port;
	//file contains ip and ports of all the brokers
	private String brokersFile;


	public void calculateKeys() { }

	public Publisher acceptConnection(Publisher publisher) {
		registeredPublishers.add(publisher);
		return publisher;
	}

	public Consumer acceptConnection(Consumer consumer) {
		registeredUsers.add(consumer);
		return consumer;
	}

	public void notifyPublisher(String notification) {

	}

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
			Broker b = new Broker(args[0],Integer.parseInt(args[1]),args[2]);
			b.startServer();
		}catch (Exception e) {
			System.out.println("Usage: java Broker ip port");
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
				//out = new ObjectOutputStream(socket.getOutputStream());

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
					System.out.println("Broker got connection on port from consumer");

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