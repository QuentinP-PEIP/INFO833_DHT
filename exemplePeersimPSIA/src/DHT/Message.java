package DHT;

import peersim.edsim.*;

public class Message {

	//Afin d'associer chaque message avec les actions correspondantes, on crée donc différents types de messages
    public final static int DHT = 0;
    public final static int JOIN = 1;
    public final static int PLACE_NODE = 2;
    public final static int SET_NOEUD_SUIV = 3;
    public final static int SET_NOEUD_PREC = 4;
    public final static int LEAVE = 5;
    public final static int SEND = 6;
    public final static int DELIVER = 7;
    public final static int PUT = 8;
    public final static int PLACE_DATA = 9;
    public final static int GET = 10;
    public final static int ADD_NODE_TO_TABLE = 11;
    public final static int REMOVE_NODE_TO_TABLE = 12;
    public final static int ACCESS_TABLE = 13;
    
   
    private int type;  //Le type de message
    private String content; 
    private Integer uid;
    private int network_rank; //Rang du noeud dans le réseau
    private DHT noeud_a_set; //Permet de faire passer un noeud dans un message
    private Data donnee; //Permet de faire passer un noeud dans un message
    
    //Nous utilisons différents constructeurs de message afin de transmettre les informations dont nous avons besoin
    
    //Tous les messages sont composés d'un type et d'un contenu
    Message(int type, String content) {
	this.type = type;
	this.content = content;
    }
    
    //Ce constructeur permet de transporter un noeud
    Message(int type, String content, DHT noeud_a_set) {
        this.type = type;
        this.content = content;
        this.noeud_a_set= noeud_a_set;
        }
    
    //Ce constructeur permet de transporter un uid et le rang dans le réseau
    Message(int type, String content, Integer uid, int network_rank) {
    this.type = type;
    this.content = content;
    this.uid= uid;
    this.setNetwork_rank(network_rank);
    }
    
    //Ce constructeur permet de transporter un uid, le rang dans le réseau et un noeud
    Message(int type, String content, Integer uid, int network_rank, DHT noeud_a_set) {
        this.type = type;
        this.content = content;
        this.uid= uid;
        this.setNetwork_rank(network_rank);
        this.setNoeud_a_set(noeud_a_set);
        }
    
    //Ce constructeur permet de transporter une donnée
    Message(int type, String content, Data donnee) {
        this.type = type;
        this.content = content;
        this.setDonnee(donnee);
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

	public Data getDonnee() {
		return donnee;
	}

	public void setDonnee(Data donnee) {
		this.donnee = donnee;
	}
    
}