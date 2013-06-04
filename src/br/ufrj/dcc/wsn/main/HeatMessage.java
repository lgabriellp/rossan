package br.ufrj.dcc.wsn.main;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.link.PacketWriter;
import br.ufrj.dcc.wsn.network.proc.Message;


public class HeatMessage extends Message {
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

	public void readFrom(PacketReader reader) {
		temperature = reader.getNextShort();
	}
	
	public void writeInto(PacketWriter writer) {
		writer.setNext(temperature);
	}

	public String toString() {
		return "(heat," + temperature+")";
	}

	public int getLength() {
		return 2;
	}
}
