package br.ufrj.dcc.wsn.main;

import br.ufrj.dcc.wsn.link.PacketReader;
import br.ufrj.dcc.wsn.network.proc.Message;
import br.ufrj.dcc.wsn.network.proc.Node;


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
