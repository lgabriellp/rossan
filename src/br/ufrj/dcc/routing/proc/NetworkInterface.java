package br.ufrj.dcc.routing.proc;

import java.util.Random;
import java.util.Vector;

import br.ufrj.dcc.mac.MacInterface;
import br.ufrj.dcc.mac.PacketReader;
import br.ufrj.dcc.mac.PacketWriter;
import br.ufrj.dcc.util.Sorter;

import com.sun.spot.peripheral.Spot;
import com.sun.spot.peripheral.radio.RadioFactory;


public class NetworkInterface implements Runnable {
	public static final long BROADCAST = 0xFFFF;
	public static final byte SYNC = 1;
	public static final byte COORD = 2;
	public static final byte DATA = 3;
	public static final int BACKOFF_MAX_WAIT = 1000;
	public static final int ROUTING_RULES_MAX = 100;
	
	private static final Random random = new Random();

	private MacInterface mac;
	
	private Vector neighbors;
	private Application app;
	private Thread receiver;
	private RoutingEntry mySelf;
	private RoutingEntry parent;
	private Sorter sorter;
	
	public NetworkInterface(Application app) {
		this.mac = MacInterface.getInstance();
		this.neighbors = new Vector();
		this.receiver = new Thread(this);
		this.mySelf = new RoutingEntry();
		this.sorter = new Sorter(this.neighbors);
		this.parent = null;
		this.app = app;
	}
	
	public void sendPacket(byte type, long address, Message message) {
		message = app.processRoutingMessage(message, address);
		
		PacketWriter writer = mac.getWriter();
		writer.setSourceAddress(getAddress());
		writer.setDestinationAddress(address);
		writer.setSize(1 + message.getSize());
		writer.setNext(type);
		message.writeTo(writer);
		mac.flush();
	}
	
	public void sendRoutingPacket(byte type, long address) {
		sendPacket(type, address, mySelf);
	}
	
	public boolean hasNoRoute() {
		return parent == null;
	}
	
	public void sendDataPacket(Message message) {
		if (hasNoRoute())
			return;
		
		sendPacket(DATA, parent.getAddress(), message);
	}
	
	private void addNeighbor(RoutingEntry entry) {
		int index = neighbors.indexOf(entry);

		if (index == -1) {
			neighbors.addElement(entry);
		} else {
			neighbors.setElementAt(entry, index);
		}
	}
	
	private void resolveBelongToBackbone() {
		int rules = app.getRoutingRules(neighbors, parent);

		if (rules < 0)
			rules = 0;
		else if (rules > ROUTING_RULES_MAX)
			rules = ROUTING_RULES_MAX;

		mySelf.setCoord(random.nextInt(ROUTING_RULES_MAX) < rules);
	}
	
	protected int getBackoffTime() {
		return random.nextInt(BACKOFF_MAX_WAIT);
	}
	
	public void startNewCycle(int cycle) {
		mySelf.setCycle((byte)cycle);
		mySelf.setHops((byte)0);
		mySelf.setCoord(false);
		mySelf.setEnergy(getEnergy());
		mySelf.setAddress(getAddress());
		
		sendRoutingPacket(SYNC, BROADCAST);
		app.startRoutingCycle(mySelf.getCycle(), mySelf.isCoord());
	}
	
	private boolean startNewCycle(RoutingEntry sync) {
		if (sync.getCycle() <= mySelf.getCycle())
			return false;
		
		resolveBelongToBackbone();
		mySelf.setCycle(sync.getCycle());
		mySelf.setHops((byte)(sync.getHops() + 1));
		mySelf.setEnergy(getEnergy());
		mySelf.setAddress(getAddress());
		
		sendRoutingPacket(SYNC, BROADCAST);
		app.startRoutingCycle(mySelf.getCycle(), mySelf.isCoord());
		waitNotInterrupted(getBackoffTime());
		
		return true;
	}

	private void refreshParent() {
		sorter.sort(mySelf.isCoord());
		parent = (RoutingEntry)neighbors.elementAt(0);
	}
	
	private boolean doesParentBelongToBackbone() {
		return parent.isCoord();
	}
	
	private boolean remainWithoutRoute() {
		refreshParent();
		
		return doesParentBelongToBackbone();
	}

	private void forceRouteThrougth(byte type, RoutingEntry entry) {
		entry.setCoord(true);
		sendRoutingPacket(type, entry.getAddress());
	}
	
	private void forceRouteThrougthMySelf() {
		forceRouteThrougth(SYNC, mySelf);
		app.joinedToBackbone();
	}
	
	private void forceRouteThrougthParent() {
		forceRouteThrougth(COORD, parent);
	}
	
	private void handleSync(RoutingEntry sync) {
		addNeighbor(sync);
		
		if (!startNewCycle(sync))
			return;
		
		if (!remainWithoutRoute())
			return;
		
		forceRouteThrougthParent();
	}

	private void handleCoord(RoutingEntry coord) {
		addNeighbor(coord);
		
		forceRouteThrougthMySelf();
	}
	
	private void handleData(PacketReader reader) {
		Message message = app.processDataMessage(reader);
		
		if (hasNoRoute() || message == null)
			return;
		
		sendDataPacket(message);
	}
	
	public boolean waitNotInterrupted(int inTime) {
		try {
			Thread.sleep(inTime);
		} catch (InterruptedException e) {
			return false;
		}
		return true;
	}
	
	public void run() {
		while (waitNotInterrupted(1)) {
			PacketReader reader = mac.getReader();
			
			byte packetType = reader.getNextByte();
			
			if (packetType == SYNC) {
				handleSync(new RoutingEntry(reader));
			} else if (packetType == COORD) {
				handleCoord(new RoutingEntry(reader));
			} else {
				handleData(reader);
			}
		}
	}
	
	public void startListening() {
		receiver.start();
	}
	
	public void interrupt() {
		receiver.interrupt();
	}

	public short getEnergy() {
		return (short)Spot.getInstance().getPowerController().getBattery().getBatteryLevel();
	}
	
	public long getAddress() {
		return RadioFactory.getRadioPolicyManager().getIEEEAddress();
	}
	
	public void setApp(Application app) {
		this.app = app;
	}
}