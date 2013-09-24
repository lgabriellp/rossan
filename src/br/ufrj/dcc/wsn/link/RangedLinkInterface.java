package br.ufrj.dcc.wsn.link;

import br.ufrj.dcc.wsn.util.Logger;

import com.sun.spot.peripheral.ISpot;
import com.sun.spot.peripheral.Spot;

public class RangedLinkInterface implements ILinkInterface {
	private static RangedLinkInterface instance;
	private ILinkInterface link;
	private Logger log;
	
	private int range;
	private int position;

	public static ILinkInterface getInstance() {
		if (instance == null)
			instance = new RangedLinkInterface();
		return instance;
	}
	
	private RangedLinkInterface() {
		this.link = LinkInterface.getInstance();
		this.log = link.getLog();
		ISpot self = Spot.getInstance();
		this.range = Integer.parseInt(self.getPersistentProperty("Range"));
		this.position = Integer.parseInt(self.getPersistentProperty("Position"));
		
		log.log(Logger.LINK, "position "+position+" range "+range);
	}

	private boolean notInRange(int position) {
		return Math.abs(this.position - position) > range;
	}
	
	public PacketReader getReader() {
		PacketReader reader;
		int position;
		
		do {
			reader  = link.getReader();
			position = reader.getNextInt();
		} while (notInRange(position));
		
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

	public long getAddress() {
		return link.getAddress();
	}

	public Logger getLog() {
		return link.getLog();
	}
}
