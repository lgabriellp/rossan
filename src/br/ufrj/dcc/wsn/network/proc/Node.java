package br.ufrj.dcc.wsn.network.proc;

import java.util.Vector;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.util.Logger;

import com.sun.spot.util.IEEEAddress;


public abstract class Node extends MIDlet implements Application, Runnable {
	private NetworkInterface router;
	private Thread main;
	protected final Logger log;
	
	public Node(String name) {
		router = new NetworkInterface(this);
		log = new Logger(IEEEAddress.toDottedHex(this.router.getAddress()));
		main = new Thread(this);
		
		router.setApp(this);
		log.setLevel(Logger.DEBUG | Logger.INFO);
	}
	
	protected NetworkInterface getRoutingInterface() {
		return this.router;
	}
	
	protected void startApp() throws MIDletStateChangeException {
		log.info("Starting");
		router.startListening();
		main.start();
	}
    
    protected void pauseApp() {
    	log.info("Pausing");
    }
    
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        log.info("Terminating");
        router.interrupt();
    }

	public int getRoutingRules(Vector neighbors, RoutingEntry parent) {
		String p = "null";
		if (parent != null) {
			p = IEEEAddress.toDottedHex(parent.getAddress());
		}
		log.info("getRoutingRules neighbor.size="+neighbors.size()+" parent.address="+p);
		return 0;
	}

	public void startRoutingCycle(int cycle, boolean coord) {
		log.info("StartingRoutingCycle cycle="+cycle+" coord="+coord);
	}

	public void joinedToBackbone() {
		log.info("JoinedToBackbone");
	}

	public Message processRoutingMessage(Message message, long address) {
		log.info("PrepareRoutingMessage "+message+" address="+IEEEAddress.toDottedHex(address));
		return message;
	}

	public Message processDataMessage(PacketReader reader, long address) {
		log.info("PrepareDataMessage to.address="+IEEEAddress.toDottedHex(address));
		return null;
	}
	
	public boolean waitNotInterrupted(int inTime) {
		return router.waitNotInterrupted(inTime);
	}

	public void send(Message message) {
		log.info("Sending "+message);
		router.sendDataPacket(message);
	}
	
	public void run() {
		while (mainStep());
	}
	
	protected abstract boolean mainStep();
}
