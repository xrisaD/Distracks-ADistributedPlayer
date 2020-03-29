import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Publisher extends Node implements Serializable {
	//ArtistName -> MusicFileMetaDatas of this artist
	private Map<ArtistName, ArrayList<MusicFileMetaData>> artistToMusicFileMetaData = Collections.synchronizedMap(new HashMap<>());
	private MP3Cutter cutter;
	private String ip;
	private int port;
	private String first;
	private String last;
	private String fileWithBrokers;
	private List<byte[]> currentsong;

	public void getBrokerList(String filename)  {
		Scanner myReader = null;
		try {
			myReader = new Scanner(new File(filename));
			//Notifying all brokers
			while (myReader.hasNextLine()) {
				//Parsing a broker
				String data = myReader.nextLine();
				String[] arrOfStr = data.split("\\s");
				String ip = arrOfStr[0];
				int port = Integer.parseInt(arrOfStr[1]);
				notifyBroker(ip , port);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void hashTopic(ArtistName artist) { }

	public void push(String artist, String song, ObjectOutputStream out) throws IOException, NoSuchAlgorithmException {
		ArrayList<MusicFileMetaData> songs = artistToMusicFileMetaData.get(new ArtistName(artist));

		if(songs!=null ){
			//&& songs.size()>0
			System.out.println("IN search for song");
			for (MusicFileMetaData s : songs) {
				if (s.getTrackName().toLowerCase().equals(song)) {
					String path = s.getPath();
					cutter = new MP3Cutter(new File(path));

					currentsong = cutter.splitFile();//returns arraylist with byte[]. So size of arraylist is number of chunks
					int numofchunks = currentsong.size();//arithmos chunks
					Request.ReplyFromPublisher reply = new Request.ReplyFromPublisher();
					reply.statusCode = Request.StatusCodes.OK;
					reply.numChunks = numofchunks;
					out.writeObject(reply);
					Utilities ut=new Utilities();
					for(byte[] b:currentsong){
						MusicFile finalMF= new MusicFile(s,b,ut.getMd5(b));//metadata + kathe chunk
						out.writeObject(finalMF);
					}
					return;
				}
			}
		}
		//Not found means the publisher was unable to find the artist or the songs
		notifyFailure(Request.StatusCodes.NOT_FOUND, out);
	}
	public void search(String artist, ObjectOutputStream out) throws IOException {
		ArrayList<MusicFileMetaData> songs = artistToMusicFileMetaData.get(new ArtistName(artist));
		if(songs!=null){
			Request.ReplyFromPublisher reply = new Request.ReplyFromPublisher();
			reply.statusCode = Request.StatusCodes.OK;
			reply.metaData = songs;
			out.writeObject(reply);
		}
		//Not found means the publisher was unable to find the artist or the song's metadata
		notifyFailure(Request.StatusCodes.NOT_FOUND, out);
	}
	public void notifyFailure(int statusCode, ObjectOutputStream out) throws IOException {
		Request.ReplyFromPublisher reply = new Request.ReplyFromPublisher();
		reply.statusCode = statusCode;
		out.writeObject(reply);
	}

	/**
	 * Server starts for Brokers
	 */
	public void startServer() {
		ServerSocket providerSocket = null;
		Socket connection = null;
		try {
			providerSocket = new ServerSocket(this.port, 10);
			System.out.println("Publisher listening on port " + getPort());
			while (true) {
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
			socket = new Socket(ip,port);
			System.out.printf("[PUBLISHER %d] Connected to broker on port %d , ip %s%n" ,getPort() , port , ip);
			out = new ObjectOutputStream(socket.getOutputStream());
			//Creating notify request to Broker
			//String message = String.format("notify %s %d" , getIp() , getPort());
			Request.RequestToBroker request = new Request.RequestToBroker();
			request.publisherIp = this.getIp();
			request.publisherPort = this.getPort();
			request.method = Request.Methods.NOTIFY;
			request.artistNames = new ArrayList<String>();

			for(ArtistName artist  : artistToMusicFileMetaData.keySet()){
				request.artistNames.add(artist.getArtistName());
			}
			System.out.printf("[PUBLISHER %d] Sending message \"%s\" to broker on port %d , ip %s%n" ,getPort(), request , port , ip);
			out.writeObject(request);
			//read Ok?
		}
		catch(Exception e){
			System.out.printf("[PUBLISHER %d] Failure on notifybroker Broker(ip = %s port = %d  %n)" , getPort() , ip , port);
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		finally{
			try {
				if(out!=null) out.close();
				if(socket!=null) socket.close();
			}
			catch(Exception e){
				System.out.println("Error while closing streams");
				throw new RuntimeException(e);
			}
		}

	}
	public static void main(String[] args){
		try{
			// arg[0]: ip arg[1]:port
			// arg[2]: first letter of responsible artistname arg[3]: last letter of responsible artistname
			// arg[4]: file with Broker's information
			Publisher p = new Publisher(args[0],Integer.parseInt(args[1]) , args[2], args[3],args[4]);
			Path currentRelativePath = Paths.get("");
			p.getBrokerList(args[4]);
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
				in = new ObjectInputStream(socket.getInputStream());
				//Take Broker's request
				//Broker's request is a ArtistName and a song
				Request.RequestToPublisher req= (Request.RequestToPublisher) in.readObject();
				System.out.printf("[PUBLISHER %s , %d] GOT REQUEST " + req.toString() , getIp() , getPort());
				out = new ObjectOutputStream(socket.getOutputStream());
				if(req.method == Request.Methods.PUSH) {
					if(req.artistName==null || req.songName==null){
						notifyFailure(Request.StatusCodes.MALFORMED_REQUEST, out);
					}else {
						push(req.artistName, req.songName.toLowerCase(), out);
					}
				}else if(req.method == Request.Methods.SEARCH){
					if(req.artistName==null){
						notifyFailure(Request.StatusCodes.MALFORMED_REQUEST, out);
					}else{
						search(req.artistName, out);
					}
				}else{
					notifyFailure(Request.StatusCodes.MALFORMED_REQUEST, out);
				}
			}catch (ClassNotFoundException c) {
				c.printStackTrace();
				return;
			} catch (IOException ioException) {
				ioException.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
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

	//constructor
	public Publisher(String ip, int port , String first, String last, String fileWithBrokers){
		this.ip = ip;
		this.port = port;
		this.first = first;
		this.last = last;
		this.fileWithBrokers = fileWithBrokers;
		//Read file with artists and music file info
		//initialize HashTable
		List<MusicFileMetaData> allMetaData= MP3Cutter.getSongsMetaData(first, last);
		//create artistToMusicFileMetaData Hashtable by parsing allMetaData

		for (MusicFileMetaData song : allMetaData) {

			if(artistToMusicFileMetaData.get(new ArtistName(song.getArtistName()))==null){
				//initialize artist
				artistToMusicFileMetaData.put(new ArtistName(song.getArtistName()), new ArrayList<MusicFileMetaData>());
			}
			//add song to the particular artist
			artistToMusicFileMetaData.get(new ArtistName(song.getArtistName())).add(song);
		}
	}

	//Getters setters
	public Map<ArtistName, ArrayList<MusicFileMetaData>> getArtistToMusicFileMetaData(){
		return artistToMusicFileMetaData;
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
}