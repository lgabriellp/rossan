package br.ufrj.dcc.wsn.link;

import com.sun.spot.peripheral.radio.RadioPacket;

public class PacketWriter {
	private final RadioPacket packet;
	private int position;

	public PacketWriter() {
		this.packet = RadioPacket.getDataPacket();
		this.position = 0;
	}
	
	public RadioPacket getPacket() {
		return packet;
	}

	public int advance(int bytes) {
		int position = this.position;
		this.position += bytes;
		return position;
	}
	
	public void setNext(byte value) {
		packet.setMACPayloadAt(advance(1), value);
	}
	
	public void setNext(boolean coord) {
		setNext((byte)(coord ? 1 : 0));
	}
	
	public void setNext(short value) {
		packet.setMACPayloadBigEndShortAt(advance(2), value);
	}
	
	public void setNext(int value) {
		packet.setMACPayloadBigEndIntAt(advance(4), value);
	}
	
	public void setNext(long value) {
		packet.setMACPayloadBigEndLongAt(advance(8), value);
	}
	
	public void setSourceAddress(long address) {
		packet.setSourceAddress(address);
	}
	
	public void setDestinationAddress(long address) {
		packet.setDestinationAddress(address);
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
	public int getPosition() {
		return position;
	}
	
	public void setLength(int capacity) {
		packet.setMACPayloadLength(capacity);
	}
	
	public void copyPayload(PacketReader reader) {
		setLength(reader.getLength());
		for (int i = 0; i < reader.getLength(); i++) {
			setNext(reader.getNextByte());
		}
	}
}
