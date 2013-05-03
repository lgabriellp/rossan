package br.ufrj.dcc.wsn.link;

import java.util.Random;

import com.sun.spot.peripheral.radio.RadioFactory;
import com.sun.spot.util.IEEEAddress;

public class RangedLinkInterface implements ILinkInterface {
	private static Random random = new Random(System.currentTimeMillis());
	private static RangedLinkInterface instance;
	private ILinkInterface link;
	private int range;
	private int position;

	public static ILinkInterface getInstance() {
		if (instance == null)
			instance = new RangedLinkInterface();
		return instance;
	}
	
	private RangedLinkInterface() {
		this.link = LinkInterface.getInstance();
		this.range = 50;
		this.position = random.nextInt(100);
		
		System.out.println(IEEEAddress.toDottedHex(RadioFactory.getRadioPolicyManager().getIEEEAddress()) + " born in "+position);
	}

	private boolean isInRange(int position) {
		return Math.abs(this.position - position) <= range;
	}
	
	public PacketReader getReader() {
		PacketReader reader;
		
		while (true) {
			reader = link.getReader();
			int position = reader.getNextInt();
			
			if (isInRange(position))
				break;
			
			System.out.println(IEEEAddress.toDottedHex(reader.getSourceAddress()) + "is out of range");
		}
		
		System.out.println("Receiving from " + IEEEAddress.toDottedHex(reader.getSourceAddress()));
		
		return reader;
	}

	public PacketWriter getWriter() {
		PacketWriter writer = link.getWriter();
		writer.setNext(position);
		return writer;
	}

	public boolean flush() {
		return link.flush();
	}
}
