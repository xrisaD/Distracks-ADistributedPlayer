import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;

public class Consumer extends Node implements Serializable {

	private ArrayList<Component> knownBrokers = new ArrayList<>();
	private ArrayList<MusicFile> chunks = new ArrayList<>();

	public Consumer(){}

	public void register(Broker broker, ArtistName artist) { }

	public void disconnect(Broker broker, ArtistName artist) { }

	public void playData(ArtistName artist, String  songName) throws Exception {
		Socket s = null;
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		try {
			System.out.println("Start PlayData.. "+artist +" "+songName);

			String ip = knownBrokers.get(0).getIp();
			int port =  knownBrokers.get(0).getPort();

			//While we find a broker who is not responsible for the artistname
			Request.ReplyFromBroker reply=null;
			int statusCode = Request.StatusCodes.NOT_RESPONSIBLE;
			while(statusCode == Request.StatusCodes.NOT_RESPONSIBLE){
				s = new Socket(ip, port);
				//Creating the request object
				Request.RequestToBroker request = new Request.RequestToBroker();
				request.method = Request.Methods.PULL;
				request.pullArtistName = artist.getArtistName();
				request.songName = songName;
				//Writing the request object
				out = new ObjectOutputStream(s.getOutputStream());
				out.writeObject(request);

				//Waiting for the reply
				in = new ObjectInputStream(s.getInputStream());
				reply = (Request.ReplyFromBroker) in.readObject();
				System.out.printf("[CONSUMER] Got reply from Broker(%s,%d) : %s", ip, port, reply);
				statusCode = reply.statusCode;
				ip = reply.responsibleBrokerIp;
				port = reply.responsibleBrokerPort;
			}

			if(statusCode == Request.StatusCodes.NOT_FOUND){
				throw new Exception("Song or Artist does not exist");
			}
			//Song exists and the broker is responsible for the artist
			if(statusCode == Request.StatusCodes.OK){
				//Start reading chunks
				int size=0;
				for(int i = 0 ; i < reply.numChunks ; i++){
					//HandleCHunks

					MusicFile chunk = (MusicFile) in.readObject();
					size+=chunk.getMusicFileExtract().length;
					chunks.add(chunk);
				}
				save(chunks,size,reply.numChunks);
			}
			//In this case the status code is MALFORMED_REQUEST
			else{
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

	private void save(ArrayList<MusicFile> chunks,int size,int numChunks) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();//baos stream gia bytes
		for(int k=0;k<numChunks;k++){
			baos.write(chunks.get(k).getMusicFileExtract());
		}
		byte[] concatenated_byte_array = baos.toByteArray();//metatrepei to stream se array
		try (FileOutputStream fos = new FileOutputStream("C:\\Users\\Jero\\Desktop\\DistributedSystemsAssignment\\savetested.mp3")) {
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


	public void run() {

	}
	public static void main(String[] args){
		try {
			Consumer c = new Consumer();
			c.readBrokers(args[0]);
			System.out.println("Let's start PlayData.. ");
			c.playData(new ArtistName("GTXM"),"The Big Numbers Song");
		}
		catch(Exception e){
			System.err.println("Usage : java Consumer <brokerFile>");
		}
	}

}