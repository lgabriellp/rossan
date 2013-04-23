package com.sun.spot.peripheral.radio;

import java.util.Random;
import java.util.Vector;

import com.sun.spot.peripheral.radio.proc.util.Tokenizer;
import com.sun.spot.util.IEEEAddress;


public class RoutingInterface {
	public static final long BROADCAST = 0xFFFF;
	public static final String SYNC = "sync";
	public static final String COORD = "coord";
	public static final String DATA = "data";
	public static final int BACKOFF_MAX_WAIT = 1000;
	
	private static final Random random = new Random();
	
	private char[] buffer = new char[RadioPacket.MIN_PAYLOAD_LENGTH];
	private I802_15_4_MAC mac;
	private Vector neighbors;
	private AccessPoint accessPoint;
	private Application app;
	
	private RoutingEntry state;
	private RoutingEntry parent;
	
	private Receiver receiver;
	private Sender sender;
	
	private RadioPacket input;
	private RadioPacket output;
	
	public RoutingInterface() {
		this.mac = RadioFactory.getI802_15_4_MAC();
		this.neighbors = new Vector();
		
		this.input = RadioPacket.getDataPacket();
		this.output = RadioPacket.getDataPacket();
	
		this.receiver = new Receiver();
		this.sender = new Sender();
		
		this.state = new RoutingEntry();
		this.parent = null;
	}

	public void log(String message) {
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
	}
	
	protected void setHops(int hops) {
		state.hops = hops;
		log("update hops to " + hops);
	}
	
	protected void setCoord(boolean coord) {
		state.coord = coord;
		log("update coord to " + coord);
	}
	
	protected RoutingEntry getParent() {
		return parent;
	}
	
	protected RadioPacket readPacket() {
		mac.mcpsDataIndication(input);
		return input;
	}

	private String writeRoutingMessage(String messageType) {
		String message = new String(messageType + "," + state);
		
		output.setMACPayloadLength(message.getBytes().length);
		for (int i = 0; i < output.getMACPayloadLength(); i++) {
			output.setMACPayloadAt(i,(byte)message.getBytes()[i]);
		}
		
		return message;
	}
	
	private void writeDestinationAddress(long address) {
		output.setSourceAddress(getAddress());
		output.setDestinationAddress(address);
	}
	
	public void sendRoutingPacket(String messageType, long address) {
		String message = writeRoutingMessage(messageType);
		writeDestinationAddress(address);
		
		log("sending " + message + " to " + IEEEAddress.toDottedHex(address));
		mac.mcpsDataRequest(output);
	}
	
	protected void addNeighbor(RoutingEntry entry) {
		neighbors.addElement(entry);
		log("adding neighbor " + IEEEAddress.toDottedHex(entry.source));
	}
	
	protected boolean notNewCycle(RoutingEntry sync) {
		return sync.cycle <= state.cycle;
	}
	
	protected int getBackoffTime() {
		return random.nextInt(BACKOFF_MAX_WAIT);
	}
	
	protected void handleSync(RoutingEntry sync) {
		addNeighbor(sync);
		refreshParent();
		
		if (notNewCycle(sync))
			return;

		setCycle(sync.cycle);
		setHops(sync.hops + 1);
		setCoord(false);
		
		sendRoutingPacket(SYNC, BROADCAST);
		waitNotInterrupted(getBackoffTime());
		
		refreshParent();
		
		if (getParent().coord)
			return;
		
		sendRoutingPacket(COORD, getParent().source);
		getParent().coord = true;
	}

	private void handleCoord(RoutingEntry coord) {
		addNeighbor(coord);
		
		setCoord(true);
		sendRoutingPacket(SYNC, BROADCAST);
	}
	
	private void handleData(DataMessage dataMessage) {
		
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

	protected void executeSendLoop() {
		
	}
	
	protected static boolean waitNotInterrupted(int inTime) {
		try {
			Thread.sleep(inTime);
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	public void start() {
		receiver.start();
		sender.start();
	}
	
	public void interrupt() {
		receiver.interrupt();
		sender.interrupt();
	}

	public void setAccessPoint(AccessPoint accessPoint) {
		this.accessPoint = accessPoint;
	}

	public void setApp(Application app) {
		this.app = app;
	}

	public class Sender extends Thread {
		public final void run() {
			while (waitNotInterrupted(1)) {
				executeSendLoop();
			}
		}
	}
	
	public class Receiver extends Thread {
		public void run() {
			while (waitNotInterrupted(1)) {
				handleMessage(readPacket());
			}
		}
	}
}