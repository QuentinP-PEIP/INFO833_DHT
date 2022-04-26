package DHT;

public class Data {
	
	//Chaque donnée est définie par un id
	private int id;
	
	public Data(int id) {
		this.setId(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
