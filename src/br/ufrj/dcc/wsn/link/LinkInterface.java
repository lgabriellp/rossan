package br.ufrj.dcc.wsn.link;

import br.ufrj.dcc.wsn.util.Logger;

import com.sun.spot.peripheral.radio.I802_15_4_MAC;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.util.IEEEAddress;

public class LinkInterface implements ILinkInterface {
	private static ILinkInterface instance;
	
	private final I802_15_4_MAC mac;
	private final PacketReader reader;
	private final PacketWriter writer;
	private final Logger log;

	public static ILinkInterface getInstance() {
		if (instance == null)
			instance = new LinkInterface();
		return instance;
	}
	
	protected LinkInterface() {
		this.mac = RadioFactory.getI802_15_4_MAC();
		this.reader = new PacketReader();
		this.writer = new PacketWriter();
		this.log = new Logger(IEEEAddress.toDottedHex(getAddress()));
		log.setLevel(Logger.NET);
	}
	
	public PacketReader getReader() {
		mac.mcpsDataIndication(reader.getPacket());
		reader.setPosition(0);
		System.out.print("reading ");
		for (int i = 0; i < reader.getLength(); i++) {
			System.out.print(reader.getNextByte());
			System.out.print(' ');
		}
		System.out.println();
		reader.setPosition(0);
		return reader;
	}
	
	public PacketWriter getWriter() {
		writer.setPosition(0);
		return writer;
	}
	
	public boolean flush() {
		PacketReader reader = new PacketReader(writer.getPacket());
		reader.setPosition(0);
		System.out.print("writing ");
		for (int i = 0; i < reader.getLength(); i++) {
			System.out.print(reader.getNextByte());
			System.out.print(' ');
		}
		System.out.println();
		return mac.mcpsDataRequest(writer.getPacket()) == I802_15_4_MAC.SUCCESS; 
	}

	public long getAddress() {
		return RadioFactory.getRadioPolicyManager().getIEEEAddress();
	}

	public Logger getLog() {
		return log;
	}
}
