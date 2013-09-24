package br.ufrj.dcc.wsn.network.proc;

import java.util.Vector;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.util.Logger;

import com.sun.spot.peripheral.ISpot;
import com.sun.spot.peripheral.Spot;
import com.sun.spot.util.IEEEAddress;


public abstract class Node extends MIDlet implements Application, Runnable {
	private final NetworkInterface router;
	private final Thread main;
	protected final Logger log;
	private final int interval;
	private ApplicationBehavior behavior;
	
	public Node(String name) {
		ISpot spot = Spot.getInstance();
		spot.setPersistentProperty("Range", getAppProperty("Range"));
		spot.setPersistentProperty("Position", getAppProperty("Position"));
		spot.setPersistentProperty("Behavior", getAppProperty("Behavior"));
		
		int behavior = Integer.parseInt(getAppProperty("Behavior"));
		if (behavior == 0)
			this.behavior = new NodeDensityBehavior();
		else if (behavior == 1)
			this.behavior = new ExponentialBehavior();
		else
			this.behavior = new DummyBehavior();
		
		this.interval = Integer.parseInt(getAppProperty("Interval"));
		this.router = NetworkInterface.getInstance();
		this.log = router.getLog();
		this.main = new Thread(this);
		router.setApp(this);
	}
	
	protected NetworkInterface getRoutingInterface() {
		return this.router;
	}
	
	protected void startApp() throws MIDletStateChangeException {
		log.log(Logger.APP, "Starting");
		router.startListening();
		main.start();
	}
    
    protected void pauseApp() {
    	log.log(Logger.APP, "Pausing");
    }
    
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
    	log.log(Logger.APP, "Terminating");
        router.interrupt();
    }

	public int getRoutingRules(Vector neighbors, RoutingEntry parent) {
		String p = "null";
		if (parent != null) {
			p = IEEEAddress.toDottedHex(parent.getAddress());
		}
		for (int i = 0; i < neighbors.size(); i++) {
			log.log(Logger.APP, "Neighbor: "+ IEEEAddress.toDottedHex(((RoutingEntry)neighbors.elementAt(i)).getAddress()));
		}
		log.log(Logger.APP, "parent: "+p);
		
		return behavior.getRoutingRules(neighbors, parent);
	}

	public void startRoutingCycle(int cycle, boolean coord) {
		log.log(Logger.APP, "StartingRoutingCycle cycle="+cycle+" coord="+coord);
		behavior.startRoutingCycle(cycle, coord);
	}

	public void joinedBackbone() {
		log.log(Logger.APP, "JoinedToBackbone");
		behavior.joinedBackbone();
	}

	public Message processRoutingMessage(Message message, long address) {
		log.log(Logger.APP, "PrepareRoutingMessage "+message+" address="+IEEEAddress.toDottedHex(address));
		return message;
	}

	public Message processDataMessage(PacketReader reader, long address) {
		log.log(Logger.APP, "PrepareDataMessage to.address="+IEEEAddress.toDottedHex(address));
		return null;
	}
	
	public boolean waitNotInterrupted(int inTime) {
		return router.waitNotInterrupted(inTime);
	}

	public boolean send(Message message) {
		boolean success = router.sendDataPacket(message);
		
		if (success) {
			log.log(Logger.APP, "Sent "+message);
		}
		
		return success;
	}
	
	public long getAddress() {
		return router.getAddress();
	}
	
	public int getInterval() {
		return interval;
	}
	
	public boolean hasNoRoute() {
		return router.hasNoRoute();
	}
	
	public void run() {
		while (mainStep());
	}
	
	protected abstract boolean mainStep();
}
