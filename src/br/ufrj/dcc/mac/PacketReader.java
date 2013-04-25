package br.ufrj.dcc.mac;

import com.sun.spot.peripheral.radio.RadioPacket;

public class PacketReader {
	private final RadioPacket packet;
	private int position;

	public PacketReader() {
		this.packet = RadioPacket.getDataPacket();
	}
	
	public RadioPacket getPacket() {
		return packet;
	}

	public int advance(int bytes) {
		int position = this.position;
		this.position += bytes;		
		return position;
	}
	
	public byte getNextByte() {
		return packet.getMACPayloadAt(advance(1));
	}
	
	public short getNextShort() {
		return (short)packet.getMACPayloadBigEndShortAt(advance(2));
	}
	
	public int getNextInt() {
		return packet.getMACPayloadBigEndIntAt(advance(4));
	}
	
	public long getNextLong() {
		return packet.getMACPayloadBigEndLongAt(advance(8));
	}
	
	public long getSourceAddress() {
		return packet.getSourceAddress();
	}
	
	public long getDestinationAddress() {
		return packet.getDestinationAddress();
	}
	
	public void setPosition(int position) {
		this.position = position;
	}
	
	public int getPosition() {
		return position;
	}
	
	public int getCapacity() {
		return packet.getMACPayloadLength();
	}
}
