package com.sun.spot.peripheral.radio;

import java.util.Random;
import java.util.Vector;

import com.sun.spot.peripheral.IBattery;
import com.sun.spot.peripheral.Spot;
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
	private IBattery battery;
	private Vector neighbors;
	private Application app;
	private Thread receiver;
	private RadioPacket input;
	private RadioPacket output;
	private RoutingEntry state;
	private RoutingEntry parent;
	private int debugLevel;
	
	public RoutingInterface(Application app) {
		this.mac = RadioFactory.getI802_15_4_MAC();
		this.battery = Spot.getInstance().getPowerController().getBattery();
		this.neighbors = new Vector();
		this.input = RadioPacket.getDataPacket();
		this.output = RadioPacket.getDataPacket();
		this.receiver = new Thread(this);
		this.state = new RoutingEntry();
		this.parent = null;
		this.app = app;
		this.debugLevel = 2;
	}

	public void setDebug(int debug) {
		this.debugLevel = debug;
	}
	
	private void log(String message) {
		System.out.println("[" + IEEEAddress.toDottedHex(getAddress()) + "] " + message);
	}
	
	public void debug(String message) {
		if (debugLevel <= 0) log(message);
	}
	
	public void warning(String message) {
		if (debugLevel <= 1) log(message);
	}
	
	public void info(String message) {
		if (debugLevel <= 2) log(message);
	}
	
	public int getEnergy() {
		return battery.getBatteryLevel();
	}
	
	private void updateEnergy() {
		state.energy = getEnergy();
	}
	
	public long getAddress() {
		return RadioFactory.getRadioPolicyManager().getIEEEAddress();
	}
	
	public void setCycle(int cycle) {
		state.cycle = cycle;
		warning("update cycle to " + cycle);
		notifyNewCycle();
	}
	
	protected void setHops(int hops) {
		state.hops = hops;
		warning("update hops to " + hops);
	}
	
	protected void setCoord(boolean coord) {
		state.coord = coord;
		warning("update coord to " + coord);
	}
	
	protected RadioPacket readPacket() {
		mac.mcpsDataIndication(input);
		return input;
	}
	
	protected String getRoutingMessage() {
		return state.toString();
	}
	
	protected void addNeighbor(RoutingEntry entry) {
		int index = neighbors.indexOf(entry);
		
		if (index == -1) {
			neighbors.addElement(entry);
		} else {
			neighbors.setElementAt(entry, index);
		}
		
		warning("adding neighbor " + IEEEAddress.toDottedHex(entry.source));
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
		info("update parent to " + IEEEAddress.toDottedHex(parent.source));
	}
	
	protected void writeMessage(String message) {
		output.setMACPayloadLength(message.getBytes().length);
		for (int i = 0; i < message.getBytes().length; i++) {
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
		debug("sending " + message + " to " + IEEEAddress.toDottedHex(address));
	}
	
	public boolean sendDataPacket(String message) {
		if (parent == null)
			return false;
		
		message = new String(DATA + "," + message);
		
		writeMessage(message);
		writeMessageAddresses(getParentAddress());
		
		mac.mcpsDataRequest(output);
		debug("sending " + message + " to " + IEEEAddress.toDottedHex(getParentAddress()));
		
		return true;
	}
	
	protected void forward(String message, long from) {
		if (parent == null)
			return;
		
		message = app.forwardData(message, getParentAddress());
		
		if (message == null)
			return;
		
		writeMessage(message);
		writeMessageAddresses(getParentAddress());
		mac.mcpsDataRequest(output);
		info("forwarding " + message + " from " + IEEEAddress.toDottedHex(from) + " to " + IEEEAddress.toDottedHex(getParentAddress()));
	}
	
	protected boolean notNewCycle(RoutingEntry sync) {
		return sync.cycle <= state.cycle;
	}
	
	public void startNewCycle(int backoffMultiplier) {
		setCycle(state.cycle + 1);
		sendRoutingPacket(RoutingInterface.SYNC, RoutingInterface.BROADCAST);
		waitNotInterrupted(backoffMultiplier * BACKOFF_MAX_WAIT);
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
		app.newCycleStarted(state.cycle, state.coord);
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
		
		if (notNewCycle(sync))
			return;

		refreshParent();
		updateEnergy();
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
		forward(data.getMessage(), data.getSource());
	}

	protected char[] handleMessage(RadioPacket packet) {
		for (int i = 0; i < buffer.length; i++) buffer[i] = 0;
		for (int i = 0; i < packet.getMACPayloadLength(); i++) {
			buffer[i] = (char)packet.getMACPayloadAt(i);
		}
		
		Tokenizer tokenizer = new Tokenizer(buffer);
		long sourceAddress = packet.getSourceAddress();
		String messageType = tokenizer.nextToken();
		
		debug("received " + new String(buffer) + " from " + IEEEAddress.toDottedHex(sourceAddress));
		
		if (messageType.equalsIgnoreCase(SYNC)) {
			handleSync(new RoutingEntry(sourceAddress, tokenizer));
		} else if (messageType.equalsIgnoreCase(COORD)) {
			handleCoord(new RoutingEntry(sourceAddress, tokenizer));
		} else if (messageType.equalsIgnoreCase(DATA)) {
			handleData(new DataMessage(sourceAddress, tokenizer));
		}
		
		return buffer;
	}
	
	public boolean waitNotInterrupted(int inTime) {
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
}