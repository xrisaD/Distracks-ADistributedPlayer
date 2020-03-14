import javax.management.RuntimeErrorException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;


public class Publisher extends Node implements Serializable {

	private String ip;
	private int port;

	private Hashtable<ArtistName, ArrayList<Value>> artistToValue;

	public void getBrokerList() { }

	public Broker hashTopic(ArtistName artist) { return null; }

	public void push(ArtistName artist,Value value) { }

	public void notifyFailure(Broker broker) { }
	
	//constructor
	public Publisher(String ip, int port,String fileName){
		this.ip = ip;
		this.port = port;
		//Read file with artists and music file info
		//initialize HashTable


	}
	//Getters setters


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
	 * Server starts for Brokers
	 */
	public void startServer() {
		ServerSocket providerSocket = null;
		Socket connection = null;
		try {
			providerSocket = new ServerSocket(this.port, 10);
			while (true) {
				System.out.println("Publisher listening on port " + getPort());
				connection = providerSocket.accept();
				//We start a thread

				//this thread will do the communication


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
	 * 	Connects to the broker on @param ip and @param port and sends the publisher object through the socket
	 */
	public void notifyBroker(String ip , int port){
		Socket socket = null;
		ObjectOutputStream out = null;
		try{
			//Connecting to the broker
			System.out.printf("Connecting to broker on port %d , ip %s%n" , port , ip);
			socket = new Socket(ip,port);
			System.out.printf("Connected to broker on port %d , ip %s%n" , port , ip);
			out = new ObjectOutputStream(socket.getOutputStream());
			out.writeObject(this);
		}
		catch(Exception e){
			e.printStackTrace();
			System.out.println(e.getMessage());
			System.out.printf("Failure on notify broker on publisher port = %d ip = %s %n" , getPort() , getIp());
		}
		finally{
			try {
				//socket.close();
				out.close();
			}
			catch(Exception e){
				System.out.println("Error while closing streams");
				throw new RuntimeException(e);
			}
		}

	}
	public static void main(String[] args){
		try{
			Publisher p = new Publisher(args[0],Integer.parseInt(args[1]),args[2]);
			//p.startServer();
			p.notifyBroker("127.0.0.1" , 3560);

		}catch (Exception e) {
			System.out.println("Usage: java Publisher ip port");
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public class PublisherHandler extends Thread{
		Socket socket;
		public PublisherHandler(Socket socket){
			this.socket = socket;
		}
		@Override
		public void run(){ //Protocol
			ObjectInputStream in = null;
			ObjectOutputStream out = null;
			try{
				//Take Broker's request
				//Broker's request is a ArtistName

				ArtistName aName = null;
				aName= (ArtistName) in.readObject();

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
