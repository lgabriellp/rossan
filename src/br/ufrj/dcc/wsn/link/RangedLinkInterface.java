package br.ufrj.dcc.wsn.link;

import java.util.Random;

import br.ufrj.dcc.wsn.util.Logger;

public class RangedLinkInterface implements ILinkInterface {
	private static Random random = new Random(System.currentTimeMillis());
	private static RangedLinkInterface instance;
	private ILinkInterface link;
	private Logger log;
	
	private int range;
	private int position;
	private int area;

	public static ILinkInterface getInstance() {
		if (instance == null)
			instance = new RangedLinkInterface();
		return instance;
	}
	
	private RangedLinkInterface() {
		this.link = LinkInterface.getInstance();
		this.log = link.getLog();
	
		this.area = 1000;
		this.range = 200;
		this.position = random.nextInt(area);
		
		log.info("at "+position);
	}

	private boolean isInRange(int position) {
		return Math.abs(this.position - position) <= range;
	}
	
	public PacketReader getReader() {
		PacketReader reader;
		int position;
		
		do {
			reader  = link.getReader();
			position = reader.getNextInt();
		} while (isInRange(position));
		
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
