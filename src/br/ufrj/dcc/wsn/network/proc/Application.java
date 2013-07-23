package br.ufrj.dcc.wsn.network.proc;

import java.util.Vector;

import br.ufrj.dcc.wsn.link.PacketReader;

public interface Application {

	public int getRoutingRules(Vector neighbors, RoutingEntry parent);

	public void startRoutingCycle(int cycle, boolean coord);
	
	public void joinedBackbone();

	public Message processRoutingMessage(Message message, long address);

	public Message processDataMessage(PacketReader reader);

	

}
