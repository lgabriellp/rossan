package br.ufrj.dcc.wsn.profile;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.link.PacketWriter;
import br.ufrj.dcc.wsn.network.proc.Message;
import br.ufrj.dcc.wsn.network.proc.RoutingEntry;

public class ProfileMessage extends Message {
	private long processingTime;
	private long receivedBytes;
	private long transmitedBytes;
	private byte cycle;
	private byte hops;
	private boolean coord;
	
	public ProfileMessage() {
		
	}

	public int getLength() {
		return 15;
	}

	public void readFrom(PacketReader reader) {
		processingTime = reader.getNextLong();
		receivedBytes = reader.getNextLong();
		transmitedBytes = reader.getNextLong();
		
		cycle = reader.getNextByte();
		hops = reader.getNextByte();
		coord = reader.getNextBoolean();
	}

	public void writeInto(PacketWriter writer) {
		writer.setNext(processingTime);
		writer.setNext(receivedBytes);
		writer.setNext(transmitedBytes);
		
		writer.setNext(cycle);
		writer.setNext(hops);
		writer.setNext(coord);
	}

	public String toString() {
		return "";
	}

	public void setRoutingState(RoutingEntry state) {
		cycle = state.getCycle();
		hops = state.getHops();
		coord = state.isCoord();
	}
	
	public void setProfileState(Profiler state) {
		processingTime = state.getProcessingTimeMs();
		receivedBytes = state.getReceivedBytes();
		transmitedBytes = state.getTransmitedBytes();
	}
}
