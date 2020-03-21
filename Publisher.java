import javax.management.RuntimeErrorException;
import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.*;
import java.nio.file.*;
import com.mpatric.mp3agic.*;

public class Publisher extends Node implements Serializable {

	private String ip;
	private int port;

	private Hashtable<ArtistName, ArrayList<Value>> artistToValue = new Hashtable<>();

	public void getBrokerList() { }

	public Broker hashTopic(ArtistName artist) { return null; }

	public void push(ArtistName artist,Value value) { }

	public void notifyFailure(Broker broker) { }

	//constructor
	public Publisher(String ip, int port){
		this.ip = ip;
		this.port = port;
		//Read file with artists and music file info
		//initialize HashTable
	}
	//Adds artists with no songs
	public Publisher(String ip, int port,ArrayList<String> artists){
		this(ip , port);
		for(String artist : artists) {
			artistToValue.put(new ArtistName(artist) , new ArrayList<>());
		}

	}
	//Getters setters
	public Hashtable<ArtistName, ArrayList<Value>> getArtistToValue(){
		return artistToValue;
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
				PublisherHandler ph = new PublisherHandler(connection);
				ph.start();

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
		System.out.printf("Publisher(%s,%d) notifying Broker(%s,%d)\n" , getIp(),getPort() , ip , port);
		Socket socket = null;
		ObjectOutputStream out = null;
		try{
			//Connecting to the broker
			System.out.printf("[PUBLISHER %d] Connecting to broker on port %d , ip %s%n" , getPort() , port , ip);
			socket = new Socket(ip,port);
			System.out.printf("[PUBLISHER %d] Connected to broker on port %d , ip %s%n" ,getPort() , port , ip);
			out = new ObjectOutputStream(socket.getOutputStream());
			//Creating notify message
			String message = String.format("notify %s %d" , getIp() , getPort());
			for(ArtistName name : artistToValue.keySet()){
				message += " " + name.getArtistName();
			}
			System.out.printf("[PUBLISHER %d] Sending message \"%s\" to broker on port %d , ip %s%n" ,getPort(), message , port , ip);
			out.writeObject(message);
		}
		catch(Exception e){
			System.out.printf("[PUBLISHER %d] Failure on notifybroker Broker(ip = %s port = %d  %n)" , getPort() , ip , port);
			e.printStackTrace();
			System.out.println(e.getMessage());
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

	public void sendChunkToBroker(String ip , int port){
		System.out.printf("Publisher(%s,%d) sending song to Broker(%s,%d)\n" , getIp(),getPort() , ip , port);
		Socket socket = null;
		ObjectOutputStream out = null;
		try{
			//Connecting to the broker
			System.out.printf("[PUBLISHER %d] Connecting to broker on port %d , ip %s%n" , getPort() , port , ip);
			socket = new Socket(ip,port);
			System.out.printf("[PUBLISHER %d] Connected to broker on port %d , ip %s%n" ,getPort() , port , ip);
			System.out.printf("DEN EBAINE EDW");
			out = new ObjectOutputStream(socket.getOutputStream());
			//Creating notify message
			int sizeOfFiles = 1024 * 512;// 1MB

			File mp3= new File("C:\\Users\\Jero\\Desktop\\dataset1\\Horror\\Horroriffic");
			byte[] buffer = read(mp3);
			MusicFile mp3File = new MusicFile("a","b","c","d",buffer);
			out.writeObject(mp3File);
		}
		catch(Exception e){
			System.out.printf("[PUBLISHER %d] Failure on notifybroker Broker(ip = %s port = %d  %n)" , getPort() , ip , port);
			e.printStackTrace();
			System.out.println(e.getMessage());
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

	public byte[] read(File file) throws IOException {

		byte[] buffer = new byte[(int) file.length()];
		InputStream ios = null;
		try {
			ios = new FileInputStream(file);
			if (ios.read(buffer) == -1) {
				throw new IOException(
						"EOF reached while trying to read the whole file");
			}
		} finally {
			try {
				if (ios != null)
					ios.close();
			} catch (IOException e) {
			}
		}
		return buffer;
	}

	public static void main(String[] args){
		try{
			//Parsing the list of artist names from command line
			ArrayList<String> artists = new ArrayList<>();
			for(int i = 3 ; i < args.length ; i++){
				artists.add(args[i]);
			}

			Publisher p = new Publisher(args[0],Integer.parseInt(args[1]) , artists);
			File myObj = new File(args[2]);
			/**
			Scanner myReader = new Scanner(myObj);
			//Notifying all brokers

			while (myReader.hasNextLine()) {
				//Parsing a broker
				String data = myReader.nextLine();
				String[] arrOfStr = data.split("\\s");
				String ip = arrOfStr[0];
				int port = Integer.parseInt(arrOfStr[1]);
				int hashValue = Integer.parseInt(arrOfStr[2]);
				p.notifyBroker(ip , port);
			}
			**/
			p.startServer();

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

				in = new ObjectInputStream(socket.getInputStream());
                out = new ObjectOutputStream(socket.getOutputStream());
                String aName= (String) in.readObject();
                String[] args = aName.split("\\s");

				//if(args[0].toLowerCase().equals("push")){
					System.out.println("BHKE STO PUSH TOU PUBLISHER");
                    String artist = args[1];
                    String song = args[2];
                    MP3Cutter Chunker= new MP3Cutter(new File("C:\\Users\\Jero\\Desktop\\dataset1\\Horror\\Horroriffic"));
                    Path currentRelativePath = Paths.get("");
					String result = Chunker.walk(currentRelativePath.toAbsolutePath().toString()+"\\songs", song);//stp result exoume to filename pou epistrefei h walk
                    if(!result.equals("error")){//an h walk den epistrepsei error
                    	File splitting=new File(result);
                    	int chunks=Chunker.splitFile(splitting);
                    	String title=splitting.getName();
                    	if(chunks!=0){
                    		int partCounter=0;
                    		for(int flag=1;flag<=chunks;flag++){
                    			int indexOfMp3 = title.indexOf(".mp3");
								String newName = title.substring(0,indexOfMp3);
								String filePartName = String.format("%s%03d.mp3", newName, partCounter++);
								File mp3= new File(currentRelativePath.toAbsolutePath().toString()+"\\songs"+"\\"+filePartName);
								byte[] buffer = read(mp3);
								MusicFile mp3File = new MusicFile("a","b","c","d",buffer);
								out.writeObject(mp3File);
                    		}
                    	}
					}
                /**}else if(args[0].toLowerCase().equals("checkArtist")){
					//message from Publisher
					String ip = args[1];
					int port = Integer.parseInt(args[2]);
					String artist = args[4];

					sendChunkToBroker(ip,port);
				}
				**/
                        
                    

                    
                    //tha psaxnei gia to tragoudi, tha vlepei an o artist name einai o idios, id3  na kanei cross check, an to vrei na kanei split se chunks
                    //while write object to kathe chunk se outstrea out.writeobject
                    //message from Publisher
           
                
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