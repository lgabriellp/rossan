package br.ufrj.dcc.wsn.link;

import java.util.Random;

import br.ufrj.dcc.wsn.util.Logger;

import com.sun.spot.peripheral.radio.I802_15_4_MAC;
import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.util.IEEEAddress;

public class LinkInterface implements ILinkInterface {
	private static ILinkInterface instance;
	private static final Random random = new Random();
	private static final int BIT_ERROR_INTERVAL = 10000;
	
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
		log.setLevel(Logger.DIGEST);
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
	
	private boolean anyBitError() {
		for (int k = 0; k < writer.getPacket().getMACPayloadLength(); k++) {
			if (random.nextInt(BIT_ERROR_INTERVAL) == 0)
				return true;
		}
		return false;
	}
	
	public boolean flush() {
		if (anyBitError())
			return false;
		
		while (mac.mcpsDataRequest(writer.getPacket()) != I802_15_4_MAC.SUCCESS);
		return true;
	}

	public long getAddress() {
		return RadioFactory.getRadioPolicyManager().getIEEEAddress();
	}

	public Logger getLog() {
		return log;
	}
}
