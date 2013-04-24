package br.ufrj.dcc.proc;


public class AccessPointNode extends Node {

	public AccessPointNode() {
		super("AccessPoint");
	}

	public String forwardData(String message, long address) {
		log("DataArrived " + message);
		return null;
	}
	
	protected void mainStep() {
		getRoutingInterface().startNewCycle(20);
		log("Ready for new cycle.");
	}
}
