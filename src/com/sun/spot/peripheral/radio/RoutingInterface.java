package com.sun.spot.peripheral.radio;

import java.util.Random;
import java.util.Vector;

import com.sun.spot.peripheral.radio.proc.util.Tokenizer;
import com.sun.spot.util.IEEEAddress;


public class RoutingInterface implements Runnable {
	public static final long BROADCAST = 0xFFFF;
	public static final String SYNC = "sync";
	public static final String COORD = "coord";
	public static final String DATA = "data";
	public static final int BACKOFF_MAX_WAIT = 1000;
	public static final int ROUTING_RULES_MAX = 100;
	
	private static final Random random = new Random();
	
	private char[] buffer = new char[RadioPacket.MIN_PAYLOAD_LENGTH];
	private I802_15_4_MAC mac;
	private Vector neighbors;
	private Application app;
	private Thread receiver;
	private RadioPacket input;
	private RadioPacket output;
	private RoutingEntry state;
	private RoutingEntry parent;
	private boolean debug;
	
	public RoutingInterface(Application app) {
		this.mac = RadioFactory.getI802_15_4_MAC();
		this.neighbors = new Vector();
		this.input = RadioPacket.getDataPacket();
		this.output = RadioPacket.getDataPacket();
		this.receiver = new Thread(this);
		this.state = new RoutingEntry();
		this.parent = null;
		this.app = app;
		this.debug = false;
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	private void log(String message) {
		if (debug == false)
			return;
		
		System.out.println("[" + IEEEAddress.toDottedHex(getAddress()) + "] " + message);
	}
	
	public long getAddress() {
		return RadioFactory.getRadioPolicyManager().getIEEEAddress();
	}
	
	protected int coordinatorCompare(RoutingEntry e1, RoutingEntry e2) {
		int diff = e1.hops - e2.hops;
		
		if (diff != 0) return diff;
		
		diff = (e1.coord ? 1 : 0) - (e2.coord ? 1 : 0);
		
		if (diff != 0) return diff;
		
		diff = -(e1.energy - e2.energy);
		
		return diff;
	}
	
	protected int leafCompare(RoutingEntry e1, RoutingEntry e2) {
		int diff = (e1.coord ? 1 : 0) - (e2.coord ? 1 : 0); 
		
		if (diff != 0) return diff;
		
		diff = e1.hops - e2.hops;
		
		if (diff != 0) return diff;
		
		diff = -(e1.energy - e2.energy);
		
		return diff;
	}
	
	protected void refreshParent() {
		for (int i = 0; i < neighbors.size(); i++) {
			RoutingEntry e1 = (RoutingEntry)neighbors.elementAt(i);
			
			for (int j = 0; j < neighbors.size(); j++) {
				RoutingEntry e2 = (RoutingEntry)neighbors.elementAt(j);
				
				if ((state.coord && coordinatorCompare(e1, e2) < 0) ||
					(!state.coord && leafCompare(e1, e2) < 0)) {
					
					neighbors.setElementAt(e1, j);
					neighbors.setElementAt(e2, i);
				}
			}
		}

		parent = (RoutingEntry)neighbors.elementAt(0);
		log("update parent to " + IEEEAddress.toDottedHex(parent.source));
	}
	
	public void setCycle(int cycle) {
		state.cycle = cycle;
		log("update cycle to " + cycle);
		notifyNewCycle();
	}
	
	protected void setHops(int hops) {
		state.hops = hops;
		log("update hops to " + hops);
	}
	
	protected void setCoord(boolean coord) {
		state.coord = coord;
		log("update coord to " + coord);
	}
	
	protected RadioPacket readPacket() {
		mac.mcpsDataIndication(input);
		return input;
	}
	
	protected String getRoutingMessage() {
		return state.toString();
	}
	
	protected void writeMessage(String message) {
		output.setMACPayloadLength(message.getBytes().length);
		for (int i = 0; i < output.getMACPayloadLength(); i++) {
			output.setMACPayloadAt(i,(byte)message.getBytes()[i]);
		}
	}
	
	private void writeMessageAddresses(long address) {
		output.setSourceAddress(getAddress());
		output.setDestinationAddress(address);
	}
	
	public void sendRoutingPacket(String messageType, long address) {
		String message = new String(messageType + "," + state.toString());
		message = app.prepareRoutingPacket(message, address);
		
		writeMessage(message);
		writeMessageAddresses(address);
		
		mac.mcpsDataRequest(output);
		log("sending " + message + " to " + IEEEAddress.toDottedHex(address));
	}
	
	protected void forward(String message) {
		message = app.forwardData(message);
		
		if (message == null)
			return;
		
		writeMessage(message);
		writeMessageAddresses(getParentAddress());
		mac.mcpsDataRequest(output);
	}
	
	protected void addNeighbor(RoutingEntry entry) {
		int index = neighbors.indexOf(entry);
		
		if (index == -1) {
			neighbors.addElement(entry);
		} else {
			neighbors.setElementAt(entry, index);
		}
		
		log("adding neighbor " + IEEEAddress.toDottedHex(entry.source));
	}
	
	protected boolean notNewCycle(RoutingEntry sync) {
		return sync.cycle <= state.cycle;
	}
	
	protected int getBackoffTime() {
		return random.nextInt(BACKOFF_MAX_WAIT);
	}
	
	protected void resolveIsCoord() {
		int rules = app.getRoutingRules(neighbors, parent);
		
		if (rules < 0)
			rules = 0;
		else if (rules > ROUTING_RULES_MAX)
			rules = ROUTING_RULES_MAX;
		
		setCoord(random.nextInt(ROUTING_RULES_MAX) < rules);
	}
	
	protected void forceCoord() {
		app.forcedCoord();
		setCoord(true);
	}
	
	protected void notifyNewCycle() {
		app.startNewCycle(state.cycle, state.coord);
	}
	
	public long getParentAddress() {
		return parent.source;
	}
	
	public boolean isParentCoord() {
		return parent.coord;
	}
	
	public void setParentAsCoord() {
		parent.coord = true;
	}
	
	protected void handleSync(RoutingEntry sync) {
		addNeighbor(sync);
		refreshParent();
		
		if (notNewCycle(sync))
			return;

		resolveIsCoord();
		setCycle(sync.cycle);
		setHops(sync.hops + 1);
		
		sendRoutingPacket(SYNC, BROADCAST);
		waitNotInterrupted(getBackoffTime());
		
		refreshParent();
		
		if (isParentCoord())
			return;
		
		sendRoutingPacket(COORD, getParentAddress());
		setParentAsCoord();
	}

	private void handleCoord(RoutingEntry coord) {
		addNeighbor(coord);
		
		forceCoord();
		sendRoutingPacket(SYNC, BROADCAST);
	}
	
	private void handleData(DataMessage data) {
		forward(data.getMessage());
	}

	protected char[] handleMessage(RadioPacket packet) {
		for (int i = 0; i < packet.getMACPayloadLength(); i++) {
			buffer[i] = (char)packet.getMACPayloadAt(i);
		}
		
		Tokenizer tokenizer = new Tokenizer(buffer);
		long sourceAddress = packet.getSourceAddress();
		String messageType = tokenizer.nextToken();
		
		log("received " + new String(buffer) + " from " + IEEEAddress.toDottedHex(sourceAddress));
		
		if (messageType.equalsIgnoreCase(SYNC)) {
			handleSync(new RoutingEntry(sourceAddress, tokenizer));
		} else if (messageType.equalsIgnoreCase(COORD)) {
			handleCoord(new RoutingEntry(sourceAddress, tokenizer));
		} else if (messageType.equalsIgnoreCase(DATA)) {
			handleData(new DataMessage(tokenizer));
		}
		
		return buffer;
	}
	
	protected static boolean waitNotInterrupted(int inTime) {
		try {
			Thread.sleep(inTime);
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	public void startListening() {
		receiver.start();
	}
	
	public void interrupt() {
		receiver.interrupt();
	}

	public void setApp(Application app) {
		this.app = app;
	}
	
	public void run() {
		while (waitNotInterrupted(1)) {
			handleMessage(readPacket());
		}
	}

	public void startNewCycle(int backoffMultiplier) {
		setCycle(state.cycle + 1);
		sendRoutingPacket(RoutingInterface.SYNC, RoutingInterface.BROADCAST);
		waitNotInterrupted(backoffMultiplier * BACKOFF_MAX_WAIT);
	}
}