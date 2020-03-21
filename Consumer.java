import java.io.*;
import java.net.Socket;
import java.util.*;

public class Consumer extends Node implements Serializable {

	private ArrayList<Component> knownBrokers = new ArrayList<>();
	public Consumer(){}

	public void register(Broker broker,ArtistName artist) { }

	public void disconnect(Broker broker,ArtistName artist) { }

	public void playData(ArtistName artist,Value value) {
		Socket s = null;
		ObjectInputStream in = null;
		ObjectOutputStream out = null;
		try {
			String ip = knownBrokers.get(0).getIp();
			int port =  knownBrokers.get(0).getPort();
			s = new Socket( ip, port);
			out = new ObjectOutputStream(s.getOutputStream());
			//Creating the request object
			String request = String.format("pull %s %s" , artist , value);
			//Writing the request object
			out.writeObject(request);
			//Waiting for the reply
			in = new ObjectInputStream(s.getInputStream());
			String reply = (String) in.readObject();
			System.out.printf("[CONSUMER] Got reply from Broker(%s,%d) : %s" , ip , port , reply);
			//TODO handle case where broker is not responsible

			//TODO handle case where the song does not exits

			//TODO handle case where all is well

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
				if (s != null) s.close();
				if (in != null) in.close();
				if (out != null) out.close();
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