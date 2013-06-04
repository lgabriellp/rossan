package br.ufrj.dcc.wsn.network.proc;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.link.PacketWriter;

public abstract class Message {
	private long address;

	public Message() {
	
	}
	
	public void setAddress(long address) {
		this.address = address;
	}
	
	public long getAddress() {
		return address;
	}
	
	public abstract int getLength();
	public abstract void readFrom(PacketReader reader);
	public abstract void writeInto(PacketWriter writer);
	public abstract String toString();
}
