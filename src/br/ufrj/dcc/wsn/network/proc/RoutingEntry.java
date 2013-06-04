package br.ufrj.dcc.wsn.network.proc;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.link.PacketWriter;


public class RoutingEntry extends Message {
	private byte cycle;
	private byte hops;
	private boolean coord;
	private short energy;
	
	public RoutingEntry() {
		
	}
	
	public void readFrom(PacketReader reader) {
		setAddress(reader.getSourceAddress());
		cycle = reader.getNextByte();
		hops = reader.getNextByte();
		coord = reader.getNextBoolean();
		energy = reader.getNextShort();
	}
	
	public void writeInto(PacketWriter writer) {
		writer.setNext(cycle);
		writer.setNext(hops);
		writer.setNext(coord);
		writer.setNext(energy);
	}
	
	protected int coordCompare(RoutingEntry other) {
		int diff = hops - other.hops;
		if (diff != 0) return diff;
		
		diff = (coord ? 1 : 0) - (other.coord ? 1 : 0);
		if (diff != 0) return diff;
		
		return -(energy - other.energy);
	}
	
	protected int leafCompare(RoutingEntry other) {
		int diff = (coord ? 1 : 0) - (other.coord ? 1 : 0); 
		if (diff != 0) return diff;
		
		diff = hops - other.hops;
		if (diff != 0) return diff;
		
		return -(energy - other.energy);
	}
	
	public int compare(Object other, boolean coord) {
		if (coord) {
			return coordCompare((RoutingEntry)other);
		} else {
			return leafCompare((RoutingEntry)other);
		}
	}
	
	public String toString() {
		return 	"("+cycle + "," + hops + "," + coord + "," + energy+")";
	}
	
	public boolean equals(Object obj) {
		return getAddress() == ((RoutingEntry)obj).getAddress();
	}

	public byte getCycle() {
		return cycle;
	}

	public void setCycle(byte cycle) {
		this.cycle = cycle;
	}

	public byte getHops() {
		return hops;
	}

	public void setHops(byte hops) {
		this.hops = hops;
	}

	public boolean isCoord() {
		return coord;
	}

	public void setCoord(boolean coord) {
		this.coord = coord;
	}

	public short getEnergy() {
		return energy;
	}

	public void setEnergy(short energy) {
		this.energy = energy;
	}

	public int getLength() {
		return 5;
	}
}
