package br.ufrj.dcc.routing.proc;

import br.ufrj.dcc.mac.PacketReader;
import br.ufrj.dcc.mac.PacketWriter;

public abstract class Message {
	private long address;

	protected Message() {
		address = 0;
	}
	
	protected Message(PacketReader reader) {
		address = reader.getSourceAddress();
	}
	
	public void setAddress(long address) {
		this.address = address;
	}
	
	public long getAddress() {
		return address;
	}
	
	public abstract int getSize();
	public abstract void readFrom(PacketReader reader);
	public abstract void writeTo(PacketWriter writer);
	public abstract String toString();
}
