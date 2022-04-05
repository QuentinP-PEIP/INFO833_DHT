package DHT;

import peersim.edsim.*;

public class Message {

    public final static int DHT = 0;
    public final static int JOIN = 1;
    public final static int PLACE = 2;
    public final static int SET_NOEUD_SUIV = 3;
    public final static int SET_NOEUD_PREC = 4;
    
    

    private int type;
    private String content;
    private Integer uid;
    private int network_rank;
    private DHT noeud_a_set;

    Message(int type, String content) {
	this.type = type;
	this.content = content;
    }
    
    Message(int type, String content, Integer uid, int network_rank) {
    this.type = type;
    this.content = content;
    this.uid= uid;
    this.setNetwork_rank(network_rank);
    }
    
    Message(int type, String content, Integer uid, int network_rank, DHT noeud_a_set) {
        this.type = type;
        this.content = content;
        this.uid= uid;
        this.setNetwork_rank(network_rank);
        this.setNoeud_a_set(noeud_a_set);
        }

    public String getContent() {
	return this.content;
    }

    public int getType() {
	return this.type;
    }

	public Integer getUid() {
		return uid;
	}

	public void setUid(Integer uid) {
		this.uid = uid;
	}

	public int getNetwork_rank() {
		return network_rank;
	}

	public void setNetwork_rank(int network_rank) {
		this.network_rank = network_rank;
	}

	public DHT getNoeud_a_set() {
		return noeud_a_set;
	}

	public void setNoeud_a_set(DHT noeud_a_set) {
		this.noeud_a_set = noeud_a_set;
	}
    
}