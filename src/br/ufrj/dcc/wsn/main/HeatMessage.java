package br.ufrj.dcc.wsn.main;

import com.sun.spot.util.IEEEAddress;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.link.PacketWriter;
import br.ufrj.dcc.wsn.network.proc.Message;


public class HeatMessage extends Message {
	private long origin;
	private short temperature;
	
	public HeatMessage() {
		this.temperature = 0;
	}
	
	public short getTemperature() {
		return temperature;
	}

	public void setTemperature(short temperature) {
		this.temperature = temperature;
	}

	public long getOrigin() {
		return origin;
	}

	public void setOrigin(long origin) {
		this.origin = origin;
	}
	
	public void readFrom(PacketReader reader) {
		setAddress(reader.getSourceAddress());
		origin = reader.getNextLong();
		temperature = reader.getNextShort();
	}

	public void writeInto(PacketWriter writer) {
		writer.setNext(origin);
		writer.setNext(temperature);
	}

	public String toString() {
		return "(heat,"+IEEEAddress.toDottedHex(origin)+","+temperature+")";
	}

	public int getLength() {
		return 10;
	}
}
