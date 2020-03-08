import java.util.List;

public abstract class Node {

	List<Broker> brokers;

	public void init(int initialize) {
	}
	
	public void connect() {
	}

	public void disconnect() {
	}

	public void updateNode() {
	}
	
	//setters and getters
	public void setBrokers(List<Broker> brokers) {
		this.brokers = brokers;
	}
	public List<Broker> getBrokers() { return brokers; }

}