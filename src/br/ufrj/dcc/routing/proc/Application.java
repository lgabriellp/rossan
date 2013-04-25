package br.ufrj.dcc.routing.proc;

import java.util.Vector;

import br.ufrj.dcc.mac.PacketReader;

public interface Application {

	public int getRoutingRules(Vector neighbors, RoutingEntry parent);

	public void startRoutingCycle(int cycle, boolean coord);
	
	public void joinedToBackbone();

	public Message processRoutingMessage(Message message, long address);

	public Message processDataMessage(PacketReader reader, long address);

	

}
