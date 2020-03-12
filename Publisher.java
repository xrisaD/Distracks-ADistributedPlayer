import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;


public class Publisher extends Node {

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

	public void startServer() {
		ServerSocket providerSocket = null;
		Socket connection = null;
		try {
			providerSocket = new ServerSocket(this.port, 10);
			while (true) {
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
	public static void main(String[] args){
		try{
			new Publisher(args[0],Integer.parseInt(args[1]),args[2]).startServer();
		}catch (Exception e) {
			System.out.print("Usage: java Publisher ip port");
		}
	}
	public class ServerConnectionHandler extends Thread{
		ServerSocket providerSocket;
		public ServerConnectionHandler(ServerSocket providerSocket){
			this.providerSocket = providerSocket;
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
