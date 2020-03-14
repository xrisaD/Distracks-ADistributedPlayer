import java.io.Serializable;
import java.util.List;

public class Consumer extends Node implements Serializable {

	public Consumer(){}

	public void register(Broker broker,ArtistName artist) { }

	public void disconnect(Broker broker,ArtistName artist) { }

	public void playData(ArtistName artist,Value value) { }

	public void run() {

	}
	public static void main(String[] args){
		new Consumer().run();
	}

}