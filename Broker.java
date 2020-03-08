import java.util.List;

public class Broker extends Node{

	private List<Consumer> registeredUsers;

	private List<Publisher> registeredPublishers;
	
	public void calculateKeys() { }

	public Publisher acceptConnection(Publisher publisher) { return null;}

	public Consumer acceptConnection(Consumer consumer) { return null; }

	public void notifyPublisher(String notification) { }

	public void pull(ArtistName artist) { }
	
	
	//constructor
	public Broker(){}
	//setters and getters
	public List<Consumer> getRegisteredUsers() {
		return registeredUsers;
	}

	public void setRegisteredUsers(List<Consumer> registeredUsers) {
		this.registeredUsers = registeredUsers;
	}

	public List<Publisher> getRegisteredPublishers() {
		return registeredPublishers;
	}

	public void setRegisteredPublishers(List<Publisher> registeredPublishers) {
		this.registeredPublishers = registeredPublishers;
	}
	

}