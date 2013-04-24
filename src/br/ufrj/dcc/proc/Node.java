package br.ufrj.dcc.proc;

import java.util.Vector;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.spot.peripheral.radio.Application;
import com.sun.spot.peripheral.radio.RoutingEntry;
import com.sun.spot.peripheral.radio.RoutingInterface;
import com.sun.spot.util.IEEEAddress;

abstract public class Node extends MIDlet implements Application, Runnable {
	private RoutingInterface router;
	private Thread main;
	private String name;
	private boolean debug;
	
	public Node(String name) {
		this.router = new RoutingInterface(this);
		this.router.setDebug(2);
		this.debug = false;
		this.main = new Thread(this);
		this.name = name;  
		router.setApp(this);
	}
	
	protected RoutingInterface getRoutingInterface() {
		return this.router;
	}
	
	protected void startApp() throws MIDletStateChangeException {
		log("Starting");
		router.startListening();
		main.start();
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

	public void newCycleStarted(int cycle, boolean coord) {
		log("StartNewCycle");
	}

	public String prepareRoutingPacket(String message, long address) {
		log("PrepareRoutingPacket " + message + " to " + IEEEAddress.toDottedHex(address));
		return message;
	}

	public void forcedCoord() {
		log("ForcedCoord");
	}

	public String forwardData(String message, long address) {
		log("ForwardData " + message + " to " + IEEEAddress.toDottedHex(address));
		return message;
	}
	
	public void waitNotInterrupted(int inTime) {
		router.waitNotInterrupted(inTime);
	}

	public boolean send(String message) {
		log("Send " + message);
		return router.sendDataPacket(message);
	}
	
	protected void log(String message) {
		if (!debug)
			return;
		
		System.out.println("[" + IEEEAddress.toDottedHex(router.getAddress()) + "] " + name + ": " + message);
	}
	
	public void run() {
		while (true) {
			mainStep();
		}
	}
	
	protected abstract void mainStep();
}
