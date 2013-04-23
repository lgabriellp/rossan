package br.ufrj.dcc.proc;

import java.util.Vector;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.spot.peripheral.radio.Application;
import com.sun.spot.peripheral.radio.RoutingEntry;
import com.sun.spot.peripheral.radio.RoutingInterface;

public class Node extends MIDlet implements Application {
	private RoutingInterface router;
	
	public Node() {
		router = new RoutingInterface(this);
		router.setApp(this);
	}
	
	protected void startApp() throws MIDletStateChangeException {
		log("Starting");
		router.startListening();
	}
    
    protected void pauseApp() {
    	log("Pausing");
    }
    
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        log("Terminating");
        router.interrupt();
    }

	public int getRoutingRules(Vector neighbors, RoutingEntry parent) {
		log("GetRoutingRules");
		return 0;
	}

	public void startNewCycle(int cycle, boolean coord) {
		log("StartNewCycle");
	}

	public String prepareRoutingPacket(String message, long address) {
		log("PrepareRoutingPacket " + message + " to " + address);
		return message;
	}

	public void forcedCoord() {
		log("ForcedCoord");
	}

	public String forwardData(String message) {
		log("ForwardData");
		return message;
	}
	
	private void log(String message) {
		System.out.println("Node: " + message);
	}
}
