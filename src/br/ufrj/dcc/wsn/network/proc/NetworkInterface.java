package br.ufrj.dcc.wsn.network.proc;

import java.util.Random;
import java.util.Vector;

import br.ufrj.dcc.wsn.link.ILinkInterface;
import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.link.PacketWriter;
import br.ufrj.dcc.wsn.link.RangedLinkInterface;
import br.ufrj.dcc.wsn.profile.Profiler;
import br.ufrj.dcc.wsn.util.Sorter;

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
	private static NetworkInterface instance;

	private ILinkInterface link;
	
	private Vector neighbors;
	private Application app;
	private Thread receiver;
	private RoutingEntry mySelf;
	private RoutingEntry parent;
	private Sorter sorter;
	
	private NetworkInterface() {
		this.link = RangedLinkInterface.getInstance();
		this.neighbors = new Vector();
		this.receiver = new Thread(this);
		this.mySelf = new RoutingEntry();
		this.sorter = new Sorter(this.neighbors);
		this.parent = null;
		this.app = null;
	}
	
	public static NetworkInterface getInstance() {
		if (instance == null)
			instance = new NetworkInterface();
		return instance;
	}
	
	public boolean sendPacket(byte type, long address, Message message) {
		message = app.processRoutingMessage(message, address);
		
		int messageLength = 1 + message.getLength();
		
		PacketWriter writer = link.getWriter();
		writer.setSourceAddress(getAddress());
		writer.setDestinationAddress(address);
		writer.setLength(messageLength);
		writer.setNext(type);
		message.writeInto(writer);
		Profiler.getInstance().transmiting(messageLength);
		return link.flush();
	}
	
	public void sendRoutingPacket(byte type, long address) {
		sendPacket(type, address, mySelf);
	}
	
	public boolean hasNoRoute() {
		return parent == null;
	}
	
	public boolean sendDataPacket(Message message) {
		if (hasNoRoute())
			return false;
		
		return sendPacket(DATA, parent.getAddress(), message);
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
		mySelf.setEnergy(getSpentEnergy());
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
		mySelf.setEnergy(getSpentEnergy());
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
		Profiler.getInstance().stopProcessing();
		Message message = app.processDataMessage(reader);
		Profiler.getInstance().startProcessing();
		
		if (hasNoRoute() || message == null)
			return;
		
		sendDataPacket(message);
	}
	
	public boolean waitNotInterrupted(int inTime) {
		Profiler.getInstance().stopProcessing();
		
		try {
			Thread.sleep(inTime);
		} catch (InterruptedException e) {
			return false;
		}
		
		Profiler.getInstance().startProcessing();
		return true;
	}
	
	public void run() {
		Profiler.getInstance().startProcessing();
		
		while (waitNotInterrupted(1)) {
			PacketReader reader = link.getReader();
			Profiler.getInstance().receiving(reader.getLength());
			byte packetType = reader.getNextByte();
			
			if (packetType == SYNC) {
				handleSync(new RoutingEntry(reader));
			} else if (packetType == COORD) {
				handleCoord(new RoutingEntry(reader));
			} else {
				handleData(reader);
			}
		}
		
		Profiler.getInstance().stopProcessing();
	}
	
	public void startListening() {
		receiver.start();
	}
	
	public void interrupt() {
		receiver.interrupt();
	}

	public short getSpentEnergy() {
		return (short)Spot.getInstance().getPowerController().getBattery().getBatteryLevel();
		//return Profiler.getInstance().getSpentEnergy();
	}
	
	public long getAddress() {
		return RadioFactory.getRadioPolicyManager().getIEEEAddress();
	}
	
	public void setApp(Application app) {
		this.app = app;
	}
}