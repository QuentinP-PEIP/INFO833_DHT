package DHT;

import peersim.edsim.*;
import peersim.core.*;

import java.util.ArrayList;

import peersim.config.*;

public class DHT implements EDProtocol {
	
	//identifiant de la couche transport
    private int transportPid;

    //objet couche transport
    private HWTransport transport;

    //identifiant de la couche courante (la couche applicative)
    private int mypid;

    //le numero de noeud dans le network
    private int nodeId;
    
    //numéro du noeud du noeud
    private int nodeUid;
    
    

	//numéro du noeud avec un n° inférieur
    private DHT noeudPrec;
    
    //numéro du noeud avec un n° supérieur
    private DHT noeudSuiv;

    //prefixe de la couche (nom de la variable de protocole du fichier de config)
    private String prefix;
    
    //Booleen qui est true si le noeud à envoyé un ping au noeud 0 depuis un certain temps
    private boolean pingEnvoye = false;
    
    //Liste de données
    private ArrayList<Data> listeDonnees = new ArrayList<Data>();

    public DHT(String prefix) {
	this.prefix = prefix;
	//initialisation des identifiants a partir du fichier de configuration
	this.transportPid = Configuration.getPid(prefix + ".transport");
	this.mypid = Configuration.getPid(prefix + ".myself");
	this.transport = null;
    }

    //methode appelee lorsqu'un message est recu par le protocole HelloWorld du noeud
    public void processEvent( Node node, int pid, Object event ) {
	this.receive((Message)event);
    }
    
    //methode necessaire pour la creation du reseau (qui se fait par clonage d'un prototype)
    public Object clone() {

	DHT dolly = new DHT(this.prefix);

	return dolly;
    }

    //liaison entre un objet de la couche applicative et un 
    //objet de la couche transport situes sur le meme noeud
    public void setTransportLayer(int nodeId, int nodeUid) {
    	this.nodeId = nodeId;
    	this.nodeUid = nodeUid;
    	//System.out.println((Network.get(this.nodeId).getProtocol(this.transportPid)));
    	//System.out.println(Network.get(this.nodeId).getProtocol(this.transportPid).getClass().getSimpleName());
    	this.transport = (HWTransport) Network.get(this.nodeId).getProtocol(this.transportPid);
    }

    //envoi d'un message (l'envoi se fait via la couche transport)
    public void send(Message msg, Node dest) {
    	this.transport.send(getMyNode(), dest, msg, this.mypid);
    }

