package br.ufrj.dcc.main;

import br.ufrj.dcc.mac.PacketReader;
import br.ufrj.dcc.routing.proc.Message;
import br.ufrj.dcc.routing.proc.Node;


public class AccessPointNode extends Node {
	private int cycle;
	
	public AccessPointNode() {
		super("AccessPoint");
		cycle = 1;
	}
	
	public Message processDataMessage(PacketReader reader) {
		log.info("PrepareDataMessage " + new HeatMessage(reader));
		return null;
	}
	
	protected boolean mainStep() {
		getRoutingInterface().startNewCycle(cycle++);
		return waitNotInterrupted(20000);
	}
}
