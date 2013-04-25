package br.ufrj.dcc.mac;

import com.sun.spot.peripheral.radio.I802_15_4_MAC;
import com.sun.spot.peripheral.radio.RadioFactory;

public class MacInterface {
	private static MacInterface instance;
	
	private final I802_15_4_MAC mac;
	private final PacketReader reader;
	private final PacketWriter writer;

	public static MacInterface getInstance() {
		if (instance == null)
			instance = new MacInterface();
		return instance;
	}
	
	private MacInterface() {
		this.mac = RadioFactory.getI802_15_4_MAC();
		this.reader = new PacketReader();
		this.writer = new PacketWriter();
	}
	
	public PacketReader getReader() {
		mac.mcpsDataIndication(reader.getPacket());
		reader.setPosition(0);
		return reader;
	}
	
	public PacketWriter getWriter() {
		writer.setPosition(0);
		return writer;
	}
	
	public void flush() {
		mac.mcpsDataRequest(writer.getPacket());
	}
}
