package br.ufrj.dcc.proc;

import java.util.Vector;

import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import com.sun.spot.peripheral.radio.Application;
import com.sun.spot.peripheral.radio.DataMessage;
import com.sun.spot.peripheral.radio.RoutingEntry;
import com.sun.spot.peripheral.radio.RoutingInterface;
import com.sun.spot.peripheral.radio.proc.util.Log;
import com.sun.spot.util.IEEEAddress;

abstract public class Node extends MIDlet implements Application, Runnable {
	private RoutingInterface router;
	private Thread main;
	protected final Log log;
	
	public Node(String name) {
		router = new RoutingInterface(this);
		log = new Log(IEEEAddress.toDottedHex(this.router.getAddress()));
		main = new Thread(this);
		
		router.setApp(this);
		router.setLogLevel(Log.DEBUG | Log.INFO);
		log.setLevel(Log.DEBUG | Log.INFO);
		
	}
	
	protected RoutingInterface getRoutingInterface() {
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
		log.info("GetRoutingRules");
		return 0;
	}

	public void newCycleStarted(int cycle, boolean coord) {
		log.info("StartNewCycle");
	}

	public String prepareRoutingPacket(String message, long address) {
		log.info("PrepareRoutingPacket");
		return message;
	}

	public void forcedCoord() {
		log.info("ForcedCoord");
	}

	public String forwardData(String message, long address) {
		log.info("ForwardData");
		return message;
	}
	
	public void waitNotInterrupted(int inTime) {
		router.waitNotInterrupted(inTime);
	}

	public boolean send(int message) {
		log.info("Send " + message);
		return router.sendDataPacket(new DataMessage(message));
	}
	
	public void run() {
		while (true) {
			mainStep();
		}
	}
	
	protected abstract void mainStep();
}
