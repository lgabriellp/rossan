package br.ufrj.dcc.main;

import br.ufrj.dcc.routing.proc.Node;


public class AccessPointNode extends Node {

	public AccessPointNode() {
		super("AccessPoint");
	}

	public String forwardData(String message, long address) {
		log.info("DataArrived " + message);
		return null;
	}
	
	protected boolean mainStep() {
		getRoutingInterface().startNewCycle(1);
		return waitNotInterrupted(20000);
	}
}
