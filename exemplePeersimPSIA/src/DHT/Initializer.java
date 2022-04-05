package DHT;

import peersim.edsim.*;
import peersim.core.*;

import java.util.ArrayList;
import java.util.Random;


import peersim.config.*;

/*
  Module d'initialisation de helloWorld: 
  Fonctionnement:
    pour chaque noeud, le module fait le lien entre la couche transport et la couche applicative
    ensuite, il fait envoyer au noeud 0 un message "Hello" a tous les autres noeuds
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
	
	//création des trois premier noeuds
	DHT noeud0 = (DHT) Network.get(0).getProtocol(this.dHTPid);
	noeud0.setTransportLayer(0, 0);



	//recuperation de la couche applicative de l'emetteur (le noeud 0)
	//emitter = (DHT) Network.get(0).getProtocol(this.dHTPid);
	//emitter.setTransportLayer(0);
	emitter = noeud0;

	//pour chaque noeud, on fait le lien entre la couche applicative et la couche transport
	//puis on fait envoyer au noeud 0 un message "Hello"
	
	for (int i = 1; i < nodeNb; i++) { //Pour un réseau de 20 noeuds, on en initialise que 3, on les connecte (infos) et on essaye d'en rajouter un
		
		int nodeUid = new Random().nextInt(1000) + 1;
		while (nodeIds.contains(nodeUid)) {
			nodeUid = new Random().nextInt(1000) + 1;
		}
		nodeIds.add(nodeUid);
	    current = (DHT) Network.get(i).getProtocol(this.dHTPid);
	    current.setTransportLayer(i, nodeUid);
	    //emitter.send(helloMsg, dest);
	}
	
	System.out.println(nodeIds);
	
	DHT noeud1 = (DHT) Network.get(1).getProtocol(this.dHTPid);
	DHT noeud2 = (DHT) Network.get(2).getProtocol(this.dHTPid);
	
	if (noeud1.getNodeUid() < noeud2.getNodeUid()) {
		
		noeud0.setNoeudPrec(noeud2);
		noeud0.setNoeudSuiv(noeud1);
		
		noeud1.setNoeudPrec(noeud0);
		noeud1.setNoeudSuiv(noeud2);
		
		noeud2.setNoeudPrec(noeud1);
		noeud2.setNoeudSuiv(noeud0);
	}
	
	if (noeud1.getNodeUid() > noeud2.getNodeUid()) {
		
		noeud0.setNoeudPrec(noeud1);
		noeud0.setNoeudSuiv(noeud2);
		
		noeud1.setNoeudPrec(noeud2);
		noeud1.setNoeudSuiv(noeud0);
		
		noeud2.setNoeudPrec(noeud0);
		noeud2.setNoeudSuiv(noeud1);
	}

	dest = Network.get(0);
	emitter.send(new Message(Message.DHT,"Envoyé par : " + noeud0.getNodeUid()), dest);
	
	EDSimulator.add(1000, new Message(Message.JOIN,"Demande de JOIN"), Network.get(3), 0);
	
	
	EDSimulator.add(2000, new Message(Message.DHT,"Test du JOIN"), Network.get(0), 0);
	
	

	System.out.println("Initialization completed");
	return false;
    }
}