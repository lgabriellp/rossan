package br.ufrj.dcc.wsn.main;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.link.PacketWriter;
import br.ufrj.dcc.wsn.network.proc.Message;


public class HeatMessage extends Message {
	private short temperature;
	
	public HeatMessage(short temperature) {
		this.temperature = temperature;
	}
	
	public HeatMessage(PacketReader reader) {
		super(reader);
		readFrom(reader);
	}

	public void readFrom(PacketReader reader) {
		temperature = reader.getNextShort();
	}
	
	public void writeInto(PacketWriter writer) {
		writer.setSourceAddress(getAddress());
		writer.setNext(temperature);
	}

	public String toString() {
		return "heat," + temperature;
	}

	public int getLength() {
		return 2;
	}
}
