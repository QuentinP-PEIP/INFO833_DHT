package helloWorld;

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
	Message helloMsg;

	//recuperation de la taille du reseau
	nodeNb = Network.size();
	System.out.println(nodeNb);
	//creation du message
	helloMsg = new Message(Message.DHT,"Coucou Clément");
	if (nodeNb < 1) {
	    System.err.println("Network size is not positive");
	    System.exit(1);
	}

	//recuperation de la couche applicative de l'emetteur (le noeud 0)
	emitter = (DHT) Network.get(0).getProtocol(this.dHTPid);
	emitter.setTransportLayer(0);

	//pour chaque noeud, on fait le lien entre la couche applicative et la couche transport
	//puis on fait envoyer au noeud 0 un message "Hello"
	
	
	
	for (int i = 1; i < nodeNb; i++) { //Pour un réseau de 20 noeuds, on en initialise que 3, on les connecte (infos) et on essaye d'en rajouter un
		
		int nodeId = new Random().nextInt(nodeNb-1) + 1;
		while (nodeIds.contains(nodeId)) {
			nodeId = new Random().nextInt(nodeNb-1) + 1;
		}
		nodeIds.add(nodeId);
		System.out.println(nodeIds);
	    dest = Network.get(i);
	    current = (DHT)dest.getProtocol(this.dHTPid);
	    current.setTransportLayer(nodeId);
	    //emitter.send(helloMsg, dest);
	}
	
	dest = Network.get(1);
	emitter.send(helloMsg, dest);
	
	

	System.out.println("Initialization completed");
	return false;
    }
}