package DHT;

import peersim.edsim.*;
import peersim.core.*;

import java.util.ArrayList;
import java.util.Collections;

import peersim.config.*;

public class DHT implements EDProtocol, Comparable<DHT> {
	
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
    
    //Table de routage
    private ArrayList<DHT> tableRoutage = new ArrayList<DHT>();

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
    	this.transport = (HWTransport) Network.get(this.nodeId).getProtocol(this.transportPid);
    }

    //envoi d'un message (l'envoi se fait via la couche transport)
    public void send(Message msg, Node dest) {
    	this.transport.send(getMyNode(), dest, msg, this.mypid);
    }

    //affichage a la reception, certains affichages sont en commentaire pour faciliter la compréhension de la simulation
    private void receive(Message msg) {
    	Network.get(this.nodeId);
    	
    	
    	//Si le message est de type DHT
    	if (msg.getType() == Message.DHT) {
    		
    		System.out.println(CommonState.getTime() + " |Message.DHT| Node n°" + this.nodeUid + " " + msg.getContent());
    		
    		//Si le noeud actuel est le dernier noeud de la DHT
			if (this.getNoeudSuiv().getNodeId() == 0) {
				//Chaque noeud envoi un message au noeud 0, s'il n'est pas dans la table de routage, il est ajouté
				this.send(new Message(Message.ADD_NODE_TO_TABLE, "Envoyé par : " + this.nodeUid, this), Network.get(0));
	    		System.out.println("TOUTE LA DHT A ETE PARCOURUE");
	    	}
			
			else {
				//Si le noeud n'est pas dans la table de routage, on doit l'ajouter
				this.send(new Message(Message.ADD_NODE_TO_TABLE, "Envoyé par : " + this.nodeUid, this), Network.get(0));

				//Le noeud actuel transmet le message à son voisin, le noeud suivant.
				Node dest = Network.get(this.noeudSuiv.getNodeId());
	    		this.send(new Message(0, "Envoyé par : " + this.nodeUid), dest);
			}
		
    	}
    	
    	//Si le message est de type JOIN
    	if (msg.getType() == Message.JOIN) {
    		
    		//JOIN sert à envoyer un message PLACE_NODE au noeud 0 avec l'uid et l'id du noeud qui rejoint la DHT
    		System.out.println(CommonState.getTime() + " |Message.JOIN| Demande de JOIN du Node n°" + this.nodeUid);
    		this.send(new Message(Message.PLACE_NODE, "Envoyé par : " +this.nodeUid, this.nodeUid, this.nodeId, this), Network.get(0));
    	}
    	
    	//Si le message est de type PLACE_NODE
    	if (msg.getType() == Message.PLACE_NODE) {
    		
    		//System.out.println(CommonState.getTime() + " |Message.PLACE| Node n°" + this.nodeUid + " " + msg.getContent()+ " NOEUD A SET : " + msg.getNoeud_a_set());
    		
    		//On part du noeud 0, et on compare l'uid du noeud à placer avec le noeud suivant
    		//System.out.println(CommonState.getTime() + " MSG UID : " + msg.getUid() + " NOEUD SUIV UID : " + this.noeudSuiv.getNodeUid());
    		
    		//si < uid.suiv on place
    		if (msg.getUid() < this.noeudSuiv.getNodeUid()) {
    			//System.out.println(CommonState.getTime() + " |C'EST MOINS| MSG UID : " + msg.getUid() + " NOEUD SUIV UID : " + this.noeudSuiv.getNodeUid());
    			DHT nouveau_noeud = (DHT) Network.get(msg.getNetwork_rank()).getProtocol(0);
    			nouveau_noeud.setNoeudSuiv(this.getNoeudSuiv()); //Le noeud qui suit le nouveau noeud est le noeud suivant le noeud actuel
    			nouveau_noeud.setNoeudPrec(this); //Le noeud qui précede le nouveau noeud est le noeud actuel
    			
    			//Le noeud précédent du noeud suivant doit être modifié
    			nouveau_noeud.send(new Message(Message.SET_NOEUD_PREC, "Envoyé par : " + this.nodeUid, msg.getUid(), msg.getNetwork_rank(), msg.getNoeud_a_set()), Network.get(nouveau_noeud.noeudSuiv.getNodeId()));
    			//Le noeud suivant du noeud précédent doit être modifié
    			nouveau_noeud.send(new Message(Message.SET_NOEUD_SUIV, "Envoyé par : " + this.nodeUid, msg.getUid(), msg.getNetwork_rank(), msg.getNoeud_a_set()), Network.get(nouveau_noeud.noeudPrec.getNodeId()));
    			
    			//On ajoute le nouveau noeud à la table de routage
    			this.send(new Message(Message.ADD_NODE_TO_TABLE, "Envoyé par : " + this.nodeUid, msg.getNoeud_a_set()), Network.get(0));
    			System.out.println(CommonState.getTime() + " LE NOEUD n°" + nouveau_noeud.getNodeUid() + " A POUR NOEUD PREC : " + nouveau_noeud.getNoeudPrec().getNodeUid() + " ET POUR NOEUD SUIV : " + nouveau_noeud.getNoeudSuiv().getNodeUid());
    			

    		}
    		
    		//sinon si noeudSuiv.id == 0, le noeud qui rejoint à le plus grand uid, on place le noeud
    		else if (this.noeudSuiv.getNodeId() == 0) {
    			//System.out.println(CommonState.getTime() + " |C'EST ZERO| MSG UID : " + msg.getUid() + " NOEUD SUIV UID : " + this.noeudSuiv.getNodeUid());
    			DHT nouveau_noeud = (DHT) Network.get(msg.getNetwork_rank()).getProtocol(0);
    			nouveau_noeud.setNoeudSuiv(this.getNoeudSuiv()); //Le noeud qui suit le nouveau noeud est le noeud suivant le noeud actuel
    			nouveau_noeud.setNoeudPrec(this); //Le noeud qui précede le nouveau noeud est le noeud actuel
    			
    			//Il faut maintenant que les voisins des noeuds autour du nouveau noeud soient actualisés
    			nouveau_noeud.send(new Message(Message.SET_NOEUD_PREC, "Envoyé par : " + this.nodeUid, msg.getUid(), msg.getNetwork_rank(), msg.getNoeud_a_set()), Network.get(0));
    			nouveau_noeud.send(new Message(Message.SET_NOEUD_SUIV, "Envoyé par : " + this.nodeUid, msg.getUid(), msg.getNetwork_rank(), msg.getNoeud_a_set()), Network.get(this.getNodeId()));
    			
    			//On ajoute le nouveau noeud à la table de routage
    			this.send(new Message(Message.ADD_NODE_TO_TABLE, "Envoyé par : " + this.nodeUid, msg.getNoeud_a_set()), Network.get(0));
    			System.out.println(CommonState.getTime() + " LE NOEUD n°" + nouveau_noeud.getNodeUid() + " A POUR NOEUD PREC : " + nouveau_noeud.getNoeudPrec().getNodeUid() + " ET POUR NOEUD SUIV : " + nouveau_noeud.getNoeudSuiv().getNodeUid());
    		}
    		
    		//sinon, on test avec le noeud suivant
    		else {
    			//System.out.println(CommonState.getTime() + " |C'EST PLUS| MSG UID : " + msg.getUid() + " NOEUD SUIV UID : " + this.noeudSuiv.getNodeUid());
    			this.send(new Message(Message.PLACE_NODE, "Envoyé par : " +this.nodeUid, msg.getUid(), msg.getNetwork_rank(), msg.getNoeud_a_set()), Network.get(this.noeudSuiv.getNodeId()));
    		}
    		
    		
    	}
    	
    	//Pour set les nouveau voisins, on envoit des messages SET_NOEUD_SUIV et SET_NOEUD_PREC
    	//Si le message est de type SET_NOEUD_SUIV
    	if (msg.getType() == Message.SET_NOEUD_SUIV) {
    		
    		System.out.println(CommonState.getTime() + " |Message.SET_NOEUD_SUIV| NOEUD n°" + this.getNodeUid() +" QUI A POUR NOEUD SUIV : " + this.getNoeudSuiv().getNodeUid());
    		this.setNoeudSuiv(msg.getNoeud_a_set());
    		System.out.println(CommonState.getTime() + " |Message.SET_NOEUD_SUIV| LE NOEUD N° : " + this.getNodeUid() + " A MAINTENANT POUR NOEUD SUIV : " + this.getNoeudSuiv().getNodeUid());
    		
    	}
    	
    	//Si le message est de type SET_NOEUD_PREC
    	if (msg.getType() == Message.SET_NOEUD_PREC) {
    		
    		System.out.println(CommonState.getTime() + " |Message.SET_NOEUD_PREC| NOEUD n°" + this.getNodeUid() + " QUI A POUR NOEUD PREC : " + this.getNoeudPrec().getNodeUid());
    		this.setNoeudPrec(msg.getNoeud_a_set());
    		System.out.println(CommonState.getTime() + " |Message.SET_NOEUD_PREC| LE NOEUD N° : " + this.getNodeUid() + " A MAINTENANT POUR NOEUD PREC : " + this.getNoeudPrec().getNodeUid());
    		
    	}
    	
    	
    	//Pour le leave, on utilise les message New prec et new suiv en envoyant le noeud prec au noeud suiv et inversement
    	
    	//Si le message est de type LEAVE
    	if (msg.getType() == Message.LEAVE) {
    		
    		System.out.println(CommonState.getTime() + " |Message.LEAVE| Node n°" + this.nodeUid + " " + msg.getContent());
    		
    		this.send(new Message(Message.SET_NOEUD_PREC, "Envoyé par : " + this.nodeUid, msg.getUid(), msg.getNetwork_rank(), this.getNoeudPrec()), Network.get(this.getNoeudSuiv().getNodeId()));
    		this.send(new Message(Message.SET_NOEUD_SUIV, "Envoyé par : " + this.nodeUid, msg.getUid(), msg.getNetwork_rank(), this.getNoeudSuiv()), Network.get(this.getNoeudPrec().getNodeId()));
    		
    		//On set à null les voisins du noeud qui a quitté la DHT
    		this.setNoeudPrec(null);
    		this.setNoeudSuiv(null);
    		
    		//On l'enlève aussi de la table de routage
    		this.send(new Message(Message.REMOVE_NODE_TO_TABLE, "Envoyé par : " + this.nodeUid, this), Network.get(0));
    	}
    	
    	//Si le message est de type SEND
    	if (msg.getType() == Message.SEND) {
    		
    		//Le noeud doit avertir le noeud 0 qu'il est toujours actif
    		System.out.println(CommonState.getTime() + " |Message.SEND| LE NOEUD N° : " + this.getNodeUid() + " ENVOI UN PING AU NOEUD 0");
    		this.send(new Message(Message.DELIVER, "Envoyé par : " + this.nodeUid + " Je suis toujours actif", this), Network.get(0));
    		
    	}
    	
    	//Si le message est de type DELIVER
    	if (msg.getType() == Message.DELIVER) {
    		
    		System.out.println(CommonState.getTime() + " |Message.DELIVER| LE NOEUD N° : " + this.getNodeUid() + " A RECU UN PING DU NOEUD N° : " + msg.getNoeud_a_set().getNodeUid());
    		msg.getNoeud_a_set().setPingEnvoye(true);
    		
    		//Après il faut faire en sorte que si un noeud n'a pas envoyé de ping depuis trop longtemps, il soit retiré de la DHT  car il peut être tombé en panne
    	}
    	
    	//Si le message est de type PUT
    	if (msg.getType() == Message.PUT) {
    		
    		System.out.println(CommonState.getTime() + " |Message.PUT| AJOUT DE LA DONNEE D'ID N° : " + msg.getDonnee().getId());
    		this.send(new Message(Message.PLACE_DATA, "Envoyé par : " + this.nodeUid, msg.getDonnee()), Network.get(0));
    		
    	}
    	
    	//Si le message est de type PLACE_DATA
    	if (msg.getType() == Message.PLACE_DATA) {
    		
    		//System.out.println(CommonState.getTime() + " |Message.PLACE_DATA| PLACEMENT DE LA DONNEE D'ID N° : " + msg.getDonnee().getId());
    		
    		//Il faut placer la donnée sur le noeud dont l'uid est le plus proche de l'id de la donnée, nous allons raisonner un peu comme pour JOIN
    		
    		//Pour déterminer le noeud d'accueil de la donnée, on va calculer à partir du noeud 0 la différence entre l'id de la donnée
    		//et l'uid du noeud actuel pour la comparer avec la différence entre l'id de la donnée et l'uid du noeud suivant
    		
    		//Si la valeur absolue de la différence entre l'id de la donnée et l'uid du noeud actuel est inférieure à la différence entre 
    		// l'id de la donnée et l'uid du noeud suivant, on place la donnée sur le noeud actuel
    		if (Math.abs(msg.getDonnee().getId() - this.getNodeUid()) <= Math.abs(msg.getDonnee().getId() - this.getNoeudSuiv().getNodeUid())) {
    			System.out.println(CommonState.getTime() + " |Message.PLACE_DATA| LA DONNEE D'ID N° : " + msg.getDonnee().getId() + " A ETE AJOUTEE AU NOEUD N° : " + this.getNodeUid());
    			this.listeDonnees.add(msg.getDonnee());
    			
    			//Afin d'assurer la réplication des données, on ajoute aussi la donnée aux noeuds voisins
    			this.getNoeudPrec().listeDonnees.add(msg.getDonnee());
    			this.getNoeudSuiv().listeDonnees.add(msg.getDonnee());
    			System.out.println(CommonState.getTime() + " |Message.PLACE_DATA| TABLEAU DE DONNEES DU NOEUD N° : " + this.getNodeUid() + " : " + this.getListeDonnees());
    		}
    		
    		//Sinon, on passe au noeud suivant
    		else {
    			//System.out.println(CommonState.getTime() + " |Message.PLACE_DATA| ON RENVOI LE MESSAGE AU NOEUD N° : " + this.getNoeudSuiv().getNodeUid());
    			this.send(new Message(Message.PLACE_DATA, "Envoyé par : " + this.nodeUid, msg.getDonnee()), Network.get(this.getNoeudSuiv().getNodeId()));
    		}
    	}
    	
    	//Si le message est de type GET
    	if (msg.getType() == Message.GET) {
    		
    		System.out.println(CommonState.getTime() + " |Message.GET| RECHERCHE DE LA DONNEE D'ID N° : " + msg.getDonnee().getId() + " SUR LE NOEUD N° : " + this.getNodeUid());
    		
    		//Si la donnée est sur le noeud, on la récupère
    		if (this.getListeDonnees().contains(msg.getDonnee())) {
    			
    			System.out.println(CommonState.getTime() + " |Message.GET| LE NOEUD N° : " + this.getNodeUid() + " A LA DONNEE N° : " + msg.getDonnee().getId());
    		}
    		
    		//Sinon, on passe au noeud suivant
    		else {
    			
    			System.out.println(CommonState.getTime() + " |Message.GET| LA DONNEE N° : " + msg.getDonnee().getId() + " N'EST PAS DISPONIBLE SUR LE NOEUD N° : " + this.getNodeUid());
    			this.send(new Message(Message.GET, "Envoyé par : " + this.nodeUid, msg.getDonnee()), Network.get(this.getNoeudSuiv().getNodeId()));
    		}
    		
    	}
    	
    	//Si le message est de type ADD_NODE_TO_TABLE
    	if (msg.getType() == Message.ADD_NODE_TO_TABLE) {
    		
    		//Si la table de routage ne contient pas le noeud, il est ajouté
    		if (!this.tableRoutage.contains(msg.getNoeud_a_set())){
	    		//System.out.println(CommonState.getTime() + " |Message.ADD_NODE_TO_TABLE| ON AJOUTE LE NOEUD N° : " + msg.getNoeud_a_set().getNodeUid() + " A LA TABLE DE ROUTAGE");
	    		this.tableRoutage.add(msg.getNoeud_a_set());
	    		//this.send(new Message(Message.ACCESS_TABLE, "Envoyé par : " + this.nodeUid), Network.get(0));
    		}
    		
    	}
    	
    	//Si le message est de type REMOVE_NODE_TO_TABLE
    	if (msg.getType() == Message.REMOVE_NODE_TO_TABLE) {
    		
    		//Le noeud en question est enlevé de la liste
    		System.out.println(CommonState.getTime() + " |Message.REMOVE_NODE_TO_TABLE| ON ENLEVE LE NOEUD N° : " + msg.getNoeud_a_set().getNodeUid() + " A LA TABLE DE ROUTAGE");
    		this.tableRoutage.remove(msg.getNoeud_a_set());
    		this.send(new Message(Message.ACCESS_TABLE, "Envoyé par : " + this.nodeUid), Network.get(0));
    		
    	}
    	
    	//Si le message est de type ACCESS_TABLE
    	if (msg.getType() == Message.ACCESS_TABLE) {
    		
    		//Il faut trier la liste pour représenter l'ordre des noeuds dans la DHT
    		Collections.sort(tableRoutage);
    		
    		System.out.println(CommonState.getTime() + " |Message.ACCESS_TABLE| VOICI LA TABLE DE ROUTAGE : " + this.tableRoutage);
    		
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

	//Méthode permettant de trier les noeuds suivant leur uid
	
	@Override
	public int compareTo(DHT d) {
		Integer i = this.getNodeUid();
		return i.compareTo(d.getNodeUid());
	}

}
