package br.ufrj.dcc.proc;


public class AccessPointNode extends Node {

	public AccessPointNode() {
		super("AccessPoint");
	}

	public String forwardData(String message, long address) {
		log.info("DataArrived");
		return null;
	}
	
	protected void mainStep() {
		getRoutingInterface().startNewCycle();
		waitNotInterrupted(20000);
		log.info("Ready for new cycle");
	}
}
