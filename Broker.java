import java.util.List;

public class Broker {

	public List<Consumer> registeredUsers;

	public List<Publisher> registeredPublishers;

	public Broker(){}

	public void calculateKeys() { }

	public Publisher acceptConnection(Publisher publisher) { }

	public Consumer acceptConnection(Consumer consumer) { }

	public void notifyPublisher(String notification) { }

	public void pull(ArtistName artist) { }

}