
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.*;

public class Consumer extends Node implements Serializable {

	private ArrayList<Component> knownBrokers = new ArrayList<>();
	private Map<ArtistName, Component> artistToBroker = new HashMap<ArtistName, Component>();


	private ArrayList<MusicFile> chunks;

	public Consumer(){}

	public void register(Component c, ArtistName artist) {
		artistToBroker.put(artist,c);
		this.knownBrokers.add(c);
	}

	public void disconnect(Broker broker, ArtistName artist) { }

	private void requestToBroker(ArtistName artist, String songName, ObjectOutputStream out) throws IOException {
		Request.RequestToBroker request = new Request.RequestToBroker();
		request.method = Request.Methods.PULL;
		request.pullArtistName = artist.getArtistName();
		request.songName = songName;
		out.writeObject(request);
	}
	public void playData(ArtistName artist, String  songName , boolean download) throws Exception {
		//set Broker's ip and port
		String ip = null;
		int port = 0;
		//try to find the responsible broker
		Component c = artistToBroker.get(artist);
		if(c!=null){
			//this consumer have done this search before
			ip = c.getIp();
			port = c.getPort();
		}//take a random broker
		else{
			int index = new Random().nextInt(knownBrokers.size());
			ip = knownBrokers.get(index).getIp();
			port = knownBrokers.get(index).getPort();
		}
		Socket s = null;
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		try {
			//While we find a broker who is not responsible for the artistname
			Request.ReplyFromBroker reply=null;
			int statusCode = Request.StatusCodes.NOT_RESPONSIBLE;
			while(statusCode == Request.StatusCodes.NOT_RESPONSIBLE){
				s = new Socket(ip, port);
				//Creating the request to Broker for this artist
				out = new ObjectOutputStream(s.getOutputStream());
				requestToBroker(artist, songName, out);
				//Waiting for the reply
				in = new ObjectInputStream(s.getInputStream());
				reply = (Request.ReplyFromBroker) in.readObject();
				System.out.printf("[CONSUMER] Got reply from Broker(%s,%d) : %s", ip, port, reply);
				statusCode = reply.statusCode;
				ip = reply.responsibleBrokerIp;
				port = reply.responsibleBrokerPort;
			}
			if(statusCode == Request.StatusCodes.NOT_FOUND){
				System.out.println("Song or Artist does not exist");
				throw new Exception("Song or Artist does not exist");
			}
			//Song exists and the broker is responsible for the artist
			if(statusCode == Request.StatusCodes.OK){
				//Save the information that this broker is responsible for the requested artist
				register(new Component(ip,port) , artist);
				//download mp3 to the device
				if(download) {
					int size = 0;
					//Start reading chunks
					for (int i = 0; i < reply.numChunks; i++) {
						//HandleCHunks
						MusicFile chunk = (MusicFile) in.readObject();
						size += chunk.getMusicFileExtract().length;
						//Add chunk to the icomplete list
						chunks.add(chunk);
					}
					save(chunks, "TEMPTEMPTMEPTEMPTMEPMTEPMTEMTPETMETPEP");
				}
				//Play the music now
				else{
					int size = 0;
					MusicPlayer mp = new MusicPlayer(reply.numChunks);
					mp.play();
					for (int i = 0; i < reply.numChunks; i++) {
						//HandleCHunks
						MusicFile chunk = (MusicFile) in.readObject();
						size += chunk.getMusicFileExtract().length;
						//Add chunk to the icomplete list
						mp.addChunk(chunk);
					}

				}
			}
			//In this case the status code is MALFORMED_REQUEST
			else{
				System.out.println("MALFORMED_REQUEST");
				throw new Exception("MALFORMED_REQUEST");
			}
		}
		catch(ClassNotFoundException e){
			//Protocol Error (Unexpected Object Caught) its a protocol error
			System.out.printf("[CONSUMER] Unexpected object on playData %s " , e.getMessage());
		}
		catch (IOException e){
			System.out.printf("[CONSUMER] Error on playData %s " , e.getMessage());
		}
		finally {
			try {
				if (in != null) in.close();
				if (out != null) out.close();
				if (s != null) s.close();
			}
			catch(Exception e){
				System.out.printf("[CONSUMER] Error while closing socket on playData %s " , e.getMessage());
			}

		}
	}

	private void save(ArrayList<MusicFile> chunks , String filename) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();//baos stream gia bytes
		for(int k = 0 ; k < chunks.size() ; k++){
			baos.write(chunks.get(k).getMusicFileExtract());
		}
		byte[] concatenated_byte_array = baos.toByteArray();//metatrepei to stream se array
		try (FileOutputStream fos = new FileOutputStream(Paths.get("")+filename)) {
			fos.write(concatenated_byte_array);
		}
	}

	private void readBrokers(String fileName) {
		try {
			File myObj = new File(fileName);
			Scanner myReader = new Scanner(myObj);
			while (myReader.hasNextLine()) {
				String data = myReader.nextLine();
				String[] arrOfStr = data.split("\\s");
				String ip = arrOfStr[0];
				int port = Integer.parseInt(arrOfStr[1]);

				knownBrokers.add(new Component(ip,port));
			}
			//close reader
			myReader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
	}
	public static void main(String[] args){
		try {
			Consumer c = new Consumer();
			c.readBrokers(args[0]); //this shouldn't happen.. and how is the consumer going to know which broker to
									//send requests to?
			c.playData(new ArtistName("Kevin MacLeod"),"Painting Room" , false);
		}
		catch(Exception e){
			System.err.println("Usage : java Consumer <brokerFile>");
		}
	}
}