    //affichage a la reception
    private void receive(Message msg) {
    	Network.get(this.nodeId);
    	
    	
    	
    	if (msg.getType() == Message.DHT) {
    		
    		System.out.println(CommonState.getTime() + " |Message.DHT| Node n°" + this.nodeUid + " " + msg.getContent());

			//System.out.println("|Message.DHT| NOEUD PREC" + this.getNoeudPrec() + " " + "NOEUD SUIV" + this.getNoeudSuiv());
			//System.out.println(CommonState.getTime() + " NOEUD SUIV : " + this.noeudSuiv.getNodeUid());
			
			
			if (this.getNoeudSuiv().getNodeId() == 0) {
	    		System.out.println("END");
	    	}
			
			else {
				Node dest = Network.get(this.noeudSuiv.getNodeId());
	    		this.send(new Message(0, "Envoyé par : " + this.nodeUid), dest);
			}
		
    	}
    	
    	if (msg.getType() == Message.JOIN) {
    		
    		//JOIN sert à envoyer un message place au noeud 0
    		System.out.println(CommonState.getTime() + " |Message.JOIN| Demande de JOIN du Node n°" + this.nodeUid);
    		this.send(new Message(Message.PLACE_NODE, "Envoyé par : " +this.nodeUid, this.nodeUid, this.nodeId, this), Network.get(0));
    	}
    	
    	if (msg.getType() == Message.PLACE_NODE) {
    		
    		System.out.println(CommonState.getTime() + " |Message.PLACE| Node n°" + this.nodeUid + " " + msg.getContent()+ " NOEUD A SET : " + msg.getNoeud_a_set());
    		
    		//On part du noeud 0, et on compare l'uid du noeud à placer avec le noeud suivant le noeud 0, si < uid.suiv on place, sinon si this.id == 0, on place (plus haut uid), sinon, on test avec le noeud suivant
    		System.out.println(CommonState.getTime() + " MSG UID : " + msg.getUid() + " NOEUD SUIV UID : " + this.noeudSuiv.getNodeUid());
    		if (msg.getUid() < this.noeudSuiv.getNodeUid()) {
    			System.out.println(CommonState.getTime() + " |C'EST MOINS| MSG UID : " + msg.getUid() + " NOEUD SUIV UID : " + this.noeudSuiv.getNodeUid());
    			DHT nouveau_noeud = (DHT) Network.get(msg.getNetwork_rank()).getProtocol(0);
    			//System.out.println("TEST 1");
    			nouveau_noeud.setNoeudSuiv(this.getNoeudSuiv());
    			nouveau_noeud.setNoeudPrec(this);
    			//System.out.println("TEST 2 :" + msg.getNetwork_rank() + " UID NOEUD A SET" + msg.getNoeud_a_set());
    			nouveau_noeud.send(new Message(Message.SET_NOEUD_PREC, "Envoyé par : " + this.nodeUid, msg.getUid(), msg.getNetwork_rank(), msg.getNoeud_a_set()), Network.get(nouveau_noeud.noeudSuiv.getNodeId()));
    			nouveau_noeud.send(new Message(Message.SET_NOEUD_SUIV, "Envoyé par : " + this.nodeUid, msg.getUid(), msg.getNetwork_rank(), msg.getNoeud_a_set()), Network.get(nouveau_noeud.noeudPrec.getNodeId()));
    			//System.out.println("TEST 3");
    			System.out.println(CommonState.getTime() + " LE NOEUD n°" + nouveau_noeud.getNodeUid() + " A POUR NOEUD PREC : " + nouveau_noeud.getNoeudPrec().getNodeUid() + " ET POUR NOEUD SUIV : " + nouveau_noeud.getNoeudSuiv().getNodeUid());
    			

    		}
    		
    		else if (this.noeudSuiv.getNodeId() == 0) {
    			System.out.println(CommonState.getTime() + " |C'EST ZERO| MSG UID : " + msg.getUid() + " NOEUD SUIV UID : " + this.noeudSuiv.getNodeUid());
    			DHT nouveau_noeud = (DHT) Network.get(msg.getNetwork_rank()).getProtocol(0);
    			nouveau_noeud.setNoeudSuiv(this.getNoeudSuiv());
    			nouveau_noeud.setNoeudPrec(this);
    			nouveau_noeud.send(new Message(Message.SET_NOEUD_PREC, "Envoyé par : " + this.nodeUid, msg.getUid(), msg.getNetwork_rank(), msg.getNoeud_a_set()), Network.get(0));
    			nouveau_noeud.send(new Message(Message.SET_NOEUD_SUIV, "Envoyé par : " + this.nodeUid, msg.getUid(), msg.getNetwork_rank(), msg.getNoeud_a_set()), Network.get(this.getNodeId()));
    			System.out.println(CommonState.getTime() + " LE NOEUD n°" + nouveau_noeud.getNodeUid() + " A POUR NOEUD PREC : " + nouveau_noeud.getNoeudPrec().getNodeUid() + " ET POUR NOEUD SUIV : " + nouveau_noeud.getNoeudSuiv().getNodeUid());
    		}
    		
    		else {
    			System.out.println(CommonState.getTime() + " |C'EST PLUS| MSG UID : " + msg.getUid() + " NOEUD SUIV UID : " + this.noeudSuiv.getNodeUid());
    			this.send(new Message(Message.PLACE_NODE, "Envoyé par : " +this.nodeUid, msg.getUid(), msg.getNetwork_rank(), msg.getNoeud_a_set()), Network.get(this.noeudSuiv.getNodeId()));
    		}
    		
    		// Pour set les nouveau voisins, on envoit des messages new voisin suiv et prec
    		
    		//Quand on créé un noeud, on met getProtocol(0) à la place de dHTpid
    		//Gab utilise des id en int pour les noeuds suiv et prec
    		
    	}
    	
    	if (msg.getType() == Message.SET_NOEUD_SUIV) {
    		
    		System.out.println(CommonState.getTime() + " |Message.SET_NOEUD_SUIV| NOEUD n°" + this.getNodeUid() +" QUI A POUR NOEUD SUIV : " + this.getNoeudSuiv().getNodeUid());
    		this.setNoeudSuiv(msg.getNoeud_a_set());
    		System.out.println(CommonState.getTime() + " |Message.SET_NOEUD_SUIV| LE NOEUD N° : " + this.getNodeUid() + " A MAINTENANT POUR NOEUD SUIV : " + this.getNoeudSuiv().getNodeUid());
    		
    	}
    	
    	if (msg.getType() == Message.SET_NOEUD_PREC) {
    		
    		System.out.println(CommonState.getTime() + " |Message.SET_NOEUD_PREC| NOEUD n°" + this.getNodeUid() + " QUI A POUR NOEUD PREC : " + this.getNoeudPrec().getNodeUid());
    		this.setNoeudPrec(msg.getNoeud_a_set());
    		System.out.println(CommonState.getTime() + " |Message.SET_NOEUD_PREC| LE NOEUD N° : " + this.getNodeUid() + " A MAINTENANT POUR NOEUD PREC : " + this.getNoeudPrec().getNodeUid());
    		
    	}
    	
    	
    	//Pour le leave, on utilise les message New prec et new suiv en envoyant le noeud prec au noeud suiv et inversement
    	
    	if (msg.getType() == Message.LEAVE) {
    		
    		this.send(new Message(Message.SET_NOEUD_PREC, "Envoyé par : " + this.nodeUid, msg.getUid(), msg.getNetwork_rank(), this.getNoeudPrec()), Network.get(this.getNoeudSuiv().getNodeId()));
    		this.send(new Message(Message.SET_NOEUD_SUIV, "Envoyé par : " + this.nodeUid, msg.getUid(), msg.getNetwork_rank(), this.getNoeudSuiv()), Network.get(this.getNoeudPrec().getNodeId()));
    		
    		//this.getNoeudPrec().setNoeudSuiv(this.getNoeudSuiv());	
    		//this.getNoeudSuiv().setNoeudSuiv(this.getNoeudPrec());
    		
    		this.setNoeudPrec(null);
    		this.setNoeudSuiv(null);
    	}
    	
    	if (msg.getType() == Message.SEND) {
    		
    		//Le noeud doit avertir le noeud 0 qu'il est toujours actif
    		System.out.println(CommonState.getTime() + " |Message.SEND| LE NOEUD N° : " + this.getNodeUid() + " ENVOI UN PING AU NOEUD 0");
    		this.send(new Message(Message.DELIVER, "Envoyé par : " + this.nodeUid + " Je suis toujours actif", this), Network.get(0));
    		
    	}
    	
    	if (msg.getType() == Message.DELIVER) {
    		
    		System.out.println(CommonState.getTime() + " |Message.DELIVER| LE NOEUD N° : " + this.getNodeUid() + " A RECU UN PING DU NOEUD N° : " + msg.getNoeud_a_set().getNodeUid());
    		msg.getNoeud_a_set().setPingEnvoye(true);
    		
    		//Après il faut faire en sorte que si un noeud n'a pas envoyé de ping depuis trop longtemps, il soit retiré de la DHT  car il peut être tombé en panne
    	}
    	
    	if (msg.getType() == Message.PUT) {
    		
    		System.out.println(CommonState.getTime() + " |Message.PUT| AJOUT DE LA DONNEE D'ID N° : " + msg.getDonnee().getId());
    		this.send(new Message(Message.PLACE_DATA, "Envoyé par : " + this.nodeUid, msg.getDonnee()), Network.get(0));
    		
    	}
    	
    	if (msg.getType() == Message.PLACE_DATA) {
    		
    		System.out.println(CommonState.getTime() + " |Message.PLACE_DATA| PLACEMENT DE LA DONNEE D'ID N° : " + msg.getDonnee().getId());
    		
    		if (Math.abs(msg.getDonnee().getId() - this.getNodeUid()) <= Math.abs(msg.getDonnee().getId() - this.getNoeudSuiv().getNodeUid())) {
    			System.out.println(CommonState.getTime() + " |Message.PLACE_DATA| LA DONNEE D'ID N° : " + msg.getDonnee().getId() + " A ETE AJOUTEE AU NOEUD N° : " + this.getNodeUid());
    			this.listeDonnees.add(msg.getDonnee());
    			this.getNoeudPrec().listeDonnees.add(msg.getDonnee());
    			this.getNoeudSuiv().listeDonnees.add(msg.getDonnee());
    			System.out.println(CommonState.getTime() + " |Message.PLACE_DATA| TABLEAU DE DONNEES DU NOEUD N° : " + this.getNodeUid() + " : " + this.getListeDonnees());
    		}
    		
    		else {
    			System.out.println(CommonState.getTime() + " |Message.PLACE_DATA| ON RENVOI LE MESSAGE AU NOEUD N° : " + this.getNoeudSuiv().getNodeUid());
    			this.send(new Message(Message.PLACE_DATA, "Envoyé par : " + this.nodeUid, msg.getDonnee()), Network.get(this.getNoeudSuiv().getNodeId()));
    		}
    	}
    		
    	if (msg.getType() == Message.GET) {
    		
    		System.out.println(CommonState.getTime() + " |Message.GET| RECHERCHE DE LA DONNEE D'ID N° : " + msg.getDonnee().getId() + " SUR LE NOEUD N° : " + this.getNodeUid());
    		
    		if (this.getListeDonnees().contains(msg.getDonnee())) {
    			
    			System.out.println(CommonState.getTime() + " |Message.GET| LE NOEUD N° : " + this.getNodeUid() + " A LA DONNEE N° : " + msg.getDonnee().getId());
    		}
    		
    		else {
    			
    			System.out.println(CommonState.getTime() + " |Message.GET| LA DONNEE N° : " + msg.getDonnee().getId() + " N'EST PAS DISPONIBLE SUR LE NOEUD N° : " + this.getNodeUid());
    			this.send(new Message(Message.GET, "Envoyé par : " + this.nodeUid, msg.getDonnee()), Network.get(this.getNoeudSuiv().getNodeId()));
    		}
    		
    	}
    		
		
    }


    //retourne le noeud courant
    private Node getMyNode() {
	return Network.get(this.nodeId);
    }

    public String toString() {
	return "Node "+ this.nodeId;
    }
    
    public int getNodeId() {
		return nodeId;
	}

	public void setNodeId(int nodeId) {
		this.nodeId = nodeId;
	}

	public int getNodeUid() {
		return nodeUid;
	}

	public void setNodeUid(int nodeUid) {
		this.nodeUid = nodeUid;
	}

	public DHT getNoeudPrec() {
		return noeudPrec;
	}

	public void setNoeudPrec(DHT noeudPrec) {
		this.noeudPrec = noeudPrec;
	}

	public DHT getNoeudSuiv() {
		return noeudSuiv;
	}

	public void setNoeudSuiv(DHT noeudSuiv) {
		this.noeudSuiv = noeudSuiv;
	}

	public boolean isPingEnvoye() {
		return pingEnvoye;
	}

	public void setPingEnvoye(boolean pingEnvoye) {
		this.pingEnvoye = pingEnvoye;
	}

	public ArrayList<Data> getListeDonnees() {
		return listeDonnees;
	}

	public void setListeDonnees(ArrayList<Data> listeDonnees) {
		this.listeDonnees = listeDonnees;
	}

}
