package DHT;

import peersim.edsim.*;
import peersim.core.*;

import java.util.ArrayList;
import java.util.Random;


import peersim.config.*;

/*
  Module d'initialisation de la DHT : 
  Fonctionnement:
    pour chaque noeud, le module fait le lien entre la couche transport et la couche applicative
    ensuite, on peut ajouter des événements au simulateur
 */
public class Initializer implements peersim.core.Control {
    
    private int dHTPid;
    private ArrayList<Integer> nodeIds = new ArrayList<Integer>();

    public Initializer(String prefix) {
	//recuperation du pid de la couche applicative
	this.dHTPid = Configuration.getPid(prefix + ".dHTProtocolPid");
    }

    public boolean execute() {
	int nodeNb;
	DHT emitter, current;
	Node dest;

	//recuperation de la taille du reseau
	nodeNb = Network.size();
	System.out.println(nodeNb);
	//creation du message
	if (nodeNb < 1) {
	    System.err.println("Network size is not positive");
	    System.exit(1);
	}
	
	//création du premier noeud
	DHT noeud0 = (DHT) Network.get(0).getProtocol(this.dHTPid);
	//recuperation de la couche applicative de l'emetteur (le noeud 0)
	noeud0.setTransportLayer(0, 0);

	//Le premier émetteur est le noeud 0
	emitter = noeud0;

	//pour chaque noeud, on fait le lien entre la couche applicative et la couche transport et on leur assigne un uid aléatoire
	
	for (int i = 1; i < nodeNb; i++) {
		
		//On fait en sorte que le uid soit unique
		int nodeUid = new Random().nextInt(1000) + 1;
		while (nodeIds.contains(nodeUid)) {
			nodeUid = new Random().nextInt(1000) + 1;
		}
		nodeIds.add(nodeUid);
	    current = (DHT) Network.get(i).getProtocol(this.dHTPid);
	    current.setTransportLayer(i, nodeUid);

	}
	
	System.out.println(nodeIds);
	
	//Nous allons définir les voisins pour les trois premiers noeuds afin de les rendre "actifs"
	
	DHT noeud1 = (DHT) Network.get(1).getProtocol(this.dHTPid);
	DHT noeud2 = (DHT) Network.get(2).getProtocol(this.dHTPid);
	
	//Si le noeud 2 doit suivre le noeud 1
	if (noeud1.getNodeUid() < noeud2.getNodeUid()) {
		
		noeud0.setNoeudPrec(noeud2);
		noeud0.setNoeudSuiv(noeud1);
		
		noeud1.setNoeudPrec(noeud0);
		noeud1.setNoeudSuiv(noeud2);
		
		noeud2.setNoeudPrec(noeud1);
		noeud2.setNoeudSuiv(noeud0);
	}
	
	//Si le noeud 2 doit précéder le noeud 1
	if (noeud1.getNodeUid() > noeud2.getNodeUid()) {
		
		noeud0.setNoeudPrec(noeud1);
		noeud0.setNoeudSuiv(noeud2);
		
		noeud1.setNoeudPrec(noeud2);
		noeud1.setNoeudSuiv(noeud0);
		
		noeud2.setNoeudPrec(noeud0);
		noeud2.setNoeudSuiv(noeud1);
	}

	//On envoi un message de type DHT qui est notre message de base au noeud 0 afin de s'assurer que les noeuds sont bien paramétrés
	dest = Network.get(0);
	emitter.send(new Message(Message.DHT,"Envoyé par : " + noeud0.getNodeUid()), dest);
	
	//Nous pouvons maintenant ajouter des événements avec un délai prédéfini
	
	EDSimulator.add(1000, new Message(Message.JOIN,"Demande de JOIN"), Network.get(3), 0);
	
	EDSimulator.add(1700, new Message(Message.ACCESS_TABLE,"Test du ACCES_TABLE"), Network.get(0), 0);
	
	EDSimulator.add(2000, new Message(Message.DHT,"Test du JOIN"), Network.get(0), 0);
	
	EDSimulator.add(2400, new Message(Message.SEND,"Ping du noeud ajouté"), Network.get(3), 0);
	
	EDSimulator.add(2500, new Message(Message.LEAVE,"Leave du noeud"), Network.get(3), 0);
	
	EDSimulator.add(3000, new Message(Message.DHT,"Test du JOIN"), Network.get(0), 0);
	
	EDSimulator.add(3500, new Message(Message.JOIN,"Demande de JOIN"), Network.get(4), 0);
	
	EDSimulator.add(4000, new Message(Message.JOIN,"Demande de JOIN"), Network.get(5), 0);
	
	EDSimulator.add(4500, new Message(Message.DHT,"Test du JOIN"), Network.get(0), 0);
	
	Data dataTest = new Data(new Random().nextInt(1000) + 1);
	
	EDSimulator.add(5000, new Message(Message.PUT,"Test du PUT", dataTest), Network.get(0), 0);
	
	EDSimulator.add(5500, new Message(Message.GET,"Test du GET", dataTest), Network.get(0), 0);
	
	EDSimulator.add(6000, new Message(Message.ACCESS_TABLE,"Test du ACCES_TABLE"), Network.get(0), 0);

	System.out.println("Initialization completed");
	return false;
    }
}