import java.io.*;
import java.net.Socket;
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
			String ip = knownBrokers.get(0).getIp();
			int port =  knownBrokers.get(0).getPort();
			int statusCode = Request.StatusCodes.NOT_RESPONSIBLE;
			Request.ReplyFromBroker reply=null;
			//While we find a broker who is not responsible for the artistname
			System.out.println("in");
			while(statusCode == Request.StatusCodes.NOT_RESPONSIBLE){
				System.out.println("in");
				s = new Socket(ip, port);
				out = new ObjectOutputStream(s.getOutputStream());
				//Creating the request object
				Request.RequestToBroker request = new Request.RequestToBroker();
				request.method = Request.Methods.PULL;
				request.pullArtistName = artist.getArtistName();
				request.songName = songName;
				//Writing the request object
				out.writeObject(request);
				//Waiting for the reply
				in = new ObjectInputStream(s.getInputStream());
				reply = (Request.ReplyFromBroker) in.readObject();
				System.out.printf("[CONSUMER] Got reply from Broker(%s,%d) : %s", ip, port, reply);
				statusCode = reply.statusCode;
			}
			System.out.println("in");
			if(statusCode == Request.StatusCodes.NOT_FOUND){
				throw new Exception("Song or Artist does not exist");
			}
			//Song exists and the broker is responsible for the artist
			if(statusCode == Request.StatusCodes.OK){
				//Start reading chunks
				for(int i = 0 ; i < reply.numChunks ; i++){
					//HandleCHunks
					MusicFile chunk = (MusicFile) in.readObject();
					chunks.add(chunk);
				}
				System.out.println(chunks.size());
			}
			//In this case the status code is MALFORMED_REQUEST
			else{
				throw new Exception("MALFORMED_REQUEST");
			}
			//TODO: for Jero and kon_kons: merge in one file save as mp3 the chunks arrayList data
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
		}
		catch(Exception e){
			System.err.println("Usage : java Consumer <brokerFile>");
		}
	}

}